package at.dcosta.android.fw.gui.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.dcosta.android.fw.DateUtil;

public class Day<T> {

    private final int day;
    private final Month<T> month;
    private final List<T> content;
    private boolean weekend;

    public Day(int day, Month<T> month) {
        this.day = day;
        this.month = month;
        this.content = new ArrayList<T>();
    }

    public void addContent(T content) {
        this.content.add(content);
    }

    public List<T> getContent() {
        return content;
    }

    public Date getDate() {
        return DateUtil.getDate(day, month.getMonth(), month.getYear(), 0, 0, 0, 0);
    }

    public int getDay() {
        return day;
    }

    public String getDayAsString() {
        if (day <= 0) {
            return "";
        }
        return Integer.toString(day);
    }

    public Month<T> getMonth() {
        return month;
    }

    public boolean hasContent() {
        return content.size() > 0 && !isDummy();
    }

    public boolean isDummy() {
        return day <= 0;
    }

    public boolean isWeekend() {
        return weekend && !isDummy();
    }

    public void setWeekend(boolean weekend) {
        this.weekend = weekend;
    }

    @Override
    public String toString() {
        if (isDummy()) {
            return "";
        }
        return new StringBuilder().append(day).append(".").append(month.getMonth()).append(".").append(month.getYear()).toString();
    }

}