package at.dcosta.tracks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.gui.IconListActivity;
import at.dcosta.android.fw.gui.IconListBean;
import at.dcosta.tracks.db.SavedSearchesDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.share.BluetoothSender;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.LayoutDescription;
import at.dcosta.tracks.util.SavedSearch;

public class TrackList extends IconListActivity implements OnItemClickListener, OnClickListener {

	public static final String KEY_DATE = "keyDate";
	public static final String KEY_MENU_ID = "menuId";
	public static final String KEY_USER_SEARCH = "userDefinedSearch";

	public static final int CONTEXT_SHOW_PHOTOS = Menu.FIRST;
	public static final int CONTEXT_SHOW_ON_MAP_ID = CONTEXT_SHOW_PHOTOS + 1;
	public static final int CONTEXT_EDIT_ID = CONTEXT_SHOW_ON_MAP_ID + 1;
	public static final int CONTEXT_COPY = CONTEXT_EDIT_ID + 1;
	public static final int CONTEXT_DELETE = CONTEXT_COPY + 1;
	public static final int CONTEXT_SHARE_VIA_BT = CONTEXT_DELETE + 1;
	public static final int CONTEXT_SEARCH_TRACKS_ID = CONTEXT_SHARE_VIA_BT + 1;
	public static final int CONTEXT_SHOW_TRACKS_DETAILS = CONTEXT_SEARCH_TRACKS_ID + 1;
	public static final int CONTEXT_MERGE_SELECT = CONTEXT_SHOW_TRACKS_DETAILS + 1;

	public static final int OPTIONS_MERGE_TRACKS = Integer.MAX_VALUE;

	private static final Date DEFAULT_START = DateUtil.getDay(1, 1, 2000);
	private static final Date DEFAULT_END = DateUtil.getDay(1, 1, 2100);
	private final Configuration config = Configuration.getInstance(this);
	private final Set<Long> toMerge;
	private TrackDbAdapter trackDbAdapter;
	private SavedSearchesDbAdapter savedSearchesDbAdapter;
	private boolean showOnlyMultiActivityDays;
	private Date dateStart;
	private Date dateEnd;
	private String activity;
	private String nameLike;
	private String commentLike;
	private String headline;
	private List<IconListBean> beanItems;
	private int actualMenuId = R.id.but_day;
	private boolean isUserdefinedSearch;
	private List<String> savedSearches;
	private Menu menu;

	private Parcelable listViewState;

	public TrackList() {
		super(LayoutDescription.TRACK_LIST);
		toMerge = new HashSet<>();
	}

	private void addSavedSearchesToMenu(Menu menu) {
		int i = 0;
		for (String alias : savedSearches) {
			menu.add(0, i++, i, alias);
		}
	}

	private void closeDbs() {
		trackDbAdapter.close();
		savedSearchesDbAdapter.close();
	}

	private Intent createIntentForTrack(IconListBean trackBean, Class<?> viewClass) {
		closeDbs();
		Long id = (Long) trackBean.getExtra(TrackDescriptionNG.KEY_ID);
		Intent intent = new Intent(this, viewClass);
		Bundle state = new Bundle();
		saveSearchParamsToBundle(state);
		intent.putExtras(state);
		intent.putExtra(KEY_DATE, dateStart.getTime());
		intent.putExtra(TrackDescriptionNG.KEY_ID, id);
		intent.putExtra(TrackDescriptionNG.KEY_PATH, (String) trackBean.getExtra(TrackDescriptionNG.KEY_PATH));
		listViewState = getList().onSaveInstanceState();
		return intent;
	}

	@Override
	public List<IconListBean> getBeans() {
		return beanItems;
	}

	@Override
	public String getHeadline() {
		return headline;
	}

