package at.dcosta.tracks;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import at.dcosta.tracks.db.SavedSearchesDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.SavedSearch;
import at.dcosta.tracks.util.TrackActivity;

public class TrackSearch extends Activity implements OnClickListener {

	private static class SearchDatePickerDialog extends DatePickerDialog {
		private static int year, month, day;
		static {
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
		}

		public SearchDatePickerDialog(Context context, final EditText text) {
			super(context, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					text.setText(toStringForDatePicker(year, monthOfYear, dayOfMonth));
				}
			}, year, month, day);
		}
	}

	public static final String TRACK_NAME = "trackName";

	public static final String ACTIVITY = "activity";

	public static final String DATE_FROM = "dateFrom";
	public static final String DATE_TO = "dateTo";
	private static final int DATE_FROM_DIALOG_ID = 1;
	private static final int DATE_TO_DIALOG_ID = 2;

	static String toStringForDatePicker(int year, int monthOfYear, int dayOfMonth) {
		return new StringBuilder().append(dayOfMonth).append(".").append(monthOfYear + 1).append(".").append(year).toString();
	}

	private AutoCompleteTextView trackName;
	private ImageButton deleteButton;
	private EditText dateFrom, dateTo, alias;
	private Spinner activitySpinner;
	private final Configuration config = Configuration.getInstance(this);
	private SavedSearchesDbAdapter savedSearchesDbAdapter;
	private List<String> savedSearches;
	private Menu menu;

	private void addDateExtra(Intent intent, String name, EditText editText) {
		Date date = toDate(editText);
		if (date != null) {
			intent.putExtra(name, date);
		}
	}

	private void addExtra(Intent intent, String name, String value) {
		if (value != null && !"".equals(value.trim())) {
			intent.putExtra(name, value);
		}
	}

	private void addOnClickListener(EditText et, final int dialogId) {
		et.setInputType(InputType.TYPE_NULL);
		et.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(dialogId);
				}
			}
		});

		et.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.hasFocus()) {
					showDialog(dialogId);
				}

			}
		});
	}

	private TrackActivity[] createSpinnerItems(Collection<TrackActivity> allActivities) {
		TrackActivity[] acts = new TrackActivity[allActivities.size() + 1];
		acts[0] = TrackActivity.SELECT;
		int i = 1;
		for (TrackActivity ta : allActivities) {
			acts[i++] = ta;
		}
		return acts;
	}

	private String getValue(EditText editText) {
		return editText.getText().toString();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, new Intent());
		finish();
	}

	@Override
	public void onClick(View v) {
		String name = getValue(trackName);
		TrackActivity selectedItem = (TrackActivity) activitySpinner.getSelectedItem();
		String activityIcon = selectedItem.getIcon();
		String aliasValue = getValue(alias);
		if (aliasValue.length() > 0) {
			SavedSearch savedSearch = new SavedSearch(aliasValue, name, activityIcon, toDate(dateFrom), toDate(dateTo));
			savedSearchesDbAdapter.add(savedSearch);
		}
		Intent intent = new Intent();
		addExtra(intent, TRACK_NAME, name);
		addExtra(intent, ACTIVITY, activityIcon);
		addDateExtra(intent, DATE_FROM, dateFrom);
		addDateExtra(intent, DATE_TO, dateTo);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_tracks);

		TrackDbAdapter trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), this);
		savedSearchesDbAdapter = new SavedSearchesDbAdapter(config.getDatabaseHelper(), this);
		savedSearches = savedSearchesDbAdapter.fetchAllAliases();

		trackName = (AutoCompleteTextView) findViewById(R.id.track_name);
		Set<String> allTrackNames = trackDbAdapter.getAllTrackNames();
		String[] trackNames = new String[allTrackNames.size()];
		allTrackNames.toArray(trackNames);
		trackName.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item, trackNames));
		trackDbAdapter.close();

		Collection<TrackActivity> allActivities = new ActivityFactory(this).getAllActivities();
		activitySpinner = (Spinner) findViewById(R.id.activity);
		ArrayAdapter<TrackActivity> adapter = new ArrayAdapter<TrackActivity>(this, android.R.layout.simple_spinner_item, createSpinnerItems(allActivities));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		activitySpinner.setAdapter(adapter);

		dateFrom = (EditText) findViewById(R.id.dateFrom);
		addOnClickListener(dateFrom, DATE_FROM_DIALOG_ID);

		dateTo = (EditText) findViewById(R.id.dateTo);
		addOnClickListener(dateTo, DATE_TO_DIALOG_ID);

		Button search = (Button) findViewById(R.id.search);
		search.setOnClickListener(this);

		alias = (EditText) findViewById(R.id.alias);
		deleteButton = (ImageButton) findViewById(R.id.but_delete);
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String aliasText = alias.getText().toString();
				if ("".equals(valueNotNull(aliasText))) {
					return;
				}
				savedSearchesDbAdapter.deleteEntry(aliasText);
				savedSearches = savedSearchesDbAdapter.fetchAllAliases();
				menu.clear();
				onCreateOptionsMenu(menu);
				setValues(null, null, null, null, null);
			}
		});
		deleteButton.setEnabled(false);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DATE_FROM_DIALOG_ID:
				return new SearchDatePickerDialog(this, dateFrom);
			case DATE_TO_DIALOG_ID:
				return new SearchDatePickerDialog(this, dateTo);
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		int i = 0;
		for (String alias : savedSearches) {
			menu.add(0, i, i++, alias);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SavedSearch savedSearch = savedSearchesDbAdapter.findEntry(savedSearches.get(item.getItemId()));
		if (savedSearch != null) {
			setValues(savedSearch.getName(), savedSearch.getActivity(), savedSearch.getDateStart(), savedSearch.getDateEnd(), savedSearch.getAlias());
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		savedSearchesDbAdapter.close();
		super.onSaveInstanceState(outState);
	}

	private void setValues(String trackName, String activityItem, Date start, Date end, String searchAlias) {
		this.trackName.setText(valueNotNull(trackName));
		if (activityItem != null) {
			for (int i = 0; i < activitySpinner.getCount(); i++) {
				TrackActivity itemAtPosition = (TrackActivity) activitySpinner.getItemAtPosition(i);
				if (activityItem.equals(itemAtPosition.getIcon())) {
					activitySpinner.setSelection(i);
					break;
				}
			}
		} else {
			activitySpinner.setSelection(0);
		}
		dateFrom.setText(toStringForDatePicker(start));
		dateTo.setText(toStringForDatePicker(end));
		if (searchAlias == null || "".equals(searchAlias)) {
			alias.setText("");
			deleteButton.setEnabled(false);
		} else {
			alias.setText(searchAlias);
			deleteButton.setEnabled(true);
		}
	}

	private Date toDate(EditText editText) {
		String value = getValue(editText);
		if (value != null && !"".equals(value.trim())) {
			try {
				return config.getDateFormat().parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private String toStringForDatePicker(Date date) {
		if (date == null) {
			return "";
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return toStringForDatePicker(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	private String valueNotNull(String value) {
		if (value == null) {
			return "";
		}
		return value;
	}

}
