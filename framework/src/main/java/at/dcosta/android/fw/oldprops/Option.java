package at.dcosta.android.fw.oldprops;

import at.dcosta.android.fw.gui.IconListBean;

public class Option extends IconListBean {

    private static final long serialVersionUID = 1L;
    private final String path;

    public Option(long id, String name, String data, String path, int icon) {
        super(id, name, data, icon);
        this.path = path;
    }

    public String getData() {
        return getBody();
    }

    public String getName() {
        return getHead();
    }

    public String getPath() {
        return path;
    }
}
