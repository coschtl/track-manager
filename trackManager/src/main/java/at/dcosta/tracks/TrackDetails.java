package at.dcosta.tracks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.IdValuePair;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.graph.TrackProfile;
import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.TrackActivity;

public class TrackDetails extends ListActivity implements OnClickListener {

	public static class EfficientAdapter extends BaseAdapter implements Filterable {

		static class ViewHolder {
			TextView name, value;
		}

		private final LayoutInflater mInflater;
		private final List<IdValuePair> items;
		private final float maxNameWidth;

		public EfficientAdapter(Context context, List<IdValuePair> items, float maxNameWidth) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
			this.items = items;
			this.maxNameWidth = maxNameWidth;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Filter getFilter() {
			return null;
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a view to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			IdValuePair entry = items.get(position);

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.track_detail_row, null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.detail_name);
				holder.value = (TextView) convertView.findViewById(R.id.detail_value);
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.

			holder.name.setText(entry.getId());
			holder.name.setWidth((int) maxNameWidth);
			holder.value.setText(entry.getValue());
			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}

	private EfficientAdapter adap;
	private TrackDbAdapter trackDbAdapter;

	private long trackId;
	private long listDateStart;

	private TrackDescription trackDescription;
	private Bundle extrasFromRequest;

	private View photos;
	private TextView trackName, activityName;
	private ImageView activityIcon;

	private Intent createIntentForTrack(Class<?> viewClass) {
		Intent intent = new Intent(this, viewClass);
		intent.putExtras(extrasFromRequest);
		intent.putExtra(TrackDescription.KEY_ID, trackId);
		intent.putExtra(TrackDescription.KEY_PATH, trackDescription.getPath());
		return intent;
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtras(extrasFromRequest);
		intent.putExtra(TrackList.KEY_DATE, listDateStart);
		setResult(TrackList.CONTEXT_EDIT_ID, intent);
		finish();
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.but_edit:
				intent = createIntentForTrack(TrackEdit.class);
				startActivity(intent);
				break;
			case R.id.but_chart:
				intent = createIntentForTrack(TrackProfile.class);
				startActivity(intent);
				break;
			case R.id.but_map:
				intent = createIntentForTrack(Configuration.getInstance().getMapViewClass());
				startActivity(intent);
				break;
			case R.id.but_photos:
				intent = createIntentForTrack(ViewPhotos.class);
				intent.putExtra(ViewPhotos.KEY_IMAGES, (Serializable) trackDescription.getMultiValueExtra(TrackDescription.EXTRA_PHOTO));
				startActivity(intent);
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_detail);
		findViewById(R.id.but_edit).setOnClickListener(this);
		findViewById(R.id.but_map).setOnClickListener(this);
		findViewById(R.id.but_chart).setOnClickListener(this);
		getListView().setDivider(null);

		photos = findViewById(R.id.but_photos);
		trackName = (TextView) findViewById(R.id.track_name);
		activityName = (TextView) findViewById(R.id.track_activity);
		activityIcon = (ImageView) findViewById(R.id.activity_icon);

		trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), this);

		extrasFromRequest = getIntent().getExtras();
		trackId = extrasFromRequest.getLong(TrackDescription.KEY_ID);
		listDateStart = (Long) extrasFromRequest.get(TrackList.KEY_DATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		trackDbAdapter.close();
	}

	@Override
	protected void onResume() {
		trackDescription = trackDbAdapter.fetchEntry(trackId);
		trackName.setText(trackDescription.getName());

		TrackActivity activity = trackDescription.getActivity();
		if (activity != null) {
			activityName.setText(activity.getName());
			activityIcon.setImageResource(activity.getIconId());
		}

		if (trackDescription.getMultiValueExtra(TrackDescription.EXTRA_PHOTO) != null) {
			photos.setOnClickListener(this);
		} else {
			photos.setVisibility(View.INVISIBLE);
		}

		List<IdValuePair> properties = new ArrayList<IdValuePair>();
		properties.add(new IdValuePair(R.string.label_start, DateUtil.DATE_TIME_FORMAT_NUMERIC_SHORT.format(trackDescription.getStartTime())));
		properties.add(new IdValuePair(R.string.label_end, DateUtil.DATE_TIME_FORMAT_NUMERIC_SHORT.format(trackDescription.getEndTime())));
		properties.add(new IdValuePair(R.string.label_duration, DateUtil.durationSecondsToString(trackDescription.getMovingTimeSeconds())));
		properties.add(new IdValuePair(R.string.label_distance, Distance.getKm(trackDescription.getHotizontalDistance())));
		properties.add(new IdValuePair(R.string.label_height, trackDescription.getVerticalUp() + " HM"));
		if (trackDescription.getMovingTimeSeconds() != 0) {
			properties.add(new IdValuePair(R.string.label_ascent, trackDescription.getVerticalUp() * 3600 / trackDescription.getMovingTimeSeconds() + " HM/h"));
		}
		String comment = trackDescription.getSingleValueExtra(TrackDescription.EXTRA_COMMENT);
		if (comment != null) {
			properties.add(new IdValuePair(R.string.comment_label, comment));
		}

		float width = 0;
		TextView nameField = (TextView) LayoutInflater.from(this).inflate(R.layout.track_detail_row, null).findViewById(R.id.detail_name);
		for (IdValuePair item : properties) {
			float textWidth = nameField.getPaint().measureText(getString(item.getId()));
			if (textWidth > width) {
				width = textWidth;
			}
		}
		width += pxFromDp(nameField.getPaddingLeft() + nameField.getPaddingRight());
		adap = new EfficientAdapter(this, properties, width);
		setListAdapter(adap);
		super.onResume();
	}

	private float pxFromDp(float dp) {
		return dp * this.getResources().getDisplayMetrics().density;
	}

}
