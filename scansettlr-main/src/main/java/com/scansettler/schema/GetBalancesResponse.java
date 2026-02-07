package com.scansettler.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class GetBalancesResponse
{
    private Map<String, BigDecimal> balances;
}
