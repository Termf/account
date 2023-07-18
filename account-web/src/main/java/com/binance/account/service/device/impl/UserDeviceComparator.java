package com.binance.account.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.master.enums.TerminalEnum;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log4j2
public class UserDeviceComparator {



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceComparisonResult {

        private boolean same;

        private double score;

        private UserDevice mostSimilar;

        private int version;

    }

    private static final String PROP_TIMEZONE = "timezone";

    private static final String PROP_TIMEZONE_OFFSET = "timezoneOffset";

    private static final String PROP_PLATFORM = "platform";

    private static final String PROP_UA = "user_agent";



    // 可容忍的时区差， 默认120分钟
    @Value("${user.device.tz.offset.tolerance:120}")
    private Integer timezoneOffsetTolerance;

    // 分数超过该值就认为是同一个设备
    @Value("${user.device.same.threshold:0.75}")
    private Double sameDeviceThreshold;

    // 逻辑回归待定系数
    @Value("${user.device.alg.undetermined.coefficient:-26.06423624438798}")
    private Double undeterminedCoefficient;

    @Value("${user.device.web.weight:}")
    private String webDeviceWeight;

    @Value("${user.device.v2.alg.version:6}")
    @Getter
    private int algVersion;

    @Value("${user.device.v2.alg.switch:true}")
    private boolean alg2Switch;
//
//    @Value("${user.device.ios.weight:}")
//    private String iosDeviceWeight;
//
//    @Value("${user.device.android.weight:}")
//    private String androidDeviceWeight;

    @Value("${spring.profiles.active}")
    private String active;


    private Map<String, Object> getPropertyWeight(String agentType) {
        if (TerminalEnum.WEB.getCode().equals(agentType)) {
            return JSON.parseObject(webDeviceWeight);
        }
//        } else if ("ios".equals(agentType)) {
//            return JSON.parseObject(iosDeviceWeight);
//        } else if ("android".equals(agentType)) {
//            return JSON.parseObject(androidDeviceWeight);
//        }
        return null;
    }

