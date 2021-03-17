package at.dcosta.tracks;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.dcosta.android.fw.gui.calendar.Day;
import at.dcosta.android.fw.gui.calendar.Month;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.util.TrackActivity;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final Month<TrackDescriptionNG> month;
    private final Map<Date, List<TrackDescriptionNG>> tracks;
    private final GridView.LayoutParams layoutParams;
    private final int COLOR_DEFAULT = Color.parseColor("#FE2E2E");

    public CalendarAdapter(Context c, Month<TrackDescriptionNG> month, Map<Date, List<TrackDescriptionNG>> tracks) {
        context = c;
        this.month = month;
        this.tracks = tracks;

        // Get the screen's density scale
        float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        int pixels = (int) (43 * scale + 0.5f);
        layoutParams = new GridView.LayoutParams(pixels, pixels);
    }

    @Override
    public int getCount() {
        return 7 * month.getRowCount();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) { // if it's not recycled, initialize some attributes
            textView = new TextView(context);
            textView.setBackgroundColor(Color.BLACK);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
        } else {
            textView = (TextView) convertView;
        }
        textView.setTextColor(Color.WHITE);
        Day<TrackDescriptionNG> day = month.getDay(position);
        textView.setCursorVisible(false);
        if (day == null) {
            textView.setText("");
            textView.setBackgroundColor(Color.BLACK);
            return textView;
        }

        Date dayDate = day.getDate();
        if (dayDate != null && tracks.containsKey(dayDate)) {
            if (!day.hasContent()) {
                for (TrackDescriptionNG descr : tracks.get(dayDate)) {
                    day.addContent(descr);
                }
            }
        }
        textView.setText(day.getDayAsString());

        if (day.hasContent()) {
            List<TrackDescriptionNG> content = day.getContent();
            int color = 0;
            for (TrackDescriptionNG descr : content) {
                TrackActivity activity = descr.getActivity();
                if (activity != null) {
                    color += activity.getColor();
                }
            }
            if (color == 0) {
                color = COLOR_DEFAULT;
            }
            textView.setBackgroundColor(color);
        } else if (day.isWeekend()) {
            textView.setBackgroundColor(Color.LTGRAY);
            textView.setTextColor(Color.BLACK);
        }
        return textView;
    }
}
