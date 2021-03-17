package at.dcosta.tracks.track;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.gui.IconListBean;
import at.dcosta.tracks.R;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.TrackActivity;

public class TrackDescriptionNG implements Serializable {

    public static final String EXTRA_ICON = "icon";
    public static final String EXTRA_PHOTO = "photo";
    public static final String EXTRA_COMMENT = "comment";
    public static final String KEY_PATH = "path";
    public static final String KEY_ID = "id";
    private static final long serialVersionUID = -4802187569906508418L;
    private static final SimpleDateFormat DATE_FROM_NAME = new SimpleDateFormat("yyyy_MM_dd");
    private static final SimpleDateFormat DATE_TIME_FROM_NAME = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    private static final Pattern PATTERN_MINUS = Pattern.compile("-");
    private final long id;
    private final Map<String, String> singleValueExtras;
    private final Map<String, List<String>> multivalueExtras;
    private String path;
    private Date startTime;
    private Date endTime;
    private int movingTimeSeconds;
    private long horizontalDistance;
    private int verticalUp;
    private int avgPulse;
    private int maxPulse;
    private transient ActivityFactory activityFactory;
    private String name;

    public TrackDescriptionNG(TrackDescription td, ActivityFactory activityFactory) {
        path = td.getPath();
        startTime = td.getStartTime();

        endTime = td.getEndTime();
        id = td.getId();
        movingTimeSeconds = td.getMovingTimeSeconds();
        horizontalDistance = td.getHorizontalDistance();
        verticalUp = td.getVerticalUp();
        avgPulse = 0;
        maxPulse = 0;
        singleValueExtras = td.getSingleValueExtras();
        multivalueExtras = td.getMultiValueExtras();
        this.activityFactory = activityFactory;
        name = td.getName();
    }

    public TrackDescriptionNG(long id, String name, String path, long startEpochSecs, long endEpochSecs, int movingTimeSeconds, long horizontalDistance,
                              int verticalUp, int avgPulse, int maxPulse, ActivityFactory activityFactory) {
        this.id = id;
        startTime = new Date(startEpochSecs * 1000l);
        endTime = new Date(endEpochSecs * 1000l);
        this.name = name;
        this.path = path;
        this.movingTimeSeconds = movingTimeSeconds;
        this.horizontalDistance = horizontalDistance;
        this.verticalUp = verticalUp;
        this.avgPulse = avgPulse;
        this.maxPulse = maxPulse;
        singleValueExtras = new HashMap<String, String>();
        multivalueExtras = new HashMap<String, List<String>>();
        this.activityFactory = activityFactory;
    }

    public TrackDescriptionNG(String name, String path, TrackStatistic statistic, ActivityFactory activityFactory) {
        id = -1;
        this.name = name;
        this.path = path;
        this.activityFactory = activityFactory;
        singleValueExtras = new HashMap<String, String>();
        multivalueExtras = new HashMap<String, List<String>>();
        updateStatistic(statistic);
    }

    public TrackDescriptionNG addMultiValueExtra(String name, String value) {
        List<String> list = getMultiValueExtras().get(name);
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(value);
        setMultiValueExtra(name, list);
        return this;
    }

    public TrackActivity getActivity() {
        return getActivity(null);
    }

    private TrackActivity getActivity(TrackActivity defaultActivity) {
        if (activityFactory != null) {
            return activityFactory.fromIcon(getSingleValueExtra(EXTRA_ICON), defaultActivity);
        }
        return null;
    }

    public Date getEndTime() {
        return endTime;
    }

    public long getHorizontalDistance() {
        return horizontalDistance;
    }

    public long getId() {
        return id;
    }

    public int getMovingTimeSeconds() {
        return movingTimeSeconds;
    }

    public List<String> getMultiValueExtra(String name) {
        return getMultiValueExtras().get(name);
    }

    public Map<String, List<String>> getMultiValueExtras() {
        return multivalueExtras;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathNoHash() {
        int pos = getPath().indexOf('#');
        if (pos == -1) {
            return path;
        }
        return path.substring(0, pos);
    }

    public String getSingleValueExtra(String name) {
        return getSingleValueExtras().get(name);
    }

    public String getSingleValueExtra(String name, String defaultValue) {
        String value = getSingleValueExtras().get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Map<String, String> getSingleValueExtras() {
        return singleValueExtras;
    }

    public Date getStartTime() {
        return startTime;
    }

    public int getVerticalUp() {
        return verticalUp;
    }

    public int getAvgPulse() {
        return avgPulse;
    }

    public int getMaxPulse() {
        return maxPulse;
    }

    public boolean hasExtras() {
        return getSingleValueExtras().size() + getMultiValueExtras().size() > 0;
    }

    public void setActivityFactory(ActivityFactory activityFactory) {
        this.activityFactory = activityFactory;
    }

    public TrackDescriptionNG setMultiValueExtra(String name, List<String> values) {
        getMultiValueExtras().put(name, values);
        return this;
    }

    public TrackDescriptionNG setSingleValueExtra(String name, String value) {
        getSingleValueExtras().put(name, value);
        return this;
    }

    public IconListBean toIconListBean(long id) {
        String body = new StringBuilder().append("Start: ").append(DateUtil.DATE_TIME_FORMAT_NUMERIC_SHORT.format(getStartTime())).append("\n")
                .append(DateUtil.durationSecondsToString(getMovingTimeSeconds())).append("\n").append(Distance.getKm(getHorizontalDistance())).append(", ")
                .append(getVerticalUp()).append(" Hm").toString();
        TrackActivity activity = getActivity(TrackActivity.SELECT);
        IconListBean bean = new IconListBean(id, getName(), body, activity.getIconId());
        bean.setExtra(KEY_ID, getId());
        bean.setExtra(KEY_PATH, getPath());
        List<String> extraPhotos = getMultiValueExtra(EXTRA_PHOTO);
        if (extraPhotos != null && extraPhotos.size() > 0) {
            bean.setExtra(IconListBean.KEY_ADDITIONAL_ICON1, at.dcosta.tracks.R.mipmap.camera);
        }
        if (getAvgPulse() > 0) {
            bean.setExtra(IconListBean.KEY_ADDITIONAL_ICON2, R.mipmap.ekg);
        }
        return bean;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(getStartTime()).append(": ").append(getName()).append(", ").append(getPath()).append(": ")
                .append(getSingleValueExtras()).append(getMultiValueExtras()).toString();
    }

    public void updateStatistic(TrackStatistic statistic) {
        if (statistic.hasTimeData()) {
            startTime = statistic.getStartTime();
            endTime = statistic.getEndTime();
            movingTimeSeconds = statistic.getMovingTimeSeconds();
        } else {
            Date d = null;
            File pathFile = new File(path);
            String dateFromName = PATTERN_MINUS.matcher(pathFile.getName()).replaceAll("_");
            try {
                d = DATE_TIME_FROM_NAME.parse(dateFromName);
            } catch (ParseException e) {
                try {
                    d = DATE_FROM_NAME.parse(dateFromName);
                } catch (ParseException e1) {
                    throw new RuntimeException("Can not evaluate Date from Track name '" + name + "'");
                }
            }
            startTime = d;
            endTime = d;
            movingTimeSeconds = 0;
        }
        Distance totalDistance = statistic.getTotalDistance();
        horizontalDistance = totalDistance.getHorizontal();
        verticalUp = totalDistance.getVerticalUp();
        avgPulse = statistic.getAvgPulse();
        maxPulse = statistic.getMaxPulse();
    }

}
