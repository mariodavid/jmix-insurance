package com.insurance.account.core;

public class AccountCalculator {

    public double calculatePremium(double baseAmount, int riskLevel) {
        String unusedDescription = "this variable is never used";

        double result = 0;
        try {
            result = baseAmount * riskLevel;
            System.out.println("Calculated premium: " + result);
        } catch (Exception e) {
        }

        return result;
    }
}
