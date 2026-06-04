package com.insurance.quote.autoconfigure.ui;

import com.insurance.quote.ui.QuoteUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({QuoteUiConfiguration.class})
public class QuoteUiAutoConfiguration {}
