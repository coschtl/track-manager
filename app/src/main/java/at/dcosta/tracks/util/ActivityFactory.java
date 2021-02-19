package at.dcosta.tracks.util;

import android.content.Context;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.dcosta.tracks.R;
import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.Validators;

public class ActivityFactory {

	private final Context context;
	private final Map<Integer, TrackActivity> activitiesByActivity;
	private final Map<String, TrackActivity> activitiesByIconId;

	public ActivityFactory(Context context) {
		this.context = context;
		activitiesByActivity = new HashMap<Integer, TrackActivity>();
		activitiesByIconId = new HashMap<String, TrackActivity>();
		registerActivity(R.string.activity_climbing, R.mipmap.climbing, Color.parseColor("#F3F781"), Validators.CLIMBING);
		registerActivity(R.string.activity_hiking, R.mipmap.hiking, Color.parseColor("#F7FE2E"), Validators.HIKE);
		registerActivity(R.string.activity_mountainbiking, R.mipmap.mountainbiking, Color.parseColor("#31B404"), Validators.BIKE);
		registerActivity(R.string.activity_biking, R.mipmap.biking, Color.parseColor("#088A08"), Validators.BIKE);
		registerActivity(R.string.activity_bike_and_hike, R.mipmap.bikeandhike, Color.parseColor("#64FE2E"), Validators.BIKE);
		registerActivity(R.string.activity_running, R.mipmap.running, Color.parseColor("#31B404"), Validators.HIKE);
		registerActivity(R.string.activity_skiing, R.mipmap.skiing, Color.parseColor("#58FAF4"), Validators.SKITOUR);
		registerActivity(R.string.activity_figln, R.mipmap.figln, Color.parseColor("#58FAF4"), Validators.SKITOUR);
		registerActivity(R.string.activity_skitour, R.mipmap.skitour, Color.parseColor("#0404B4"), Validators.SKITOUR);
		registerActivity(R.string.activity_sledging, R.mipmap.sledging, Color.parseColor("#2E9AFE"), Validators.SLEDGE);
	}

	public TrackActivity fromIcon(String icon) {
		return fromIcon(icon, TrackActivity.SELECT);
	}

	public TrackActivity fromIcon(String icon, TrackActivity defaultActivity) {
		if (icon == null) {
			return defaultActivity;
		}

		TrackActivity tourActivity = activitiesByIconId.get(getPlainIconName(icon));
		if (tourActivity == null) {
			return defaultActivity;
		}
		return tourActivity;
	}

	private String getPlainIconName(String iconName) {
		if (iconName == null) {
			return null;
		}
		int pos = iconName.lastIndexOf('/');
		return iconName.substring(pos + 1);
	}

	public List<TrackActivity> getAllActivities() {
		List<TrackActivity> l = new ArrayList<TrackActivity>(activitiesByActivity.values());
		Collections.sort(l, new Comparator<TrackActivity>() {
			@Override
			public int compare(TrackActivity a1, TrackActivity a2) {
				return a1.getName().compareTo(a2.getName());
			}
		});
		return l;
	}

	private void registerActivity(final Integer nameId, final int iconId, final int color, final DistanceValidator distanceValidator) {
		final String icon = context.getString(iconId);
		if (!activitiesByActivity.containsKey(nameId)) {
			synchronized (this) {
				if (!activitiesByActivity.containsKey(nameId)) {
					final String name = context.getString(nameId);
					TrackActivity activity = new TrackActivity() {

						@Override
						public boolean equals(Object obj) {
							if (obj instanceof TrackActivity) {
								return getIcon().equals(((TrackActivity) obj).getIcon());
							}
							return false;
						}

						@Override
						public int getColor() {
							return color;
						}

						@Override
						public DistanceValidator getDistanceValidator() {
							return distanceValidator;
						}

						@Override
						public String getIcon() {
							return icon;
						}

						@Override
						public int getIconId() {
							return iconId;
						}

						@Override
						public String getName() {
							return name;
						}

						@Override
						public int hashCode() {
							return getName().hashCode() + getIcon().hashCode();
						}

						@Override
						public String toString() {
							return getName();
						}
					};
					activitiesByActivity.put(nameId, activity);
					activitiesByIconId.put(getPlainIconName(icon), activity);
				}
			}
		}
	}

}
