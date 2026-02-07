package com.scansettler.controllers;

import com.scansettler.services.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/currency")
public record CurrencyController(CurrencyService currencyService)
{
    @GetMapping
    public List<String> getCurrencies() throws IOException, InterruptedException
    {
        return currencyService.getCurrencies();
    }
}
