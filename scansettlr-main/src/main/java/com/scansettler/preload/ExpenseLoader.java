package com.scansettler.preload;

import com.scansettler.models.Expense;
import com.scansettler.repositories.ExpenseRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Order(2)
public class ExpenseLoader implements ApplicationRunner
{
    private final ExpenseRepository expenseRepository;

    public ExpenseLoader(ExpenseRepository expenseRepository)
    {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        List<Expense> expenseList = List.of(
                createExpense("1", "1", "zakupy1", "1", Map.of("2", BigDecimal.TEN)),
                createExpense("2", "1", "zakupy2", "2", Map.of("1", BigDecimal.valueOf(15)))
        );

        expenseRepository.saveAll(expenseList);
    }

    private Expense createExpense(String id, String expenseGroupId, String name, String lenderId, Map<String, BigDecimal> borrowerDetails)
    {
        return Expense.builder()
                .id(id)
                .expenseGroupId(expenseGroupId)
                .name(name)
                .lenderId(lenderId)
                .borrowerDetails(borrowerDetails)
                .build();
    }
}
