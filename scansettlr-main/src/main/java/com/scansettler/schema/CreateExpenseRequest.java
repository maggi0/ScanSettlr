package com.scansettler.schema;

import java.math.BigDecimal;
import java.util.Map;

public record CreateExpenseRequest(String expenseId, String expenseGroupId, String name, String lenderId,
        Map<String, BigDecimal> borrowerDetails)
{
    //
}