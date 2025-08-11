package com.scansettler.controllers;

import com.scansettler.models.CustomUserDetails;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.Settlement;
import com.scansettler.models.User;
import com.scansettler.services.CustomUserDetailsService;
import com.scansettler.services.ExpenseGroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/expenseGroup")
public record ExpenseGroupController(ExpenseGroupService expenseGroupService, CustomUserDetailsService customUserDetailsService)
{
    @GetMapping("/{id}")
    public ExpenseGroup getExpenseGroup(@PathVariable String id)
    {
        return expenseGroupService.getExpenseGroupById(id);
    }

    @GetMapping("/user/{username}")
    public List<ExpenseGroup> getExpenseGroupsByUsername(@PathVariable String username)
    {
        User user = customUserDetailsService.getUserByUsername(username);

        return expenseGroupService.getExpenseGroupsByIds(user.getExpenseGroupIds());
    }

    @GetMapping("/{id}/settlements")
    public Set<Settlement> getSettlements(@PathVariable String id)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(id);

        return expenseGroup.getSettlements();
    }

    @GetMapping("/{id}/balances")
    public Map<String, BigDecimal> getBalances(@PathVariable String id)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(id);

        return expenseGroup.getBalances();
    }

    @PostMapping
    public ExpenseGroup createExpenseGroup(@AuthenticationPrincipal CustomUserDetails customUserDetails, String name)
    {
        User user = customUserDetailsService.getUserById(customUserDetails.getId());
        return expenseGroupService.createExpenseGroup(user, name);
    }

    @PostMapping("/addUser")
    public ExpenseGroup addUserToExpenseGroup(String expenseGroupId, String userId)
    {
        return expenseGroupService.addUserToExpenseGroup(expenseGroupId, userId);
    }

    @DeleteMapping
    public void deleteExpenseGroup(String id)
    {
        expenseGroupService.deleteExpenseGroup(id);
    }
}
