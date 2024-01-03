package at.dcosta.tracks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.combat.SAFContent;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Track;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.TrackStatistic;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.TrackActivity;

public class TrackEdit extends Activity implements OnClickListener {
    public static final String TRACK_ID = "trackId";
    private AutoCompleteTextView name;
    private EditText comment;
    private long dateStart;
    private TrackDbAdapter trackDbAdapter;
    private TrackDescriptionNG trackDescription;
    private ActivityFactory activityFactory;
    private Spinner activitySpinner;

    public static void updateTrack(Context context, TrackDescriptionNG trackDescription, String icon, ActivityFactory activityFactory) {
        Content content = CombatFactory.getFileLocator(context).findTrack(trackDescription.getPath());
        TrackReader reader = TrackReaderFactory.getTrackReader(content, activityFactory.fromIcon(icon).getDistanceValidator());
// xxx       TrackReader reader = TrackReaderFactory.getTrackReader(new SAFContent(context, trackDescription.getPathUri(), trackDescription.getStartTime()), activityFactory.fromIcon(icon).getDistanceValidator());
        updateTrack(trackDescription, reader, icon);
    }

    public void updateTrack(TrackDescriptionNG trackDescription, String icon, ActivityFactory activityFactory) {
        updateTrack(this, trackDescription, icon, activityFactory);
    }

    public static void updateTrack(TrackDescriptionNG trackDescription, TrackReader reader, String icon) {
        TrackStatistic statistic = new TrackStatistic();
        Track track = new Track();
        reader.setListener(statistic, track);
        trackDescription.setSingleValueExtra(TrackDescriptionNG.EXTRA_ICON, icon);
        try {
            reader.readTrack();
            trackDescription.updateStatistic(statistic);
            Configuration.getInstance().getTrackCache().save(trackDescription.getId(), track.getPoints());
        } catch (ParsingException e) {
            e.printStackTrace();
        }
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

    private int getSelectedActivityId(List<TrackActivity> allActivities, TrackActivity activity) {
        if (activity != null) {
            for (int i = 0; i < allActivities.size(); i++) {
                if (allActivities.get(i).equals(activity)) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        TrackActivity activity = (TrackActivity) activitySpinner.getSelectedItem();
        String icon = activity.getIcon();
        updateTrack(icon);
        trackDescription.setName(name.getText().toString());
        String commentString = comment.getText().toString();
        if (commentString.trim().length() > 0) {
            trackDescription.setSingleValueExtra(TrackDescriptionNG.EXTRA_COMMENT, commentString.trim());
        } else {
            trackDescription.setSingleValueExtra(TrackDescriptionNG.EXTRA_COMMENT, null);
        }
        trackDbAdapter.updateEntry(trackDescription);
        Intent intent = new Intent();
        intent.putExtra(TrackList.KEY_DATE, dateStart);
        setResult(TrackList.CONTEXT_EDIT_ID, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_edit);
        trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), this);
        activityFactory = new ActivityFactory(this);

        final Bundle extras = getIntent().getExtras();
        dateStart = extras.getLong(TrackList.KEY_DATE);
        name = findViewById(R.id.track_name);
        comment = findViewById(R.id.track_comment);
        if (extras != null) {
            trackDescription = trackDbAdapter.fetchEntry(extras.getLong(TrackDescriptionNG.KEY_ID));
            name.setText(trackDescription.getName());
            comment.setText(trackDescription.getSingleValueExtra(TrackDescriptionNG.EXTRA_COMMENT, ""));
            Set<String> allTrackNames = trackDbAdapter.getAllTrackNames();
            String[] trackNames = new String[allTrackNames.size()];
            allTrackNames.toArray(trackNames);
            name.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item, trackNames));
            name.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    name.selectAll();
                    return true;
                }
            });
        }
        activitySpinner = (Spinner) findViewById(R.id.activity);
        List<TrackActivity> allActivities = activityFactory.getAllActivities();
        ArrayAdapter<TrackActivity> adapter = new ArrayAdapter<TrackActivity>(this, android.R.layout.simple_spinner_item, createSpinnerItems(allActivities));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(adapter);
        activitySpinner.setSelection(getSelectedActivityId(allActivities, trackDescription.getActivity()));

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

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
    protected void onPause() {
        super.onPause();
        trackDbAdapter.close();
    }

    private void updateTrack(String icon) {
        updateTrack(trackDescription, icon, activityFactory);
    }
}
