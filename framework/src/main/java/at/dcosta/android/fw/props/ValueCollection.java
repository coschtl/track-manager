package at.dcosta.android.fw.props;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import at.dcosta.android.fw.NameValuePair;

public class ValueCollection implements Serializable, Iterable<NameValuePair> {

    private static final long serialVersionUID = 1L;
    private final Set<NameValuePair> values;

    public ValueCollection() {
        values = new HashSet<NameValuePair>();
    }

    public void addValue(String displayname, String value) {
        values.add(new NameValuePair(displayname, value));
    }

    public Set<NameValuePair> getValues() {
        return values;
    }

    @NonNull
    @Override
    public Iterator<NameValuePair> iterator() {
        return values.iterator();
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        boolean notFirst = false;
        for (NameValuePair value : values) {
            if (notFirst) {
                b.append("|");
            }
            b.append(value);
            notFirst = true;
        }
        b.append("}");
        return b.toString();
    }

}
