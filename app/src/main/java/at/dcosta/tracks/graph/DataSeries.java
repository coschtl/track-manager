package at.dcosta.tracks.graph;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracks.util.MinMaxValue;

public class DataSeries<T extends Number> {

    private final List<T> values;
    private final MinMaxValue minMax;

    public DataSeries() {
        values = new ArrayList<>();
        minMax = new MinMaxValue();
    }

    public void add(T value) {
        values.add(value);
        minMax.update(value.longValue());
    }

    public List<T> getValues() {
        return values;
    }

    public MinMaxValue getMinMax() {
        return minMax;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    public T getLastValue() {
        if (isEmpty()) {
            throw new IllegalStateException("Series is empty!");
        }
        return values.get(values.size() - 1);
    }
}
