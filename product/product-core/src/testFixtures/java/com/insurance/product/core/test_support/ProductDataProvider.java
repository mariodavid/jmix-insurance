package com.insurance.product.core.test_support;

/**
 * Test data helper for the Product module.
 * Since all insurance products, types, and variants are modeled as Java enums
 * (e.g. {@link com.insurance.product.api.dto.InsuranceProduct}), there are no persistent
 * JPA entities in this module. Consequently, a traditional {@code TestDataProvider} implementation
 * is not required. This class serves as a conceptual placeholder and documenter.
 */
public class ProductDataProvider {
    private ProductDataProvider() {
        // empty helper
    }
}
