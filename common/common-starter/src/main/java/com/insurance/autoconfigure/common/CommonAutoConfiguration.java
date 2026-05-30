package com.insurance.autoconfigure.common;

import com.insurance.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CommonConfiguration.class})
public class CommonAutoConfiguration {
}

