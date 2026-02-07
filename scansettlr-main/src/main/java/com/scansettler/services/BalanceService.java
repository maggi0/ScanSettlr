package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.User;
import com.scansettler.repositories.ExpenseRepository;
import com.scansettler.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BalanceService
{
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BalanceService(ExpenseRepository expenseRepository, UserRepository userRepository)
    {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public Map<String, BigDecimal> calculateBalances(Set<String> expenseIds)
    {
        List<Expense> expenses = expenseRepository.findAllById(expenseIds);

        Map<String, BigDecimal> balances = new HashMap<>();

        for (Expense expense : expenses)
        {
            String lenderId = expense.getLenderId();
            String lenderUsername = userRepository.findById(lenderId)
                    .map(User::getUsername)
                    .get();

            BigDecimal totalAmount = expense.getBorrowerDetails()
                    .values()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            balances.put(lenderUsername,
                    balances.getOrDefault(lenderUsername, BigDecimal.ZERO).add(totalAmount));

            for (Map.Entry<String, BigDecimal> entry : expense.getBorrowerDetails().entrySet())
            {
                String borrowerId = entry.getKey();
                String borrowerUsername = userRepository.findById(borrowerId)
                        .map(User::getUsername)
                        .get();

                BigDecimal amount = entry.getValue();

                balances.put(borrowerUsername,
                        balances.getOrDefault(borrowerUsername, BigDecimal.ZERO).subtract(amount));
            }
        }

        return balances;
    }
}
