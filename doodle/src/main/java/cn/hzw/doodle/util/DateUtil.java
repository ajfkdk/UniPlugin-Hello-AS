package cn.hzw.doodle.util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateUtil {
    public static final int DAY = 86400000;
    public static final int HOUR = 3600000;
    public static final int MIN = 60000;

    public DateUtil() {
    }

    public static int getMonthDays(int year, int month) {
        if (month > 12) {
            month = 1;
            ++year;
        } else if (month < 1) {
            month = 12;
            --year;
        }

        int[] arr = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int days = 0;
        if (isLeapYear(year)) {
            arr[1] = 29;
        }

        try {
            days = arr[month - 1];
        } catch (Exception var5) {
            var5.getStackTrace();
        }

        return days;
    }

    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    public static List<String> getMonthDaysArray(int year, int month) {
        List<String> dayList = new ArrayList();
        int days = getMonthDays(year, month);

        for (int i = 1; i <= days; ++i) {
            dayList.add(i + "");
        }

        return dayList;
    }

    public static int getYear() {
        return Calendar.getInstance().get(1);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(2) + 1;
    }

    public static int getCurrentMonthDay() {
        return Calendar.getInstance().get(5);
    }

    public static int getHour() {
        return Calendar.getInstance().get(11);
    }

    public static int getMinute() {
        return Calendar.getInstance().get(12);
    }

    public static int getSecond() {
        return Calendar.getInstance().get(13);
    }

    public static int getMillSecond() {
        return Calendar.getInstance().get(14);
    }

    public static long getDaySpan(long time) {
        return getTimeSpan(time, 86400000L);
    }

    public static long getHourSpan(long time) {
        return getTimeSpan(time, 3600000L);
    }

    public static long getMinSpan(long time) {
        return getTimeSpan(time, 60000L);
    }

    public static long getTimeSpan(long time, long span) {
        int tiemzone = TimeZone.getDefault().getRawOffset();
        return (System.currentTimeMillis() + (long) tiemzone) / span - (time + (long) tiemzone) / span;
    }

    public static boolean isToday(long time) {
        return getDaySpan(time) == 0L;
    }

    public static boolean isYestoday(long time) {
        return getDaySpan(time) == 1L;
    }

    public static boolean isTomorrow(long time) {
        return getDaySpan(time) == -1L;
    }

    public static String getDate() {
        return getDate("yyyy-MM-dd HH-mm-ss");
    }

    public static String getDate(String format) {
        return getDate((new Date()).getTime(), format);
    }

    public static String getDate(long time, String format) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(format);
        String date = sDateFormat.format(time);
        return date;
    }
}
