package com.insurance.product.autoconfigure.api;

import com.insurance.product.api.ProductApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ProductApiConfiguration.class})
public class ProductApiAutoConfiguration {}
