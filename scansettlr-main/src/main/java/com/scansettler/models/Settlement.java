package com.scansettler.models;

import java.math.BigDecimal;

public record Settlement(String lenderId, String borrowerId, BigDecimal amount)
{
    //
}
