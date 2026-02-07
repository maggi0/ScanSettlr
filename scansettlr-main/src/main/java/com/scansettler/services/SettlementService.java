package com.scansettler.services;

import com.scansettler.models.Expense;
import com.scansettler.models.Settlement;
import com.scansettler.models.UserBalance;
import com.scansettler.repositories.ExpenseRepository;
import com.scansettler.repositories.SettlementRepository;
import com.scansettler.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SettlementService
{
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    public SettlementService(ExpenseRepository expenseRepository, SettlementRepository settlementRepository, UserRepository userRepository)
    {
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
    }

    public Settlement findById(String id)
    {
        return settlementRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Settlement with id " + id + " not found"));
    }

    public void saveAll(Set<Settlement> settlements)
    {
        settlementRepository.saveAll(settlements);
    }

    public Set<Settlement> calculateSettlements(String expenseGroupId, Set<String> expenseIds)
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

            String creditorUsername = userRepository.findById(creditor.getUserId()).get().getUsername();
            String debtorUsername = userRepository.findById(debtor.getUserId()).get().getUsername();

            settlements.add(new Settlement(
                    null,
                    expenseGroupId,
                    creditorUsername,
                    debtorUsername,
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

        saveAll(settlements);
        return settlements;
    }
}
