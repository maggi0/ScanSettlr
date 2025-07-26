package com.scansettler.controllers;

import com.scansettler.models.Expense;
import com.scansettler.services.ExpenseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense")
public record ExpenseController(ExpenseService expenseService)
{
    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable String id)
    {
        return expenseService.getExpenseById(id);
    }

    @PostMapping
    public Expense addExpense(@RequestBody Expense expense)
    {
        return expenseService.addExpense(expense);
    }
}