	private void loadAndShowTracks() {
		long time = 0;
		long distH = 0;
		long distV = 0;
		beanItems.clear();
		boolean first = true;
		Iterator<TrackDescriptionNG> it = trackDbAdapter.findEntries(dateStart, dateEnd, activity, nameLike, commentLike);
		int i = 0;
		Map<Date, TrackDescriptionNG> trackDates = new HashMap<>();
		TrackDescriptionNG lastTrack = null;
		Set<Long> toDelete = new HashSet<>();
		while (it.hasNext()) {
			TrackDescriptionNG track = it.next();
			if (first) {
				dateStart = track.getStartTime();
				first = false;
			}
			if (showOnlyMultiActivityDays) {
				if (isSameTrack(lastTrack, track)) {
					toDelete.add(track.getId());
					continue;
				}
				lastTrack = track;
				Date start = DateUtil.getDayStart(track.getStartTime());
				if (trackDates.containsKey(start)) {
					TrackDescriptionNG cached = trackDates.get(start);
					if (cached != null) {
						beanItems.add(cached.toIconListBean(i++));
						time += cached.getMovingTimeSeconds();
						distH += cached.getHorizontalDistance();
						distV += cached.getVerticalUp();
						trackDates.put(start, null);
					}
					beanItems.add(track.toIconListBean(i++));
					time += track.getMovingTimeSeconds();
					distH += track.getHorizontalDistance();
					distV += track.getVerticalUp();
				} else {
					trackDates.put(start, track);
				}
			} else {
				beanItems.add(track.toIconListBean(i++));
				time += track.getMovingTimeSeconds();
				distH += track.getHorizontalDistance();
				distV += track.getVerticalUp();
			}
		}
		System.out.println("------------");
		System.out.println("must delete " + toDelete.size() + " entries: " + toDelete);
		toDelete.stream().forEach(id -> trackDbAdapter.deleteEntry(id));
		System.out.println("------------");
		headline = new StringBuilder()
				.append(DateUtil.formatDateRange(dateStart == null ? DEFAULT_START : dateStart, dateEnd == null ? DEFAULT_END : dateEnd, DateUtil.Format.LONG))
				.append(":\n").append(i).append(" tracks: ").append(DateUtil.durationSecondsToString(time)).append("\n").append(Distance.getKm(distH))
				.append(" km, ").append(distV).append(" HM").toString();
		if (beanItems.size() == 0) {
			headline = getString(R.string.no_tracks_found);
		}
		updateView();
		if (listViewState != null) {
			getList().onRestoreInstanceState(listViewState);
			listViewState = null;
		}
	}

	private boolean isSameTrack(TrackDescriptionNG lastTrack, TrackDescriptionNG track) {
		if (lastTrack == null) {
			return track == null;
		}
		boolean isSame =  Objects.equals(lastTrack.getStartTime(), track.getStartTime())
				&& Objects.equals(lastTrack.getEndTime(), track.getEndTime());
		boolean sameNamePathUriAndActiviti = Objects.equals(lastTrack.getPath(), track.getPath())
				&& Objects.equals(lastTrack.getName(), track.getName())
				&& Objects.equals(lastTrack.getPathUri(), track.getPathUri())
				&& Objects.equals(lastTrack.getActivity(), track.getActivity());

		if (isSame && !sameNamePathUriAndActiviti) {
			System.out.println("NEARLY SAME TRACK:");
			System.out.println(lastTrack.getStartTime() + "  -  " + lastTrack.getEndTime());
			if (!Objects.equals(lastTrack.getName() , track.getName())) {
				System.out.println("Name: " + lastTrack.getName() + "  -  " + track.getName());
			}
			if (!Objects.equals(lastTrack.getPath() , track.getPath())) {
				System.out.println("Path: " + lastTrack.getPath() + "  -  " + track.getPath());
			}
			if (!Objects.equals( lastTrack.getPathUri() , track.getPathUri())) {
				System.out.println("PathUri: " + lastTrack.getPathUri() + "  -  " + track.getPathUri());
			}
			if (!Objects.equals( lastTrack.getActivity() ,track.getActivity())) {
				System.out.println("Activity: " + lastTrack.getActivity() + "  -  " + track.getActivity());
			}
			System.out.println("-----\n");
		}
		return isSame && sameNamePathUriAndActiviti;
	}

	private void loadSearchParamsFromBundle(Bundle extras) {
		if (extras != null) {
			showOnlyMultiActivityDays = extras.getBoolean(TrackSearch.MULTIACTIVITY_DAYS_ONLY, false);
			nameLike = extras.getString(TrackSearch.TRACK_NAME);
			activity = extras.getString(TrackSearch.ACTIVITY);
			dateStart = (Date) extras.get(TrackSearch.DATE_FROM);
			dateEnd = (Date) extras.get(TrackSearch.DATE_TO);
			commentLike = extras.getString(TrackSearch.COMMENT);
		}
	}

