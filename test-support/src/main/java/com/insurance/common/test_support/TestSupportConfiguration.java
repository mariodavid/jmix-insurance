package com.insurance.common.test_support;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EntityTestData.class)
public class TestSupportConfiguration {}
