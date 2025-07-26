package com.scansettler.preload;

import com.scansettler.models.ExpenseGroup;
import com.scansettler.repositories.ExpenseGroupRepository;
import com.scansettler.services.SettlementService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Order(3)
public class ExpenseGroupLoader implements ApplicationRunner
{
    private final ExpenseGroupRepository expenseGroupRepository;
    private final SettlementService settlementService;

    public ExpenseGroupLoader(ExpenseGroupRepository expenseGroupRepository, SettlementService settlementService)
    {
        this.expenseGroupRepository = expenseGroupRepository;
        this.settlementService = settlementService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        List<ExpenseGroup> initialExpenseGroups = List.of(
                createExpenseGroup("1", "Bieszczady", Set.of("1", "2"), Set.of("1", "2"))
        );

        expenseGroupRepository.saveAll(initialExpenseGroups);
    }

    private ExpenseGroup createExpenseGroup(String id, String name, Set<String> userIds, Set<String> expenseIds)
    {
        return ExpenseGroup.builder()
                .id(id)
                .name(name)
                .userIds(userIds)
                .expenseIds(expenseIds)
                .settlements(settlementService.calculateSettlements(expenseIds))
                .build();
    }
}
