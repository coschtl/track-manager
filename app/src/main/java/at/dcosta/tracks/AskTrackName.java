package at.dcosta.tracks;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.util.Configuration;

public class AskTrackName extends AlertDialog implements OnItemSelectedListener, TextWatcher {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

	private String trackName;
	private Spinner existingTracks;
	private EditText newTrack;
	private ArrayAdapter<String> adapter;
	private android.view.View.OnClickListener onClickListener;

	public AskTrackName(Context context) {
		super(context);
	}

	@Override
	public void afterTextChanged(Editable s) {
		trackName = newTrack.getText().toString();
		if (trackName != null && !"".equals(trackName) && existingTracks != null) {
			existingTracks.setSelection(0);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// not needed
	}

	public String getTrackName() {
		return trackName;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_track_name);
		setCancelable(true);

		newTrack = (EditText) findViewById(R.id.track_name);
		newTrack.addTextChangedListener(this);
		setDefaultTrackname();

		TrackDbAdapter trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), getContext());
		Set<String> names = trackDbAdapter.getAllTrackNames();
		trackDbAdapter.close();
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
		adapter.add(getContext().getString(R.string.select_to_resume));
		for (String s : names) {
			adapter.add(s);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		existingTracks = (Spinner) findViewById(R.id.track_select);
		existingTracks.setAdapter(adapter);
		existingTracks.setOnItemSelectedListener(this);

		Button cancel = (Button) findViewById(R.id.cancel);
		System.out.println(cancel);
		cancel.setOnClickListener(onClickListener);
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(onClickListener);

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (position != 0) {
			// trackName =
			System.out.println(adapter.getItem(position));
			newTrack.setText("");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// not needed
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// not needed
	}

	private void setDefaultTrackname() {
		newTrack.setText(DATE_FORMAT.format(new Date()));
	}

	public void setOnClickListener(android.view.View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	@Override
	public void show() {
		trackName = null;
		if (newTrack != null) {
			setDefaultTrackname();
		}
		super.show();
	}
}
