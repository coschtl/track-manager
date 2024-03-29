package at.dcosta.android.fw.gui;

import at.dcosta.android.fw.ExtraBased;

public class IconListBean extends ExtraBased<IconListBean> implements Comparable<IconListBean> {

    public static final String KEY_ADDITIONAL_ICON1 = "additionalIcon1";
    public static final String KEY_ADDITIONAL_ICON2 = "additionalIcon2";
    private static final long serialVersionUID = 1L;
    private final String head, body;
    private final long id;
    int iconId;

    public IconListBean(long id, String head, String body, int iconId) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.iconId = iconId;
    }

    @Override
    public int compareTo(IconListBean bean) {
        return getHead().compareTo(bean.getHead());
    }

    public String getBody() {
        return body;
    }

    public String getHead() {
        return head;
    }

    public int getIconId() {
        return iconId;
    }

    public long getId() {
        return id;
    }
}
