package com.scansettler.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Document
@Getter
@Setter
@Builder
public class Expense
{
    @Id
    private String id;
    private String expenseGroupId;
    private String name;
    private String lenderId;
    private Map<String, BigDecimal> borrowerDetails;
    private List<Item> items;
}
