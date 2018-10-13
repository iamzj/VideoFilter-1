package com.lyzirving.test.videofilter.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.lyzirving.test.videofilter.ComponentContext;

import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 区域判断的工具类
 */

public class RegionUtil {

    private static String sCountry;
    private static Boolean sIsCn = null;

    /**
     * 取用户国家码，上传服务器的“country”字段使用这个接口！
     * 优先根据sim卡状态判断，若无法获取则取手机语言
     *
     * @return 国家码
     */
    public static String getSimCountry() {
        if (sCountry == null) {
            Context context = ComponentContext.getContext();
            if (null != context) {
                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (null != manager) {
                    boolean ready = TelephonyManager.SIM_STATE_READY == manager.getSimState();
                    String country = manager.getSimCountryIso();
                    if (!TextUtils.isEmpty(country)) {
                        country = country.toLowerCase();
                        Pattern regex = Pattern.compile("^[a-z]{2}$");
                        Matcher matcher = regex.matcher(country);
                        if (matcher.matches()) {
                            sCountry = country;
                            return sCountry;
                        } else {
                            sCountry = Locale.getDefault().getCountry().toLowerCase();
                            return sCountry;
                        }
                    } else {
                        if (ready) {
                            sCountry = Locale.getDefault().getCountry().toLowerCase();
                            return sCountry;
                        }
                    }
                }
            }
            return Locale.getDefault().getCountry().toLowerCase();
        }
        return sCountry;
    }

    /**
     * 获取大写的用户国家码
     *
     * @return
     */
    public static String getUpperCaseSimCountry() {
        String countryISO = getSimCountry();
        if (countryISO != null) {
            countryISO = countryISO.toUpperCase();
        }
        return countryISO;
    }

    /**
     * 根据Sim卡判断是否是中国用户，如果没有Sim卡，则根据手机语言及设定的时区判断
     *
     * @return
     */
    public static boolean isCnUser() {
        if (sIsCn != null) {
            return sIsCn;
        }

        // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
        Context context = ComponentContext.getContext();
        if (null != context) {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != manager) {
                String oper = manager.getSimOperator();
                if (!TextUtils.isEmpty(oper)) {
                    sIsCn = oper.startsWith("460");
                    return sIsCn;
                }
                String country = manager.getSimCountryIso();
                if (!TextUtils.isEmpty(country) && country.length() == 2) {
                    sIsCn = "cn".equals(country);
                    return sIsCn;
                }
            }
        }
        // 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
        // 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
        String country = Locale.getDefault().getCountry().toLowerCase();
        if (!TextUtils.isEmpty(country) && "cn".equals(country)) {
            // 如果获取的国家信息是CN，则返回TRUE
            // 增加时区的判断，若语言是CN且时区为中国，才认为是中国用户
            String zone = TimeZone.getDefault().getDisplayName(Locale.ENGLISH);
            if (zone != null && zone.toUpperCase().contains("CHINA")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取系统的本地信息
     *
     * @return
     */
    public static Locale getLocale() {
        Locale locale = null;
        try {
            locale = ComponentContext.getContext().getResources().getConfiguration().locale;
        } catch (Exception e) {
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * 获取系统的“语言-国家”字符串
     *
     * @return
     */
    public static String getLocalString() {
        Locale locale = getLocale();
        String language = locale.getLanguage();
        if (language != null) {
            language = language.toLowerCase();
        }
        String country = locale.getCountry();
        if (country != null) {
            country = country.toLowerCase();
        }
        return String.format(Locale.US, "%s-%s", language, country);
    }

    /**
     * 获取系统的语言
     *
     * @return
     */
    public static String getLanguage() {
        Locale locale = getLocale();
        String language = locale.getLanguage();
        if (language != null) {
            language = language.toLowerCase();
        }
        return language;
    }

    /**
     * 获取系统的国家
     *
     * @return
     */
    public static String getCountry() {
        Locale locale = getLocale();
        String country = locale.getCountry();
        if (country != null) {
            country = country.toUpperCase();
        }
        return country;
    }

    /**
     * 是否是中文语言
     *
     * @return
     */
    public static boolean isZhLanguage() {
        return "zh".equals(getLanguage());
    }

    public static void openUSTest() {
        sCountry = "us";
        sIsCn = false;
    }

    public static void closeUSTest() {
        sCountry = null;
        sIsCn = null;
    }
}
