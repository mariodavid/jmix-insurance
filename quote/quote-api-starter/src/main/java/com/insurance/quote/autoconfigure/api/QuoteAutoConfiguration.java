package com.insurance.quote.autoconfigure.api;

import com.insurance.quote.api.QuoteApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({QuoteApiConfiguration.class})
public class QuoteAutoConfiguration {}
