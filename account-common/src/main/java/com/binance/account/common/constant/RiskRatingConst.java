package com.binance.account.common.constant;


import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class RiskRatingConst {
    public static final Map<String,String> riskRatingMap = new HashMap<>();
    static {
        riskRatingMap.put("AR", "L");
        riskRatingMap.put("AM", "L");
        riskRatingMap.put("AU", "L");
        riskRatingMap.put("AT", "L");
        riskRatingMap.put("AZ", "L");
        riskRatingMap.put("BE", "L");
        riskRatingMap.put("BR", "M");
        riskRatingMap.put("BG", "L");
        riskRatingMap.put("CA", "L");
        riskRatingMap.put("CL", "L");
        riskRatingMap.put("HR", "M");
        riskRatingMap.put("CY", "L");
        riskRatingMap.put("CZ", "L");
        riskRatingMap.put("DK", "L");
        riskRatingMap.put("EE", "L");
        riskRatingMap.put("SZ", "M");
        riskRatingMap.put("FI", "L");
        riskRatingMap.put("DE", "L");
        riskRatingMap.put("GI", "L");
        riskRatingMap.put("GR", "L");
        riskRatingMap.put("HK", "L");
        riskRatingMap.put("HU", "L");
        riskRatingMap.put("IS", "L");
        riskRatingMap.put("IE", "L");
        riskRatingMap.put("IL", "L");
        riskRatingMap.put("IT", "L");
        riskRatingMap.put("JM", "L");
        riskRatingMap.put("JP", "L");
        riskRatingMap.put("JE", "L");
        riskRatingMap.put("LV", "L");
        riskRatingMap.put("LI", "L");
        riskRatingMap.put("LT", "L");
        riskRatingMap.put("LU", "L");
        riskRatingMap.put("MO", "L");
        riskRatingMap.put("MT", "L");
        riskRatingMap.put("MU", "L");
        riskRatingMap.put("MX", "M");
        riskRatingMap.put("MC", "L");
        riskRatingMap.put("NL", "L");
        riskRatingMap.put("NZ", "L");
        riskRatingMap.put("NO", "L");
        riskRatingMap.put("PE", "L");
        riskRatingMap.put("PL", "L");
        riskRatingMap.put("PT", "L");
        riskRatingMap.put("RO", "L");
        riskRatingMap.put("SG", "L");
        riskRatingMap.put("SK", "L");
        riskRatingMap.put("SI", "L");
        riskRatingMap.put("ZA", "L");
        riskRatingMap.put("KR", "L");
        riskRatingMap.put("ES", "L");
        riskRatingMap.put("SE", "L");
        riskRatingMap.put("CH", "L");
        riskRatingMap.put("TR", "M");
        riskRatingMap.put("AE", "L");
        riskRatingMap.put("GB", "L");
        riskRatingMap.put("UY", "L");
    }
        public static String transfer(String country) {
            return StringUtils.isEmpty(riskRatingMap.get(country)) ? "L" : riskRatingMap.get(country);
        }
}
