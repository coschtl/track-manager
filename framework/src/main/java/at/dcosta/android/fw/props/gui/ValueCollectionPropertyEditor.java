package at.dcosta.android.fw.props.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.NameValuePair;
import at.dcosta.android.fw.R;

public class ValueCollectionPropertyEditor extends PropertyEditor implements OnItemSelectedListener {

	private Spinner spinner;
	private Map<String, String> nameToValue;
	private List<String> names;

	@Override
	protected boolean isValid() {
		return true;
	}

	@Override
	protected void onAddPropertyCall(Intent intent) {
		// nothing to do
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spinner = (Spinner) findViewById(R.id.property_spinner);
		nameToValue = new HashMap<String, String>();
		for (NameValuePair nvp : property.getPossibleValues()) {
			nameToValue.put(nvp.getName(), nvp.getValue());
		}
		names = new ArrayList<String>();
		names.addAll(nameToValue.keySet());
		Collections.sort(names);
		SpinnerAdapter sa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
		spinner.setAdapter(sa);
		spinner.setVisibility(View.VISIBLE);
		int selection = 0;
		for (String s : names) {
			if (nameToValue.get(s).equals(property.getValue())) {
				break;
			}
			selection++;
		}
		if (selection < spinner.getCount()) {
			spinner.setSelection(selection);
		}
		spinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String displayname = names.get(position);
		value.setText(nameToValue.get(displayname));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// not needed
	}

}
