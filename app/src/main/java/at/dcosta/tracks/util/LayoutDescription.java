package at.dcosta.tracks.util;

import at.dcosta.android.fw.gui.IconListIdHolder;
import at.dcosta.tracks.R;

public class LayoutDescription {

    public static final IconListIdHolder DEFAULT;
    public static final IconListIdHolder TRACK_LIST;

    static {
        DEFAULT = createDefaultHolder();
        TRACK_LIST = createDefaultHolder().setListLayoutId(R.layout.track_list).setListRowLayoutId(R.layout.track_list_row)
                .setExtra(IconListIdHolder.KEY_ADDITIONAL_ICON1, R.id.additional_icon1)
                .setExtra(IconListIdHolder.KEY_ADDITIONAL_ICON2, R.id.additional_icon2);
    }

    private static final IconListIdHolder createDefaultHolder() {
        return new IconListIdHolder().setListLayoutId(R.layout.list).setListRowLayoutId(R.layout.list_row).setHeaderId(R.id.list_header)
                .setLineIconId(R.id.line_icon).setLineHeadId(R.id.line_head).setLineBodyId(R.id.line_body);
    }
}
