
package me.corningrey.camunda.api.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.UnitedLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 通用util
 */
public class CommonUtil {

    /**
     * 日期格式化(年)
     */
    public final static FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy");
    private static final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
    private static Matcher matcher;

    /**
     * 比较两个时间的年份
     */
    public static int compareYear(String dateString1, String dateString2) throws UnitedException {
        if (StringUtils.isBlank(dateString1) || StringUtils.isBlank(dateString2)) {
            throw new UnitedException("I18N-MSG-999900000010");
        }
        try {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(dateFormat.parse(dateString1));
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(dateFormat.parse(dateString2));
            return calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
        } catch (ParseException e) {
            throw new UnitedException("I18N-MSG-999900000020");
        }
    }

    /**
     * 比较两个时间的年份
     */
    public static int compareYear(Date date1, Date date2) throws UnitedException {
        if (date1 == null || date2 == null) {
            throw new UnitedException("I18N-MSG-999900000030");
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
    }

    /**
     * 获取日期的年份
     *
     * @param dateString
     * @return
     * @throws UnitedException
     */
    public static int getYear(String dateString) throws UnitedException {
        if (StringUtils.isBlank(dateString)) {
            throw new UnitedException("I18N-MSG-999900000010");
        }
        try {
            return getYear(dateFormat.parse(dateString));
        } catch (ParseException e) {
            throw new UnitedException("I18N-MSG-999900000020");
        }
    }

    /**
     * 获取日期的年份
     *
     * @param date
     * @return
     * @throws UnitedException
     */
    public static int getYear(Date date) throws UnitedException {
        try {
            date = dateFormat.parse(dateFormat.format(date));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            throw new UnitedException("I18N-MSG-999900000020");
        }
    }

    /**
     * 计算两个日期(字符串)之间相差的天数
     *
     * @param smdateString 较小的时间
     * @param bdateString  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(String smdateString, String bdateString) throws Exception {
        if (StringUtils.isBlank(smdateString) || StringUtils.isBlank(bdateString)) {
            throw new UnitedException("I18N-MSG-999900000010");
        }
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date smdate = sdf.parse(smdateString);
        Date bdate = sdf.parse(bdateString);
        return daysBetween(smdate, bdate);
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws Exception {
        if (smdate == null || bdate == null) {
            throw new UnitedException("I18N-MSG-999900000030");
        }
        FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }


    /**
     * 格式化字符串 字符串中使用{key}表示占位符
     *
     * @param sourStr 需要匹配的字符串
     * @param param   参数集
     * @return
     */
    public static String stringFormat(String sourStr, Map<String, Object> param) {
        String tagerStr = sourStr;
        if (param == null) {
            return tagerStr;
        }
        matcher = pattern.matcher(tagerStr);
        while (matcher.find()) {
            String key = matcher.group();
            String keyclone = key.substring(1, key.length() - 1).trim();
            Object value = param.get(keyclone);
            if (value != null) {
                tagerStr = tagerStr.replace(key, value.toString());
            }
        }
        return tagerStr;
    }

    /**
     * 格式化字符串 字符串中使用{key}表示占位符 利用反射 自动获取对象属性值 (必须有get方法)
     *
     * @param sourStr 需要匹配的字符串
     * @param obj     参数集
     * @return
     */
    public static String stringFormat(String sourStr, Object obj) {
        String tagerStr = sourStr;
        matcher = pattern.matcher(tagerStr);
        if (obj == null) {
            return tagerStr;
        }

        PropertyDescriptor pd;
        Method getMethod;
        // 匹配{}中间的内容 包括括号
        while (matcher.find()) {
            String key = matcher.group();
            String keyclone = key.substring(1, key.length() - 1).trim();
            try {
                pd = new PropertyDescriptor(keyclone, obj.getClass());
                // 获得get方法
                getMethod = pd.getReadMethod();
                Object value = getMethod.invoke(obj);
                if (value != null)
                    tagerStr = tagerStr.replace(key, value.toString());
            } catch (Exception e) {
                UnitedLogger.error(e);
            }
        }
        return tagerStr;
    }

    /**
     * 判断字符串是否为json字符串
     *
     * @param str
     * @return
     */
    public static boolean isJSONValid(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            JSONObject.parseObject(str);
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}