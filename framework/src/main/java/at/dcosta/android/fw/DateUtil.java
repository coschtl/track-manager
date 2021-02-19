package at.dcosta.android.fw;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final long SECOND_MILLIS = 1000l;
    public static final long MINUTE_MILLIS = SECOND_MILLIS * 60l;
    public static final long HOUR_MILLIS = MINUTE_MILLIS * 60l;
    public static final long DAY_MILLIS = HOUR_MILLIS * 24l;
    public static final long YEAR_MILLIS = DAY_MILLIS * 365l;
    public static final SimpleDateFormat DATE_FORMAT_NUMERIC = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat TIME_FORMAT_SHORT = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat TIME_FORMAT_MEDIUM = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMAT_NUMERIC_LONG = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMAT_NUMERIC_SHORT = new SimpleDateFormat("dd.MM.yy HH:mm");

    private static final void addText(long value, String s, String plural, StringBuilder b) {
        if (b.length() > 0) {
            b.append(", ");
        }
        b.append(value).append(" ").append(s);
        if (value > 1) {
            b.append(plural);
        }
    }

    public static final String durationMillisToString(long duration) {
        StringBuilder b = new StringBuilder();
        duration = process(duration, YEAR_MILLIS, "Jahr", "e", b);
        duration = process(duration, DAY_MILLIS, "Tag", "e", b);
        duration = process(duration, HOUR_MILLIS, "Stunde", "n", b);
        duration = process(duration, MINUTE_MILLIS, "Minute", "n", b);
        duration = process(duration, SECOND_MILLIS, "Sekunde", "n", b);
        if (duration > 0) {
            addText(duration, "Millisekunde", "n", b);
        }
        return b.toString();
    }

    public static final String durationSecondsToString(long duration) {
        return durationMillisToString(duration * 1000l);
    }

    public static final String formatDateRange(Date start, Date end, Format format) {
        DateFormat df;
        switch (format) {
            case SHORT:
                df = DateFormat.getDateInstance(DateFormat.SHORT);
                break;
            case MEDIUM:
                df = DateFormat.getDateInstance(DateFormat.MEDIUM);
                break;
            case LONG:
                df = DateFormat.getDateInstance(DateFormat.LONG);
                break;
            case FULL:
                df = DateFormat.getDateInstance(DateFormat.FULL);
                break;
            default:
                df = DateFormat.getDateInstance(DateFormat.DEFAULT);
                break;
        }
        return new StringBuilder().append(df.format(start)).append(" - ").append(df.format(end)).toString();
    }

    private static Calendar getCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public static final Date getDate(int day, int month, int year, int hour, int min, int sec, int millis) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, sec);
        c.set(Calendar.MILLISECOND, millis);
        return c.getTime();
    }

    public static final Date getDay(Date date) {
        Calendar c = getCalendar(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static final Date getDay(int day, int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static final Date getDayEnd(Date date) {
        Calendar c = getCalendar(date);
        setDayEnd(c);
        return c.getTime();
    }

    public static final Date getDayStart(Date date) {
        Calendar c = getCalendar(date);
        return c.getTime();
    }

    public static final Date getEndOfMonth(Date d) {
        Calendar c = getCalendar(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        setDayEnd(c);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static final Date getEndOfMonth(int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        setDayEnd(c);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }

    public static final Date getEndOfWeek(Date d) {
        Calendar c = getCalendar(d);
        setDayEnd(c);
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return c.getTime();
    }

    public static final Date getEndOfYear(Date d) {
        Calendar c = getCalendar(d);
        setDayEnd(c);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
        return c.getTime();
    }

    public static long getEpochSecs(Date date) {
        if (date == null) {
            return 0;
        }
        return date.getTime() / 1000l;
    }

    public static final Date getStartOfMonth(Date d) {
        Calendar c = getCalendar(d);
        c.set(Calendar.DAY_OF_MONTH, 1);
        setDayStart(c);
        return c.getTime();
    }

    public static final Date getStartOfMonth(int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        setDayStart(c);
        return c.getTime();
    }

    public static final Date getStartOfWeek(Date d) {
        Calendar c = getCalendar(d);
        setDayStart(c);
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        return c.getTime();
    }

    public static final Date getStartOfYear(Date d) {
        Calendar c = getCalendar(d);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DAY_OF_MONTH, 1);
        setDayStart(c);
        return c.getTime();
    }

    private static final long process(long duration, long constant, String stringForConstant, String stringForPlural, StringBuilder b) {
        if (duration >= constant) {
            long result = duration / constant;
            long modulo = duration % constant;
            addText(result, stringForConstant, stringForPlural, b);
            return modulo;
        }
        return duration;
    }

    private static void setDayEnd(Calendar c) {
        setDayStart(c);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MILLISECOND, -1);
    }

    private static final void setDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public enum Format {
        SHORT, MEDIUM, LONG, FULL, DEFAULT
    }

}
