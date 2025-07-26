package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ExpenseService
{
    private final ExpenseRepository expenseRepository;
    private final ExpenseGroupService expenseGroupService;
    private final SettlementService settlementService;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseGroupService expenseGroupService, SettlementService settlementService)
    {
        this.expenseRepository = expenseRepository;
        this.expenseGroupService = expenseGroupService;
        this.settlementService = settlementService;
    }

    public Expense getExpenseById(String id)
    {
        return expenseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Expense with id " + id + " not found"));
    }

    public Expense addExpense(Expense expense)
    {
        Expense saved = expenseRepository.save(expense);

        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(expense.getExpenseGroupId());

        expenseGroup.getExpenseIds().add(expense.getId());
        expenseGroup.setSettlements(settlementService.calculateSettlements(expenseGroup.getExpenseIds()));

        expenseGroupService.save(expenseGroup);

        return saved;
    }
}
