package com.scansettler.models;

import lombok.Getter;

public class Rate
{
    @Getter
    private String currency;
    @Getter
    private String code;
    @Getter
    private double mid;
}

