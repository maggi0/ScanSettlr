package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ExpenseService
{
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository)
    {
        this.expenseRepository = expenseRepository;
    }

    public Expense save(Expense expense)
    {
        return expenseRepository.save(expense);
    }

    public Expense getExpenseById(String id)
    {
        return expenseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Expense with id " + id + " not found"));
    }

    public Expense modifyExpense(Expense expense)
    {
        Expense currentExpense = expenseRepository.findById(expense.getId()).get();

        currentExpense.setName(expense.getName());
        currentExpense.setLenderId(expense.getLenderId());
        currentExpense.setBorrowerDetails(expense.getBorrowerDetails());

        save(currentExpense);

        return currentExpense;
    }

    public List<Expense> getExpensesByIds(Set<String> expenseIds)
    {
        return expenseRepository.findAllById(expenseIds);
    }
}
