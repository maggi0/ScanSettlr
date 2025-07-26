package com.scansettler.services;

import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.User;
import com.scansettler.repositories.ExpenseGroupRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;


@Service
public class ExpenseGroupService
{
    public final ExpenseGroupRepository expenseGroupRepository;

    public ExpenseGroupService(ExpenseGroupRepository expenseGroupRepository)
    {
        this.expenseGroupRepository = expenseGroupRepository;
    }

    public ExpenseGroup getExpenseGroupById(String id)
    {
        return expenseGroupRepository.findById(id).orElseThrow(() -> new NoSuchElementException("ExpenseGroup with id " + id + " not found"));
    }

    public ExpenseGroup createExpenseGroup(User user, String name)
    {
        ExpenseGroup expenseGroup = ExpenseGroup.builder()
                .name(name)
                .build();

        user.getExpenseGroupIds().add(expenseGroup.getId());

        return expenseGroupRepository.save(expenseGroup);
    }

    public ExpenseGroup addUserToExpenseGroup(String expenseGroupId, String userId)
    {
        ExpenseGroup expenseGroup = getExpenseGroupById(expenseGroupId);

        expenseGroup.getUserIds().add(userId);

        return expenseGroupRepository.save(expenseGroup);
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
