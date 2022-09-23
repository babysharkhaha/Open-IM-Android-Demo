package io.openim.android.ouicore.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;

public class TimeUtil {

    public static String getTimeString(Long timestamp) {
        String result = "";
        String weekNames[] = {BaseApp.instance().getString(R.string.sunday),
                BaseApp.instance().getString(R.string.monday),
                BaseApp.instance().getString(R.string.tuesday),
                BaseApp.instance().getString(R.string.wednesday),
                BaseApp.instance().getString(R.string.thursday),
                BaseApp.instance().getString(R.string.friday),
                BaseApp.instance().getString(R.string.saturday)};
        String hourTimeFormat = "HH:mm";
        String monthTimeFormat = "M/d HH:mm";
        String yearTimeFormat = "yyyy/M/d HH:mm";
        try {
            Calendar todayCalendar = Calendar.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);

            if (todayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {//当年
                if (todayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {//当月
                    int temp = todayCalendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH);
                    switch (temp) {
                        case 0://今天
                            result = getTime(timestamp, hourTimeFormat);
                            break;
                        case 1://昨天
                            result = "昨天 " + getTime(timestamp, hourTimeFormat);
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                            result = weekNames[dayOfWeek - 1] + " " + getTime(timestamp, hourTimeFormat);
                            break;
                        default:
                            result = getTime(timestamp, monthTimeFormat);
                            break;
                    }
                } else {
                    result = getTime(timestamp, monthTimeFormat);
                }
            } else {
                result = getTime(timestamp, yearTimeFormat);
            }
            return result;
        } catch (Exception e) {
            L.e("getTimeString", e.getMessage());
            return "";
        }
    }

    public static String getTime(long time, String pattern) {
        Date date = new Date(time);
        return dateFormat(date, pattern);
    }

    public static String dateFormat(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }


}
