package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.Settlement;
import com.scansettler.models.UserBalance;
import com.scansettler.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SettlementService
{
    private final ExpenseRepository expenseRepository;

    public SettlementService(ExpenseRepository expenseRepository)
    {
        this.expenseRepository = expenseRepository;
    }

    public Set<Settlement> calculateSettlements(Set<String> expenseIds)
    {
        List<Expense> expenses = expenseRepository.findAllById(expenseIds);

        Map<String, BigDecimal> balances = new HashMap<>();

        for (Expense expense : expenses)
        {
            String lenderId = expense.getLenderId();
            BigDecimal totalAmount = expense.getBorrowerDetails()
                    .values()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            balances.put(lenderId, balances.getOrDefault(lenderId, BigDecimal.ZERO).add(totalAmount));

            for (Map.Entry<String, BigDecimal> entry : expense.getBorrowerDetails().entrySet())
            {
                String borrowerId = entry.getKey();
                BigDecimal amount = entry.getValue();

                balances.put(borrowerId, balances.getOrDefault(borrowerId, BigDecimal.ZERO).subtract(amount));
            }
        }

        PriorityQueue<UserBalance> creditors = new PriorityQueue<>((a, b) -> b.getBalance().compareTo(a.getBalance()));
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(Comparator.comparing(UserBalance::getBalance));

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet())
        {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) > 0)
            {
                creditors.add(new UserBalance(entry.getKey(), balance));
            }
            else if (balance.compareTo(BigDecimal.ZERO) < 0)
            {
                debtors.add(new UserBalance(entry.getKey(), balance));
            }
        }

        Set<Settlement> settlements = new HashSet<>();

        while (!creditors.isEmpty() && !debtors.isEmpty())
        {
            UserBalance creditor = creditors.poll();
            UserBalance debtor = debtors.poll();

            BigDecimal amount = creditor.getBalance().min(debtor.getBalance().abs());

            settlements.add(new Settlement(
                    creditor.getUserId(),
                    debtor.getUserId(),
                    amount
            ));

            creditor.setBalance(creditor.getBalance().subtract(amount));
            debtor.setBalance(debtor.getBalance().add(amount));

            if (creditor.getBalance().compareTo(BigDecimal.ZERO) > 0)
            {
                creditors.add(creditor);
            }

            if (debtor.getBalance().compareTo(BigDecimal.ZERO) < 0)
            {
                debtors.add(debtor);
            }
        }

        return settlements;
    }
}
