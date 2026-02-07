package com.scansettler.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Getter
@Setter
@Builder
public class Item
{
    @Id
    private String id;
    private String name;
    private BigDecimal amount;
    private String paidBy;
}
