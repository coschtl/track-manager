package at.dcosta.tracks;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.SAFContent;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.track.file.TmgrReader;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.SimpleDatePickerDialog;
import at.dcosta.tracks.util.TrackIO;

public class TrackCopy extends Activity implements OnClickListener {

    public static final String TRACK_ID = "trackId";
    private static final int DATE_DIALOG_ID = 1;
    private static final int TIME_DIALOG_ID = 2;
    private TrackDbAdapter trackDbAdapter;
    private AutoCompleteTextView name;
    private EditText date, time;
    private TrackDescriptionNG origTrack;

    public static void copyTrack(Context context, TrackDescriptionNG origTrack, List<Point> trackOrig, String newName, long timeDiffMillis, TrackDbAdapter trackDbAdapter) {

        File to = new File(Configuration.getInstance().getCopiedTracksDir(), IOUtil.getFilenameNoPath(origTrack.getPathNoHash()) + TmgrReader.SUFFIX);
        long copyId = trackDbAdapter.copyEntry(origTrack, newName, to.getAbsolutePath(), timeDiffMillis);
        List<Point> trackCopy = new ArrayList<Point>(trackOrig.size());
        for (Point p : trackOrig) {
            trackCopy.add(p.shift(timeDiffMillis));
        }
        TrackIO.writeTmgrTrack(to, trackCopy);
        TrackDescriptionNG copyEntry = trackDbAdapter.fetchEntry(copyId);
        if (copyEntry.getActivity() != null) {
            TrackEdit.updateTrack(context, copyEntry, copyEntry.getActivity().getIcon(), trackDbAdapter.getActivityFactory());
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

    @Override
    public void onClick(View v) {
        Date startDate;
        try {
            startDate = DateUtil.DATE_TIME_FORMAT_NUMERIC_LONG.parse(new StringBuilder(date.getText()).append(" ").append(time.getText()).append(":00")
                    .toString());
            long timeDiffMillis = startDate.getTime() - origTrack.getStartTime().getTime();
            Content origContent = CombatFactory.getFileLocator(this).findTrack(origTrack.getPath());
            copyTrack(this, origTrack, TrackIO.loadTrack(origContent), name.getText().toString(), timeDiffMillis, trackDbAdapter);
// xxx           copyTrack(this, origTrack, TrackIO.loadTrack(new SAFContent(this, origTrack.getPathUri(), origTrack.getStartTime())), name.getText().toString(), timeDiffMillis, trackDbAdapter);
            setResult(TrackList.CONTEXT_EDIT_ID, getIntent());
            finish();
        } catch (ParseException | ParsingException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_copy);
        trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), this);

        final Bundle extras = getIntent().getExtras();
        origTrack = trackDbAdapter.fetchEntry(extras.getLong(TrackDescriptionNG.KEY_ID));

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

        name = (AutoCompleteTextView) findViewById(R.id.name);
        Set<String> allTrackNames = trackDbAdapter.getAllTrackNames();
        String[] trackNames = new String[allTrackNames.size()];
        allTrackNames.toArray(trackNames);
        name.setText(origTrack.getName());
        name.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item, trackNames));

        date = (EditText) findViewById(R.id.date);
        addOnClickListener(date, DATE_DIALOG_ID);

        time = (EditText) findViewById(R.id.time);
        addOnClickListener(time, TIME_DIALOG_ID);

        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtras(extras);
                setResult(TrackList.CONTEXT_EDIT_ID, intent);
                finish();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new SimpleDatePickerDialog(this, date);
            case TIME_DIALOG_ID:
                final Calendar c = Calendar.getInstance();
                c.setTime(origTrack.getStartTime());
                return new MyTimePickerDialog(this, time, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        trackDbAdapter.close();
    }

    private static class MyTimePickerDialog extends TimePickerDialog {

        public MyTimePickerDialog(Context context, final EditText text, int hour, int minute) {
            super(context, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Date date = DateUtil.getDate(1, 1, 2000, hourOfDay, minute, 0, 0);
                    text.setText(DateUtil.TIME_FORMAT_SHORT.format(date));

                }
            }, hour, minute, true);
        }
    }
}
