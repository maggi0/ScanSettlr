package com.scansettler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeTable
{
    @Getter
    @Setter
    private List<Rate> rates;
}
