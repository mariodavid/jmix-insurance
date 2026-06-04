package com.insurance.quote.autoconfigure.core;

import com.insurance.quote.core.QuoteConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({QuoteConfiguration.class})
public class QuoteAutoConfiguration {}
