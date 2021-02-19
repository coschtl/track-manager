package at.dcosta.tracks.util;

import java.io.Serializable;
import java.util.Date;

public class SavedSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String alias, name, activity;
    private final Date dateStart, dateEnd;

    public SavedSearch(String alias, String name, String activity, Date dateStart, Date dateEnd) {
        this.alias = alias;
        this.name = name;
        this.activity = activity;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public SavedSearch(String alias, String name, String activity, long dateStart, long dateEnd) {
        this.alias = alias;
        this.name = name;
        this.activity = activity;
        this.dateStart = dateStart > 0 ? new Date(dateStart * 1000l) : null;
        this.dateEnd = dateEnd > 0 ? new Date(dateEnd * 1000l) : null;
    }

    public String getActivity() {
        return activity;
    }

    public String getAlias() {
        return alias;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public String getName() {
        return name;
    }

}
