package com.scansettler.controllers;

import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.User;
import com.scansettler.models.CustomUserDetails;
import com.scansettler.services.ExpenseGroupService;
import com.scansettler.services.CustomUserDetailsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expenseGroup")
public record ExpenseGroupController(ExpenseGroupService expenseGroupService, CustomUserDetailsService customUserDetailsService)
{
    @GetMapping("/{id}")
    public ExpenseGroup getExpenseGroup(@PathVariable String id)
    {
        return expenseGroupService.getExpenseGroupById(id);
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
