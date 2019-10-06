package jan.schuettken.bierpongleague.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Jan Schüttken on 20.12.2017 at 21:10
 */

public class DateFunctionHandler {

    public static int getThisYear() {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return Integer.parseInt(dateFormat.format(date));
    }

    public static int getThisMonth() {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        return Integer.parseInt(dateFormat.format(date));
    }

    public static int getMonth(int thisMonth, int offset) {
        thisMonth += offset;
        if (thisMonth < 1)
            thisMonth += 12;
        if (thisMonth > 12)
            thisMonth = thisMonth % 12;
        if (thisMonth == 0)
            thisMonth = 12;
        return thisMonth;
    }

    public static String getThisMonthTwoDigs() {
        String response = getThisMonth() + "";
        if (response.length() < 2)
            response = "0" + response;
        return response;
    }

    public static String getThisDayTwoDigs() {
        String response = getThisDay() + "";
        if (response.length() < 2)
            response = "0" + response;
        return response;
    }

    public static int getThisDay() {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("dd");
        Date date = new Date();
        return Integer.parseInt(dateFormat.format(date));
    }

    public static String getTwoDigs(int number) {
        return getDigs(number, 2);
    }

    public static String getDigs(int number, int digs) {
        String response = number + "";
        if (response.length() < digs)
            response = "0" + response;
        return response;
    }

    public static long timePassedSince(long timeStamp) {
        long now = System.currentTimeMillis();
        return now - timeStamp;
    }

    public static String convertDateToTimestamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static Date convertTimestampToDate(String date) {
        Calendar calendar = Calendar.getInstance();
        String[] x = date.split(" ");
        calendar.set(Calendar.YEAR, Integer.parseInt(x[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(x[1]));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(x[2]));
        return calendar.getTime();
    }

    public static String convertDateToTimestamp(int year, int month, int day) {
        return year + "-" + getTwoDigs(month) + "-" + getTwoDigs(day) + " 00:00:00";
    }

    public static String getToDigs(double value) {
        String s = "" + value;
        s = s.replaceAll(Pattern.quote("."), ",");
        if (s.split(",").length > 1) {
            if (s.split(",")[1].length() < 2) {
                s = s + "0";
            }
            return s;
        } else
            return s + ",00";
    }
}
