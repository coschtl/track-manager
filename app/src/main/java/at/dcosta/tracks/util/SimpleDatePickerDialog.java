package at.dcosta.tracks.util;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;

import at.dcosta.android.fw.DateUtil;

public class SimpleDatePickerDialog extends DatePickerDialog {
    private static final int year;
    private static final int month;
    private static final int day;

    static {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    public SimpleDatePickerDialog(Context context, final EditText text) {
        super(context, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Date date = DateUtil.getDay(dayOfMonth, monthOfYear, year);
                text.setText(DateUtil.DATE_FORMAT_NUMERIC.format(date));
            }
        }, year, month, day);
        getDatePicker().setSpinnersShown(true);
        getDatePicker().setCalendarViewShown(false);
    }
}