	private void mergeTracks() {
		Iterator<Long> it = toMerge.iterator();
		TrackDescriptionNG track1 = trackDbAdapter.fetchEntry(it.next());
		TrackDescriptionNG track2 = trackDbAdapter.fetchEntry(it.next());
		if (track1.getStartTime().after(track2.getStartTime())) {
			TrackDescriptionNG t = track1;
			track1 = track2;
			track2 = t;
		}
		if (track1.getEndTime().after(track2.getStartTime())) {
			Toast.makeText(this, R.string.merge_tracks_overlapping, Toast.LENGTH_LONG).show();
			return;
		}
		String timeDiff = DateUtil.durationMillisToString(track2.getStartTime().getTime() - track1.getEndTime().getTime());
		List<Point> track1Points = Configuration.getInstance().getTrackCache().load(track1.getId());
		List<Point> track2Points = Configuration.getInstance().getTrackCache().load(track2.getId());
		Distance distance = track2Points.get(0).getDistance(track1Points.get(track1Points.size() - 1));
		// String distanceString = distance.getHorizontal();
		Toast.makeText(this, timeDiff, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		savedSearches = savedSearchesDbAdapter.fetchAllAliases();
		if (menu != null) {
			for (int i = 0; i < menu.size(); i++) {
				menu.removeItem(i);
			}
			addSavedSearchesToMenu(menu);
		}

		if (isUserdefinedSearch && resultCode == RESULT_CANCELED) {
			finish();
		}
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {

			loadSearchParamsFromBundle(intent.getExtras());
			loadAndShowTracks();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.but_day:
			case R.id.but_week:
			case R.id.but_month:
				actualMenuId = view.getId();
				setDate(dateStart == null ? System.currentTimeMillis() : dateStart.getTime());
				loadAndShowTracks();
				break;
			case R.id.but_search:
				actualMenuId = view.getId();
				startUserdefinedSearch();
				break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final IconListBean bean = getItem((int) info.id);
		final long trackId = ((Long) bean.getExtra(TrackDescriptionNG.KEY_ID)).longValue();
		Intent intent = null;
		switch (item.getItemId()) {
			case CONTEXT_EDIT_ID:
				intent = createIntentForTrack(bean, TrackEdit.class);
				startActivityForResult(intent, CONTEXT_EDIT_ID);
				return true;
			case CONTEXT_COPY:
				intent = createIntentForTrack(bean, TrackCopy.class);
				startActivityForResult(intent, CONTEXT_COPY);
				return true;
			case CONTEXT_SHARE_VIA_BT:
				intent = createIntentForTrack(bean, BluetoothSender.class);
				startActivityForResult(intent, CONTEXT_SHARE_VIA_BT);
				// NetworkSender sender = new NetworkSender("192.168.1.150", 10856, null);
				// TrackDescription descr = trackDbAdapter.fetchEntry((Long) bean.getExtra(TrackDescription.KEY_ID));
				// try {
				// sender.send(new TrackToShare(descr, TrackIO.loadTrack(new File(descr.getPath()))));
				// } catch (ParsingException e) {
				// e.printStackTrace();
				// }
				return true;
			case CONTEXT_DELETE:
				closeDbs();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.delete_track).setMessage(R.string.are_you_sure_delete).setCancelable(false)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								trackDbAdapter.deleteEntry(trackId);
								config.getTrackCache().delete(trackId);
								closeDbs();
								TrackList.this.finish();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				builder.create().show();
				break;
			case CONTEXT_MERGE_SELECT:
				int bgColor;
				if (toMerge.contains(trackId)) {
					toMerge.remove(trackId);
					bgColor = Color.BLACK;
				} else if (toMerge.size() < 2) {
					toMerge.add(trackId);
					bgColor = Color.GRAY;
				} else {
					bgColor = Color.BLACK;
					Toast.makeText(this, R.string.merge_select_only_two_tracks, Toast.LENGTH_LONG).show();
				}
				info.targetView.setBackgroundColor(bgColor);
				if (menu != null) {
					menu.clear();
					onCreateOptionsMenu(menu);
				}
				break;
			case CONTEXT_SHOW_ON_MAP_ID:
				intent = createIntentForTrack(bean, config.getMapViewClass());
				startActivity(intent);
				return true;
			case CONTEXT_SHOW_PHOTOS:
				showPhotos(bean);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		trackDbAdapter = new TrackDbAdapter(config.getDatabaseHelper(), this);
		savedSearchesDbAdapter = new SavedSearchesDbAdapter(config.getDatabaseHelper(), this);
		savedSearches = savedSearchesDbAdapter.fetchAllAliases();
		beanItems = new ArrayList<IconListBean>();

		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			showOnlyMultiActivityDays = extras.getBoolean(TrackSearch.MULTIACTIVITY_DAYS_ONLY, false);
			actualMenuId = extras.getInt(KEY_MENU_ID, R.id.but_day);
			long actualDate = extras.getLong(KEY_DATE);
			if (actualDate > 0) {
				setDate(actualDate);
				if (showOnlyMultiActivityDays) {
					dateStart = new Date(0);
					dateEnd = new Date();
				}
				loadAndShowTracks();
			}
		}
		findViewById(R.id.but_day).setOnClickListener(this);
		findViewById(R.id.but_week).setOnClickListener(this);
		findViewById(R.id.but_month).setOnClickListener(this);
		findViewById(R.id.but_search).setOnClickListener(this);
		registerForContextMenu(getList());

		if (extras != null && extras.getBoolean(KEY_USER_SEARCH, false)) {
			isUserdefinedSearch = true;
			startUserdefinedSearch();
		} else {
			isUserdefinedSearch = false;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			IconListBean iconListBean = beanItems.get(info.position);
			int photoId = iconListBean.getIntExtra(IconListBean.KEY_ADDITIONAL_ICON1, -1);
			if (photoId >= 0) {
				menu.add(0, CONTEXT_SHOW_PHOTOS, 0, R.string.show_photos);
			}
		}
		menu.add(0, CONTEXT_SHOW_ON_MAP_ID, 0, R.string.show_on_map);
		menu.add(0, CONTEXT_EDIT_ID, 0, R.string.menu_edit_track);
		menu.add(0, CONTEXT_COPY, 0, R.string.menu_copy_track);
		menu.add(0, CONTEXT_DELETE, 0, R.string.menu_delete_track);
		menu.add(0, CONTEXT_MERGE_SELECT, 0, R.string.menu_merge_select);
		menu.add(0, CONTEXT_SHARE_VIA_BT, 0, R.string.menu_share_track_bt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
		if (toMerge.size() > 1) {
			menu.add(0, OPTIONS_MERGE_TRACKS, 0, R.string.menu_merge_tracks);
		} else {
			addSavedSearchesToMenu(menu);
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = createIntentForTrack(beanItems.get(position), TrackDetails.class);
		startActivityForResult(intent, CONTEXT_SHOW_TRACKS_DETAILS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == OPTIONS_MERGE_TRACKS) {
			mergeTracks();
			return true;
		}
		SavedSearch savedSearch = savedSearchesDbAdapter.findEntry(savedSearches.get(item.getItemId()));
		if (savedSearch != null) {
			nameLike = savedSearch.getName();
			activity = savedSearch.getActivity();
			dateStart = savedSearch.getDateStart();
			dateEnd = savedSearch.getDateEnd();
			loadAndShowTracks();
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeDbs();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		loadSearchParamsFromBundle(state);
		toMerge.clear();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		saveSearchParamsToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	private void saveSearchParamsToBundle(Bundle bundle) {
		bundle.putString(TrackSearch.TRACK_NAME, nameLike);
		bundle.putString(TrackSearch.ACTIVITY, activity);
		bundle.putSerializable(TrackSearch.DATE_FROM, dateStart);
		bundle.putSerializable(TrackSearch.DATE_TO, dateEnd);
		bundle.putSerializable(TrackSearch.COMMENT, commentLike);
	}

	private void setDate(long dateMillis) {
		Date date = new Date(dateMillis);
		switch (actualMenuId) {
			case R.id.but_day:
				dateStart = DateUtil.getDay(date);
				dateEnd = DateUtil.getDayEnd(dateStart);
				break;
			case R.id.but_week:
				dateStart = DateUtil.getStartOfWeek(date);
				dateEnd = DateUtil.getEndOfWeek(dateStart);
				break;
			case R.id.but_month:
				dateStart = DateUtil.getStartOfMonth(date);
				dateEnd = DateUtil.getEndOfMonth(dateStart);
				break;
		}
		nameLike = null;
		commentLike = null;
		activity = null;
	}

	private void showPhotos(final IconListBean bean) {
		Intent intent = new Intent(this, ViewPhotos.class);
		TrackDescriptionNG track = trackDbAdapter.findEntryByPath((String) bean.getExtra(TrackDescriptionNG.KEY_PATH));
		intent.putExtra(ViewPhotos.KEY_IMAGES, (Serializable) track.getPhotos());
		startActivity(intent);
	}

	private void startUserdefinedSearch() {
		Intent intent = new Intent(this, TrackSearch.class);
		intent.putExtra("actualMenuId", actualMenuId);
		startActivityForResult(intent, CONTEXT_SEARCH_TRACKS_ID);
	}

}
