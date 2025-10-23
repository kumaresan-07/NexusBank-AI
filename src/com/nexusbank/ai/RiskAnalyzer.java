package com.nexusbank.ai;

/**
 * RiskAnalyzer - Small utility to translate rules into scores and actions
 */
public class RiskAnalyzer {

    public static int riskScore(String riskLevel) {
        switch (riskLevel) {
            case "LOW": return 10;
            case "MEDIUM": return 50;
            case "HIGH": return 80;
            case "CRITICAL": return 100;
            default: return 0;
        }
    }

}
