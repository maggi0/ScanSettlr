package com.scansettler.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scansettler.models.ExchangeTable;
import com.scansettler.models.Rate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class CurrencyService
{
    private static final String NBP_URL = "https://api.nbp.pl/api/exchangerates/tables/A?format=json";
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000;

    private static volatile long lastFetchTime = 0;
    private static volatile List<String> cachedCurrencies = null;

    public List<String> getCurrencies() throws IOException, InterruptedException
    {
        long now = System.currentTimeMillis();

        if (cachedCurrencies != null && (now - lastFetchTime) < CACHE_TTL_MS)
        {
            return cachedCurrencies;
        }

        synchronized (CurrencyService.class)
        {
            if (cachedCurrencies != null && (now - lastFetchTime) < CACHE_TTL_MS)
            {
                return cachedCurrencies;
            }

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NBP_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();

            List<ExchangeTable> tables = mapper.readValue(
                    response.body(),
                    new TypeReference<>()
                    {
                    }
            );

            return tables.getFirst().getRates().stream()
                    .map(Rate::getCode)
                    .toList();
        }
    }
}
