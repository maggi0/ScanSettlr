package com.scansettler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scansettler.models.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrencyService
{
    private static final String NBP_URL = "https://api.nbp.pl/api/exchangerates/tables/A?format=json";
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000;

    private static volatile long lastFetchTime = 0;
    private static volatile List<Rate> cachedRates = null;

    public List<String> getCurrencies() throws IOException, InterruptedException
    {
        fetchRatesIfNeeded();
        return cachedRates.stream()
                .map(Rate::getCode)
                .toList();
    }

    public BigDecimal convertFromPln(BigDecimal amountPln, String targetCurrency) throws IOException, InterruptedException
    {
        fetchRatesIfNeeded();

        if (targetCurrency.equalsIgnoreCase("PLN"))
        {
            return amountPln;
        }

        Rate rate = cachedRates.stream()
                .filter(r -> r.getCode().equalsIgnoreCase(targetCurrency))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + targetCurrency));

        return amountPln.divide(BigDecimal.valueOf(rate.getMid()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertToPln(BigDecimal amount, String sourceCurrency) throws IOException, InterruptedException
    {
        fetchRatesIfNeeded();

        if ("PLN".equalsIgnoreCase(sourceCurrency))
        {
            return amount;
        }

        Rate rate = cachedRates.stream()
                .filter(r -> r.getCode().equalsIgnoreCase(sourceCurrency))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + sourceCurrency));

        return amount.multiply(BigDecimal.valueOf(rate.getMid())).setScale(2, RoundingMode.HALF_UP);
    }

    private void fetchRatesIfNeeded() throws IOException, InterruptedException
    {
        long now = System.currentTimeMillis();

        if (cachedRates != null && (now - lastFetchTime) < CACHE_TTL_MS) return;

        synchronized (CurrencyService.class)
        {
            if (cachedRates != null && (now - lastFetchTime) < CACHE_TTL_MS) return;

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NBP_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            List<ExchangeTable> tables = mapper.readValue(response.body(), new TypeReference<>()
            {
            });

            cachedRates = tables.get(0).getRates();
            lastFetchTime = now;
        }
    }

    public List<Item> convertItems(List<Item> items, String targetCurrency, boolean fromPln)
    {
        return items.stream().map(item ->
        {
            try
            {
                BigDecimal convertedAmount = fromPln ?
                        convertFromPln(item.getAmount(), targetCurrency) :
                        convertToPln(item.getAmount(), targetCurrency);

                return new Item(item.getId(), item.getName(), convertedAmount, item.getPaidBy());
            }
            catch (IOException | InterruptedException e)
            {
                throw new RuntimeException("Failed to convert item: " + item.getName(), e);
            }
        }).toList();
    }

    public List<Expense> convertExpenses(List<Expense> expenses, String targetCurrency, boolean fromPln)
    {
        return expenses.stream().map(expense ->
        {
            Map<String, BigDecimal> convertedBorrowers = expense.getBorrowerDetails().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry ->
                            {
                                try
                                {
                                    return fromPln ?
                                            convertFromPln(entry.getValue(), targetCurrency) :
                                            convertToPln(entry.getValue(), targetCurrency);
                                }
                                catch (IOException | InterruptedException e)
                                {
                                    throw new RuntimeException("Failed to convert borrower amount for user " + entry.getKey(), e);
                                }
                            }
                    ));

            return new Expense(expense.getId(), expense.getExpenseGroupId(), expense.getName(),
                    expense.getLenderId(), convertedBorrowers, expense.getItems());
        }).toList();
    }

    public Set<Settlement> convertSettlements(Set<Settlement> settlements, String targetCurrency, boolean fromPln)
    {
        return settlements.stream().map(settlement ->
        {
            try
            {
                BigDecimal convertedAmount = fromPln
                        ? convertFromPln(settlement.getAmount(), targetCurrency)
                        : convertToPln(settlement.getAmount(), targetCurrency);

                return new Settlement(
                        settlement.getId(),
                        settlement.getExpenseGroupId(),
                        settlement.getLender(),
                        settlement.getBorrower(),
                        convertedAmount
                );
            }
            catch (IOException | InterruptedException e)
            {
                throw new RuntimeException("Failed to convert settlement amount: " + e.getMessage());
            }
        }).collect(Collectors.toSet());
    }

    public Map<String, BigDecimal> convertBalances(Map<String, BigDecimal> balances, String currency, boolean fromPln)
    {
        return balances.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry ->
                        {
                            try
                            {
                                return fromPln
                                        ? convertFromPln(entry.getValue(), currency)
                                        : convertToPln(entry.getValue(), currency);
                            }
                            catch (IOException | InterruptedException e)
                            {
                                throw new RuntimeException("Failed to convert balance for user " + entry.getKey(), e);
                            }
                        }));
    }
}
