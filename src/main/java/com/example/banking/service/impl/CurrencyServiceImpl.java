package com.example.banking.service.impl;

import com.example.banking.repository.CurrencyRepository;
import com.example.banking.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }


    @Override
    public Map<String, String> getExchangeRates() {
        Map<String, String> result = new HashMap<>();
        currencyRepository.findAll().stream()
                .filter(currency -> !currency.getName().equals("uah"))
                .forEach(currency -> {
                    result.put(currency.getName() + "BuyingExchangeRate", currency.getBuyingExchangeRate().toString());
                    result.put(currency.getName() + "SalesExchangeRate", currency.getSalesExchangeRate().toString());
                });
        return result;
    }
}