package com.scansettler.controllers;

import com.scansettler.models.Settlement;
import com.scansettler.services.CustomUserDetailsService;
import com.scansettler.services.ExpenseGroupService;
import com.scansettler.services.SettlementService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlement")
public record SettlementController(SettlementService settlementService, ExpenseGroupService expenseGroupService, CustomUserDetailsService customUserDetailsService)
{
    @PutMapping("/{id}/pay")
    public void markSettlementAsPaid(@PathVariable String id)
    {
        Settlement settlement = settlementService.findById(id);
        String expenseGroupId = settlement.getExpenseGroupId();
        expenseGroupService.removeSettlement(expenseGroupId, settlement);
    }
}
