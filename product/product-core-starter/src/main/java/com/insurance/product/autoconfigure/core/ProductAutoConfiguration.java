package com.insurance.product.autoconfigure.core;

import com.insurance.product.core.ProductConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ProductConfiguration.class})
public class ProductAutoConfiguration {
}
