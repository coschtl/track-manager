package at.dcosta.android.fw.props.gui;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.R;

public class DatePropertyEditor extends PropertyEditor {

	private DatePicker date;

	@Override
	protected boolean isValid() {
		value.setText(date.getDayOfMonth() + "." + (1 + date.getMonth()) + "." + date.getYear());
		return true;
	}

	@Override
	protected void onAddPropertyCall(Intent intent) {
		// nothing to do
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Date");
		super.onCreate(savedInstanceState);
		date = (DatePicker) findViewById(R.id.property_date);
		if (getValue() != null && getValue().length() > 0) {
			Date d;
			try {
				d = DateUtil.DATE_FORMAT_NUMERIC.parse(getValue());
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				date.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
			} catch (ParseException e) {
				// ignore -> now() is set
			}
		}
		date.setVisibility(View.VISIBLE);
	}

}
