package com.scansettler.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
@Getter
@Setter
@Builder
public class ExpenseGroup
{
    @Id
    private String id;
    private String name;
    private Set<String> userIds;
    private Set<String> expenseIds;
    private Set<Settlement> settlements;
}
