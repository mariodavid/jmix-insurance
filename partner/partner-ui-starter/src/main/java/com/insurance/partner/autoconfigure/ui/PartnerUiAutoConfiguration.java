package com.insurance.partner.autoconfigure.ui;

import com.insurance.partner.ui.PartnerUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PartnerUiConfiguration.class})
public class PartnerUiAutoConfiguration {}
