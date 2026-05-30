package com.insurance.app.test_support;

import com.insurance.quote.core.entity.Quote;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuoteFactory {

    @Autowired
    private DataManager dataManager;

    public QuoteData.Builder defaultData() {
        return QuoteData.builder();
    }

    public Quote save(QuoteData data) {
        Quote quote = dataManager.create(Quote.class);
        quote.setPartnerNo(data.partnerNo());
        quote.setStatus(data.status());
        quote.setProductType(data.productType());
        quote.setProductVariant(data.productVariant());
        quote.setPaymentFrequency(data.paymentFrequency());
        quote.setInsuranceProduct(data.insuranceProduct());
        quote.setEffectiveDate(data.effectiveDate());
        quote.setSquareMeters(data.squareMeters());
        quote.setCalculatedPremium(data.calculatedPremium());
        quote.setValidFrom(data.validFrom());
        quote.setValidUntil(data.validUntil());
        return dataManager.save(quote);
    }

    public Quote saveDefault() {
        return save(defaultData().build());
    }
}
