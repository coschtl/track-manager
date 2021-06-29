package at.dcosta.tracks.validator;


import org.junit.Test;

import java.util.Iterator;

public class SimpleFlattenerTest {

    @Test
    public void testStore() {
        SimpleFlattener.Store store = new SimpleFlattener.Store(4);
        for (int i = 0; i < 10; i++) {
            store.add(i);
            //  System.out.println(i + ": " + toString(store.values) + " - " + toString(store));

        }
    }

    private String toString(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (double d : doubles) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(d);
        }
        return b.toString();
    }

    private String toString(SimpleFlattener.Store store) {
        StringBuilder b = new StringBuilder();
        Iterator<Double> it = store.iterator();
        {
            while (it.hasNext()) {
                double d = it.next();
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(d);
            }
            return b.toString();
        }
    }

}