package com.scansettler.controllers;

import com.scansettler.models.CustomUserDetails;
import com.scansettler.models.ExpenseGroup;
import com.scansettler.models.Settlement;
import com.scansettler.models.User;
import com.scansettler.schema.AddUsersRequest;
import com.scansettler.schema.CreateExpenseGroupRequest;
import com.scansettler.schema.GetBalancesResponse;
import com.scansettler.services.BalanceService;
import com.scansettler.services.CurrencyService;
import com.scansettler.services.CustomUserDetailsService;
import com.scansettler.services.ExpenseGroupService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/expenseGroup")
public record ExpenseGroupController(ExpenseGroupService expenseGroupService, CustomUserDetailsService customUserDetailsService, BalanceService balanceService, CurrencyService currencyService)
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
    public Set<Settlement> getSettlements(@PathVariable String id, @RequestParam(required = false) String currency)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(id);

        String targetCurrency = currencyOrDefault(currency);

        if (!targetCurrency.equals("PLN"))
        {
            return currencyService.convertSettlements(expenseGroup.getSettlements(), targetCurrency, true);
        }

        return expenseGroup.getSettlements();
    }

    @GetMapping("/{id}/balances")
    public GetBalancesResponse getBalances(@PathVariable String id, @RequestParam(required = false) String currency)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(id);
        Map<String, BigDecimal> balances = balanceService.calculateBalances(expenseGroup.getExpenseIds());
        Set<String> userIds = getUsers(id).stream().map(User::getUsername).collect(Collectors.toSet());
        userIds.forEach(userId -> balances.putIfAbsent(userId, BigDecimal.ZERO));

        String targetCurrency = currencyOrDefault(currency);
        if (!targetCurrency.equals("PLN"))
        {
            return new GetBalancesResponse(currencyService.convertBalances(balances, currencyOrDefault(currency), true));
        }

        return new GetBalancesResponse(balances);
    }

    @GetMapping("/{id}/users")
    public List<User> getUsers(@PathVariable String id)
    {
        ExpenseGroup expenseGroup = expenseGroupService.getExpenseGroupById(id);

        return customUserDetailsService.getUsersByIds(expenseGroup.getUserIds());
    }

    @PostMapping
    public ExpenseGroup createExpenseGroup(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody CreateExpenseGroupRequest request)
    {
        User user = customUserDetailsService.getUserById(customUserDetails.getId());
        return expenseGroupService.createExpenseGroup(user, request.name());
    }

    @PostMapping("/{id}/users")
    public ExpenseGroup addUsersToExpenseGroup(@PathVariable String id, @RequestBody AddUsersRequest request)
    {
        return expenseGroupService.addUsersToExpenseGroup(id, request.ids());
    }

    @DeleteMapping("/{id}/users/{userId}")
    public void removeUserFromExpenseGroup(@PathVariable String id, @PathVariable String userId)
    {
        expenseGroupService.removeUserFromExpenseGroup(id, userId);
    }

    @DeleteMapping
    public void deleteExpenseGroup(String id)
    {
        expenseGroupService.deleteExpenseGroup(id);
    }

    private String currencyOrDefault(String currency)
    {
        return (currency == null || currency.isBlank()) ? "PLN" : currency.toUpperCase();
    }
}
