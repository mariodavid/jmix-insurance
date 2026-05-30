package com.insurance.app.test_support;

public record PartnerData(String partnerNo, String firstName, String lastName) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String partnerNo;
        private String firstName = "Max";
        private String lastName = "Mustermann";

        public Builder partnerNo(String partnerNo) { this.partnerNo = partnerNo; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName)   { this.lastName = lastName;   return this; }

        public PartnerData build() {
            return new PartnerData(partnerNo, firstName, lastName);
        }
    }
}