    /**
     *
     * Python Levenshtein.ratio() 的Java实现
     *
     * @param str1
     * @param str2
     * @return
     */
    public static double levenshteinRatio(String str1, String str2) {
        str1 = ObjectUtils.defaultIfNull(str1, "");
        str2 = ObjectUtils.defaultIfNull(str2, "");
        if (StringUtils.equals(str1, str2)) {
            return 1.0;
        }

        int d[][];
        int n = str1.length();
        int m = str2.length();
        int i;
        int j;
        char ch1;
        char ch2;
        int temp;
        if (n == 0) {
            return 0;
        }
        if (m == 0) {
            return 0;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) {
            ch1 = str1.charAt(i - 1);
            for (j = 1; j <= m; j++) {
                ch2 = str2.charAt(j - 1);
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 2;
                }
                d[i][j] =
                        Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + temp));
            }
        }
        return (double) (m + n - d[n][m]) / (double) (m + n);
    }

    public DeviceComparisonResult compare(Map<String, String> candidate, Map<String, String> target, String agentType) {
        if (!alg2Switch) {
            log.info("user.device.v2.alg.switch off. ignore");
        }
        Map<String, Object> weightMap = getPropertyWeight(agentType);
        if (MapUtils.isEmpty(weightMap)) {
            log.info("couldn't find weight map for clienttype: {}", agentType);
            return new DeviceComparisonResult(false, 0d, null, algVersion);
        }

        // 检查时区
        if (!compareTimezone(candidate, target)) {
            return new DeviceComparisonResult(false, 0d, null, algVersion);
        }

        // 检查platform
        if (!comparePlatform(agentType, candidate, target)) {
            return new DeviceComparisonResult(false, 0d, null, algVersion);
        }

        // 线性输出z
        double z = undeterminedCoefficient;
        for (Map.Entry<String, String> targetEntry : target.entrySet()) {
            String candidateValue = candidate.get(targetEntry.getKey());
            String tarValue = targetEntry.getValue();
            final Object w = weightMap.get(targetEntry.getKey());
            if (w == null) {
                log.info("ignore key: {}", targetEntry.getKey());
                continue;
            }
            double propWeight = new Double(w.toString());
            double levenRatio = levenshteinRatio(candidateValue, tarValue);
            z += (propWeight * levenRatio);

            if (com.binance.master.utils.StringUtils.endsWithAny(active.toLowerCase(), "-local", "-dev", "-qa")) {
                log.info("calculating levenshtein ratio of property: {}, candiVal: {}, targetVal: {}, levenRatio: {}, z: {}",
                        targetEntry.getKey(), candidateValue, tarValue, levenRatio, z);
            }

        }

        // range of y: [0, 1]
        double y = 1 / (1 + Math.pow(Math.E, -z));
        log.info("v2 match score: {}, threshold: {}", y, sameDeviceThreshold);

        boolean same = y > sameDeviceThreshold;
        return new DeviceComparisonResult(same, y, null, algVersion);
    }

    private boolean compareTimezone(Map<String, String> candidate, Map<String, String> target) {
        final Integer candidateTzo = getTimeZoneOffset(candidate);
        final Integer targetTzo = getTimeZoneOffset(target);

        boolean tzPass = false;
        if (candidateTzo != null && targetTzo != null) {
            final int tzDiff = Math.abs(candidateTzo - targetTzo);
            tzPass = tzDiff <= timezoneOffsetTolerance;
            log.info("tzDiff: {}, pass: {}", tzDiff, tzPass);
        }
        return tzPass;
    }

    private Integer getTimeZoneOffset(Map<String, String> device) {
        String timezone = device.get(PROP_TIMEZONE);
        String timezoneOffset = "";
        log.info("getTimeZoneOffset.timezone: {}", timezone);
        if (StringUtils.isBlank(timezone)) {
            timezoneOffset = device.get(PROP_TIMEZONE_OFFSET);
            log.info("getTimeZoneOffset.timezoneOffset: {}", timezoneOffset);
        } else {
            String washedTimezone = washTimezone(timezone);
            log.info("washTimezone from {} to {}", timezone, washedTimezone);
            TimeZone tz = TimeZone.getTimeZone(washedTimezone);
            // 毫秒数转分钟
            timezoneOffset = String.valueOf(tz.getRawOffset() / 1000 / 60);
            log.info("getTimeZoneOffset.timezoneOffset: {}", timezoneOffset);
        }
        if (StringUtils.isBlank(timezoneOffset)) {
            return null;
        } else {
            try {
                return Integer.valueOf(timezoneOffset);
            } catch (Exception e) {
                return null;
            }
        }
    }


    public static final Pattern TIME_ZONE_PATTERN = Pattern.compile("(GMT[+-][0-9]*)([\\s\\S]*)");

    /**
     *
     * wash timezone like "GMT-0700 (Pacific Daylight Time)", then generate "GMT-0700"
     *
     * @param timezone
     * @return
     */
    public static String washTimezone(String timezone) {
        Matcher m = TIME_ZONE_PATTERN.matcher(timezone);
        if (m.find()) {
            return m.group(1);
        } else {
            return timezone;
        }
    }

    private boolean comparePlatform(String agentType, Map<String, String> candidate, Map<String, String> target) {
        if (StringUtils.equalsAny(agentType, "web")) {
            final String candiUaStr = candidate.get(PROP_UA);
            final String tarUaStr = target.get(PROP_UA);
            UserAgent candiUa = UserAgent.parseUserAgentString(candiUaStr);
            UserAgent tarUa = UserAgent.parseUserAgentString(tarUaStr);
            log.info("comparePlatform. candiUaStr: {}, tarUaStr: {}, parsed candiPlatform: {}, parsed targetPlatform: {}",
                    candiUa, tarUa,
                    candiUa.getOperatingSystem().name() + "-" + candiUa.getBrowser().getGroup().name(),
                    tarUa.getOperatingSystem().name() + "-" + tarUa.getBrowser().getGroup().name());
            // 去除版本号， 只比较OS和Browser
            return StringUtils.equals(candiUa.getOperatingSystem().name(), tarUa.getOperatingSystem().name()) &&
                    StringUtils.equals(candiUa.getBrowser().getGroup().name(), tarUa.getBrowser().getGroup().name());
        } else if (StringUtils.equalsAny(agentType, "ios", "android")) {
            return false;
        } else {
            return false;
        }
    }


}
