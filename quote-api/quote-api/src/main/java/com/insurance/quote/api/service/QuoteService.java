package com.insurance.quote.api.service;

import io.jmix.core.Id;

public interface QuoteService {

    void reject(Id<?> quoteId);

    Object accept(Id<?> quoteId);
}
