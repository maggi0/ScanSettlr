package com.scansettler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Getter
@Setter
@AllArgsConstructor
public class Settlement
{
    @Id
    private String id;
    private String expenseGroupId;
    private String lender;
    private String borrower;
    private BigDecimal amount;
}
