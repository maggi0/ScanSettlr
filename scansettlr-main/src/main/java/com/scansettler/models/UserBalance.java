package com.scansettler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class UserBalance
{
    private String userId;
    private BigDecimal balance;
}
