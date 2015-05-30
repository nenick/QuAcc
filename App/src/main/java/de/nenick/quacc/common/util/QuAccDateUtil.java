package de.nenick.quacc.common.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class QuAccDateUtil {

    public static final DateTimeFormatter defaultPattern = DateTimeFormat.forPattern("dd.MM.yyyy");

    public static String currentDate() {
        return new DateTime().toString(defaultPattern);
    }

    public static String currentYear() {
        return String.valueOf(new DateTime().year().get());
    }

    public static int currentMonth() {
        return new DateTime().monthOfYear().get();
    }

    public static Date toDate(int day, int month, String year) {
        return toDate(day, month, Integer.parseInt(year));
    }

    public static Date toDate(int day, int month, int year) {
        return toDate(toString(day, month, year));
    }

    public static Date toDate(String dateString) {
        return DateTime.parse(dateString, defaultPattern).toDate();
    }

    public static String toString(int day, int month, int year) {
        return toString(DateTime.parse(day + "." + month + "." + year, defaultPattern));
    }

    public static String toString(DateTime date) {
        return date.toString(defaultPattern);
    }

    public static String toString(Date date) {
        return toString(new DateTime(date));
    }

    public static int monthAfterOf(int month) {
        DateTime dateTime = DateTime.parse("1." + month + ".2015", defaultPattern);
        return dateTime.plusMonths(1).getMonthOfYear();
    }

    public static int monthBeforeOf(int month) {
        DateTime dateTime = DateTime.parse("1." + month + ".2015", defaultPattern);
        return dateTime.minusMonths(1).getMonthOfYear();
    }


}
