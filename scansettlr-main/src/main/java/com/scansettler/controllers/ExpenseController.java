package com.scansettler.controllers;

import com.scansettler.models.Expense;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.services.ExpenseGroupService;
import com.scansettler.services.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense")
public record ExpenseController(ExpenseService expenseService, ExpenseGroupService expenseGroupService)
{
    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable String id)
    {
        return expenseService.getExpenseById(id);
    }

    @GetMapping("/expenseGroup/{expenseGroupId}")
    public List<Expense> getExpensesByExpenseGroupId(@PathVariable String expenseGroupId)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(expenseGroupId);

        return expenseService.getExpensesByIds(expenseGroup.getExpenseIds());
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense)
    {
        return expenseService.addExpense(expense);
    }
}
