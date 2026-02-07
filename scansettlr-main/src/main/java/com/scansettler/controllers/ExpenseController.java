package com.scansettler.controllers;

import com.scansettler.models.Expense;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.Item;
import com.scansettler.models.UsernameAmount;
import com.scansettler.schema.CreateExpenseRequest;
import com.scansettler.schema.GetExpenseResponse;
import com.scansettler.services.CustomUserDetailsService;
import com.scansettler.services.ExpenseGroupService;
import com.scansettler.services.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expense")
public record ExpenseController(ExpenseService expenseService, ExpenseGroupService expenseGroupService, CustomUserDetailsService customUserDetailsService)
{
    @GetMapping("/{id}")
    public GetExpenseResponse getExpense(@PathVariable String id)
    {
        Expense expense = expenseService.getExpenseById(id);

        String lenderName = customUserDetailsService.getUserById(expense.getLenderId()).getUsername();
        BigDecimal lenderAmount = expense.getBorrowerDetails().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<UsernameAmount> borrowers = mapBorrowerDetails(expense.getBorrowerDetails());

        return GetExpenseResponse.builder()
                .name(expense.getName())
                .lender(UsernameAmount.builder().name(lenderName).amount(lenderAmount).build())
                .borrowers(borrowers)
                .build();
    }

    @GetMapping("/expenseGroup/{expenseGroupId}")
    public List<Expense> getExpensesByExpenseGroupId(@PathVariable String expenseGroupId)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(expenseGroupId);

        return expenseService.getExpensesByIds(expenseGroup.getExpenseIds());
    }

    @PostMapping("/{id}/items")
    public Expense addItems(@RequestBody List<Item> items, @PathVariable String id)
    {
        Expense expense = expenseService.getExpenseById(id);

        expense.setItems(items);

        return expenseService.save(expense);
    }

    @GetMapping("/{id}/items")
    public List<Item> getItems(@PathVariable String id)
    {
        Expense expense = expenseService.getExpenseById(id);

        return expense.getItems();
    }

    @PostMapping
    public Expense addExpense(@RequestBody CreateExpenseRequest request)
    {
        return expenseGroupService.addExpense(createExpense(request));
    }

    @PutMapping
    public Expense modifyExpense(@RequestBody CreateExpenseRequest request)
    {
        Expense expense = expenseService.modifyExpense(createExpense(request));
        expenseGroupService.recalculateSettlements(expense.getExpenseGroupId());
        return expense;
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
            .map(entry -> {
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
}
