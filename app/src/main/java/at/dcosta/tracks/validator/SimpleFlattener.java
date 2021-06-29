package at.dcosta.tracks.validator;

import java.util.Iterator;

import at.dcosta.tracks.track.Point;

public class SimpleFlattener implements HeightSmoothener {

    private final Store<Double> store;
    private Point lastPoint;
    private double lastGradient;

    public SimpleFlattener() {
        store = new Store<Double>(2);
    }

    public double process(Point p) {
        if (lastPoint == null) {
            lastPoint = p;
            return p.getHeight();
        }
        double gradient = (p.getHeight() - lastPoint.getHeight()) / ((p.getTimeStampAsLong() - lastPoint.getTimeStampAsLong()) / 1000.0);
        System.out.println("gradient: " + gradient + " p.getHeight() - lastPoint.getHeight()=" + (p.getHeight() - lastPoint.getHeight()));
        store.add(gradient);
        double height;
//        if (gradient >= 0 && lastGradient >= 0 || gradient <= 0 && lastGradient <= 0) {
        if (hasConstantGradient()) {
            height = p.getHeight();
            lastPoint = p;
            System.out.println("using real height: " + height);
        } else {
            height = lastPoint.getHeight();
            System.out.println("using last height " + height + " instead of real height: " + p.getHeight());
        }
        lastGradient = gradient;
        return height;
    }

    private boolean hasConstantGradient() {
        Iterator<Double> it = store.iterator();
        if (it.hasNext()) {
            double g = it.next();
            System.out.print(g);
            boolean gt = g >= 0;
            while (it.hasNext()) {
                g = it.next();
                System.out.print(" | " + g);
                if (gt != (g >= 0)) {
                    System.out.println();
                    return false;
                }
            }
        }
        System.out.println();
        return true;
    }

    static class Store<T> {

        public final Object[] values;
        private int pos;
        private boolean full;

        public Store(int size) {
            values = new Object[size];
        }

        public int size() {
            return values.length;
        }

        public void add(double d) {
            if (pos >= size()) {
                pos = 0;
                full = true;
            }
            values[pos++] = d;
        }

        public Iterator<T> iterator() {
            final IntValue count = new IntValue(0);
            final IntValue readPos = full ? new IntValue(pos - 1) : new IntValue(-1);

            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return full ? count.getValue() < size() : count.getValue() < pos;
                }

                @Override
                public T next() {
                    readPos.increment();
                    if (readPos.getValue() >= size()) {
                        readPos.setValue(0);
                    }
                    count.increment();
                    return (T) values[readPos.getValue()];
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }
            };
        }

        private static final class IntValue {
            private int value;

            public IntValue(int value) {
                this.value = value;
            }

            public void increment() {
                value++;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }
}
