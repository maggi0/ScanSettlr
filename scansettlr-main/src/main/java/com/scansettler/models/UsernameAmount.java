package com.scansettler.models;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record UsernameAmount(String name, BigDecimal amount)
{
    //
}
