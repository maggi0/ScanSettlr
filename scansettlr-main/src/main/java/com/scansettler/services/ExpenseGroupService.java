package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.Settlement;
import com.scansettler.models.User;
import com.scansettler.repositories.ExpenseGroupRepository;
import com.scansettler.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ExpenseGroupService
{
    public final ExpenseGroupRepository expenseGroupRepository;
    private final CustomUserDetailsService userDetailsService;
    private final ExpenseRepository expenseRepository;
    private final SettlementService settlementService;
    private final ExpenseService expenseService;

    public ExpenseGroupService(ExpenseGroupRepository expenseGroupRepository, CustomUserDetailsService userDetailsService, ExpenseRepository expenseRepository, SettlementService settlementService, ExpenseService expenseService)
    {
        this.expenseGroupRepository = expenseGroupRepository;
        this.userDetailsService = userDetailsService;
        this.expenseRepository = expenseRepository;
        this.settlementService = settlementService;
        this.expenseService = expenseService;
    }

    public ExpenseGroup getExpenseGroupById(String id)
    {
        return expenseGroupRepository.findById(id).orElseThrow(() -> new NoSuchElementException("ExpenseGroup with id " + id + " not found"));
    }

    public List<ExpenseGroup> getExpenseGroupsByIds(Set<String> ids)
    {
        return expenseGroupRepository.findAllById(ids);
    }

    public ExpenseGroup createExpenseGroup(User user, String name)
    {
        ExpenseGroup expenseGroup = ExpenseGroup.builder()
                .name(name)
                .expenseIds(Set.of())
                .userIds(Set.of())
                .build();

        user.getExpenseGroupIds().add(expenseGroup.getId());

        return expenseGroupRepository.save(expenseGroup);
    }

    public ExpenseGroup addUsersToExpenseGroup(String expenseGroupId, List<String> userIds)
    {
        ExpenseGroup expenseGroup = getExpenseGroupById(expenseGroupId);
        for (String userId : userIds)
        {
            if (expenseGroup.getUserIds() == null)
            {
                expenseGroup.setUserIds(new HashSet<>());
            }

            expenseGroup.getUserIds().add(userId);
            userDetailsService.addExpenseGroup(userId, expenseGroupId);
        }

        return expenseGroupRepository.save(expenseGroup);
    }

    public Expense addExpense(Expense expense)
    {
        Expense saved = expenseRepository.save(expense);

        ExpenseGroup expenseGroup = getExpenseGroupById(expense.getExpenseGroupId());

        expenseGroup.getExpenseIds().add(expense.getId());
        expenseGroup.setSettlements(settlementService.calculateSettlements(expenseGroup.getId(), expenseGroup.getExpenseIds()));

        save(expenseGroup);

        return saved;
    }

    public void recalculateSettlements(String id)
    {
        ExpenseGroup expenseGroup = getExpenseGroupById(id);

        expenseGroup.setSettlements(settlementService.calculateSettlements(expenseGroup.getId(), expenseGroup.getExpenseIds()));

        save(expenseGroup);
    }

    public void removeUserFromExpenseGroup(String expenseGroupId, String userId)
    {
        ExpenseGroup expenseGroup = getExpenseGroupById(expenseGroupId);

        expenseGroup.getUserIds().remove(userId);

        expenseGroupRepository.save(expenseGroup);
    }

    public void removeSettlement(String expenseGroupId, Settlement settlement)
    {
        String borrowerName = userDetailsService.getUserByUsername(settlement.getBorrower()).getUsername();
        String lenderName =  userDetailsService.getUserByUsername(settlement.getLender()).getUsername();
        String borrowerId = userDetailsService.getUserByUsername(settlement.getBorrower()).getId();
        String lenderId = userDetailsService.getUserByUsername(settlement.getLender()).getId();

        ExpenseGroup expenseGroup = getExpenseGroupById(expenseGroupId);

        addExpense(Expense.builder()
                .expenseGroupId(expenseGroup.getId())
                .name(borrowerName + " -> " + lenderName)
                .lenderId(borrowerId)
                .borrowerDetails(Map.of(lenderId, settlement.getAmount()))
                .build());
    }

    public void deleteExpenseGroup(String id)
    {
        expenseGroupRepository.delete(getExpenseGroupById(id));
    }

    public void save(ExpenseGroup expenseGroup)
    {
        expenseGroupRepository.save(expenseGroup);
    }
}
