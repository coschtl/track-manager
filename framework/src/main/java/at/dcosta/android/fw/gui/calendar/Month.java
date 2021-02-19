package at.dcosta.android.fw.gui.calendar;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Month<T> {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat MONTH_NAME = new SimpleDateFormat("MMMM");

	private final int month, year;
	private final SparseArray<SparseArray<Day<T>>> days;

	private final Calendar calendar;

	public Month(Calendar calendar) {
		this.calendar = calendar;
		month = calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
		days = new SparseArray<SparseArray<Day<T>>>();
		init();
	}

	public Month(int month, int year) {
		this.month = month;
		this.year = year;
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		days = new SparseArray<SparseArray<Day<T>>>();
		init();
	}

	private void createNewRow(int rowNum) {
		SparseArray<Day<T>> row = new SparseArray<Day<T>>();
		for (int i = 0; i < 7; i++) {
			row.put(i, new Day<T>(0, this));
		}
		days.put(rowNum, row);
	}

	private int getColumn(int dayOfWeek) {
		switch (dayOfWeek) {
			case Calendar.MONDAY:
				return 0;
			case Calendar.TUESDAY:
				return 1;
			case Calendar.WEDNESDAY:
				return 2;
			case Calendar.THURSDAY:
				return 3;
			case Calendar.FRIDAY:
				return 4;
			case Calendar.SATURDAY:
				return 5;
			case Calendar.SUNDAY:
				return 6;
		}
		throw new IllegalArgumentException("Unknown day of week: " + dayOfWeek);
	}

	public Day<T> getDay(int index) {
		int row = 0;
		while (index > 6) {
			row++;
			index -= 7;
		}
		return getRow(row).get(index);
	}

	public Day<T> getDay(int row, int column) {
		return getRow(row).get(column);
	}

	public int getMonth() {
		return month;
	}

	public String getName() {
		return new StringBuilder().append(MONTH_NAME.format(calendar.getTime())).append(" ").append(year).toString();
	}

	public int getNumberOfDays() {
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	private SparseArray<Day<T>> getRow(int rowNum) {
		return days.get(rowNum);
	}

	public int getRowCount() {
		return days.size();
	}

	public int getYear() {
		return year;
	}

	private void init() {
		int rowNum = 0;
		createNewRow(rowNum);

		for (int i = 1; i <= getNumberOfDays(); i++) {
			calendar.set(Calendar.DAY_OF_MONTH, i);
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			int column = getColumn(dayOfWeek);
			Day<T> day = new Day<T>(i, this);
			day.setWeekend(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
			getRow(rowNum).put(column, day);
			if (column == 6 && i < getNumberOfDays()) {
				createNewRow(++rowNum);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("-------------------- ").append(getMonth()).append(" / ").append(getYear()).append(" -------------------\n");
		for (int i = 0; i < days.size(); i++) {
			SparseArray<Day<T>> row = getRow(i);
			for (int j = 0; j < row.size(); j++) {
				Day<T> d = row.get(j);
				b.append(d == null ? " " : d.toString()).append("\t");
			}
			b.append("\n");
		}
		return b.toString();
	}
}
