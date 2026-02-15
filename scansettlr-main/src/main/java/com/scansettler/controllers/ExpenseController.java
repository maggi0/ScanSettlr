package com.scansettler.controllers;

import com.scansettler.models.Expense;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.Item;
import com.scansettler.models.UsernameAmount;
import com.scansettler.schema.CreateExpenseRequest;
import com.scansettler.schema.GetExpenseResponse;
import com.scansettler.services.CurrencyService;
import com.scansettler.services.CustomUserDetailsService;
import com.scansettler.services.ExpenseGroupService;
import com.scansettler.services.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expense")
public record ExpenseController(ExpenseService expenseService, ExpenseGroupService expenseGroupService,
                                CustomUserDetailsService customUserDetailsService, CurrencyService currencyService)
{
    @GetMapping("/{id}")
    public GetExpenseResponse getExpense(@PathVariable String id, @RequestParam(required = false) String currency)
    {
        Expense expense = expenseService.getExpenseById(id);

        Map<String, BigDecimal> borrowers;

        String targetCurrency = currencyOrDefault(currency);
        if (!targetCurrency.equals("PLN"))
        {
            borrowers = currencyService.convertBalances(expense.getBorrowerDetails(), targetCurrency, true);
        }
        else
        {
            borrowers = expense.getBorrowerDetails();
        }

        String lenderName = customUserDetailsService.getUserById(expense.getLenderId()).getUsername();
        BigDecimal lenderAmount = borrowers.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<UsernameAmount> mappedBorrowers = mapBorrowerDetails(borrowers);

        return GetExpenseResponse.builder()
                .name(expense.getName())
                .lender(UsernameAmount.builder().name(lenderName).amount(lenderAmount).build())
                .borrowers(mappedBorrowers)
                .build();
    }

    @GetMapping("/expenseGroup/{expenseGroupId}")
    public List<Expense> getExpensesByExpenseGroupId(@PathVariable String expenseGroupId, @RequestParam(required = false) String currency)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(expenseGroupId);
        List<Expense> expenses = expenseService.getExpensesByIds(expenseGroup.getExpenseIds());

        String targetCurrency = currencyOrDefault(currency);

        if (!targetCurrency.equals("PLN"))
        {
            return currencyService.convertExpenses(expenses, targetCurrency, true);
        }

        return expenses;
    }

    @PostMapping("/{id}/items")
    public Expense addItems(@RequestBody List<Item> items, @PathVariable String id, @RequestParam(required = false) String currency)
    {
        Expense expense = expenseService.getExpenseById(id);

        String targetCurrency = currencyOrDefault(currency);
        if (!targetCurrency.equals("PLN"))
        {
            expense.setItems(currencyService.convertItems(items, targetCurrency, false));
        }
        else
        {
            expense.setItems(items);
        }

        return expenseService.save(expense);
    }

    @GetMapping("/{id}/items")
    public List<Item> getItems(@PathVariable String id, @RequestParam(required = false) String currency)
    {
        Expense expense = expenseService.getExpenseById(id);

        String targetCurrency = currencyOrDefault(currency);

        if (!targetCurrency.equals("PLN"))
        {
            return currencyService.convertItems(expense.getItems(), targetCurrency, true);
        }

        return expense.getItems();
    }

    @PostMapping
    public Expense addExpense(@RequestBody CreateExpenseRequest request, @RequestParam(required = false) String currency)
    {
        Expense expense = createExpense(request);

        String targetCurrency = currencyOrDefault(currency);

        if (!targetCurrency.equals("PLN"))
        {
            expense.setBorrowerDetails(currencyService.convertBalances(expense.getBorrowerDetails(), targetCurrency, false));
        }

        return expenseGroupService.addExpense(expense);
    }

    @PutMapping
    public Expense modifyExpense(@RequestBody CreateExpenseRequest request, @RequestParam(required = false) String currency)
    {
        Expense expense = createExpense(request);

        String targetCurrency = currencyOrDefault(currency);

        if (!targetCurrency.equals("PLN"))
        {
            expense.setBorrowerDetails(currencyService.convertBalances(expense.getBorrowerDetails(), targetCurrency, false));
        }

        Expense saved = expenseService.modifyExpense(expense);
        expenseGroupService.recalculateSettlements(saved.getExpenseGroupId());

        return saved;
    }

    private Expense createExpense(CreateExpenseRequest request)
    {
        return Expense.builder()
                .id(request.expenseId())
                .expenseGroupId(request.expenseGroupId())
                .name(request.name())
                .lenderId(request.lenderId())
                .borrowerDetails(request.borrowerDetails())
                .build();
    }

    private List<UsernameAmount> mapBorrowerDetails(Map<String, BigDecimal> borrowerDetails)
    {
        return borrowerDetails.entrySet().stream()
                .map(entry ->
                {
                    String userId = entry.getKey();
                    BigDecimal amount = entry.getValue();

                    String username = customUserDetailsService
                            .getUserById(userId)
                            .getUsername();

                    return UsernameAmount.builder()
                            .name(username)
                            .amount(amount)
                            .build();
                })
                .toList();
    }

    private String currencyOrDefault(String currency)
    {
        return (currency == null || currency.isBlank()) ? "PLN" : currency.toUpperCase();
    }
}
