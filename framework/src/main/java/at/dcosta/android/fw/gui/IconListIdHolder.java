package at.dcosta.android.fw.gui;

import at.dcosta.android.fw.ExtraBased;

public class IconListIdHolder extends ExtraBased<IconListIdHolder> {

    public static final String KEY_ADDITIONAL_ICON1 = "additionalIcon1";
    public static final String KEY_ADDITIONAL_ICON2 = "additionalIcon2";
    private static final long serialVersionUID = 1L;
    private int listLayoutId, listRowLayoutId, headerId, lineHeadId, lineBodyId, lineIconId;

    public int getHeaderId() {
        return headerId;
    }

    public IconListIdHolder setHeaderId(int headerId) {
        this.headerId = headerId;
        return this;
    }

    public int getLineBodyId() {
        return lineBodyId;
    }

    public IconListIdHolder setLineBodyId(int lineBodyId) {
        this.lineBodyId = lineBodyId;
        return this;
    }

    public int getLineHeadId() {
        return lineHeadId;
    }

    public IconListIdHolder setLineHeadId(int lineHeadId) {
        this.lineHeadId = lineHeadId;
        return this;
    }

    public int getLineIconId() {
        return lineIconId;
    }

    public IconListIdHolder setLineIconId(int lineIconId) {
        this.lineIconId = lineIconId;
        return this;
    }

    public int getListLayoutId() {
        return listLayoutId;
    }

    public IconListIdHolder setListLayoutId(int listLayoutId) {
        this.listLayoutId = listLayoutId;
        return this;
    }

    public int getListRowLayoutId() {
        return listRowLayoutId;
    }

    public IconListIdHolder setListRowLayoutId(int listRowLayoutId) {
        this.listRowLayoutId = listRowLayoutId;
        return this;
    }
}
