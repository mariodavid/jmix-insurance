package com.insurance.product.autoconfigure.ui;

import com.insurance.product.ui.ProductUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ProductUiConfiguration.class})
public class ProductUiAutoConfiguration {}
