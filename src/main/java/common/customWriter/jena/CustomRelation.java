package common.customWriter.jena;

import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.Map1Iterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.*;

/**
 * Updated version of org.apache.jena.rdfxml.xmloutput.impl.Relation
 *
 * Has to be copied because the original class is package private and {@link common.customWriter.CustomBaseXMLWriter} needs it.
 */
public class CustomRelation<T> {
    private final Map<T, Set<T>> rows;
    private final Map<T, Set<T>> cols;
    private final Set<T> index;

    public CustomRelation() {
        rows = new HashMap<>();
        cols = new HashMap<>();
        index = new HashSet<>();
    }

    synchronized public void set(T a, T b) {
        index.add(a);
        index.add(b);
        innerAdd(rows, a, b);
        innerAdd(cols, b, a);
    }

    synchronized public void set11(T a, T b) {
        clearX(a, forward(a));
        clearX(backward(b), b);
        set(a, b);
    }

    synchronized public void set1N(T a, T b) {
        clearX(backward(b), b);
        set(a, b);
    }

    synchronized public void setN1(T a, T b) {
        clearX(a, forward(a));
        set(a, b);
    }

    synchronized public void setNN(T a, T b) {
        set(a, b);
    }

    synchronized public void clear(T a, T b) {
        innerClear(rows, a, b);
        innerClear(cols, b, a);
    }

    private void clearX(Set<T> s, T b) {
        if (s == null)
            return;
        s.forEach(value -> clear(value, b));
    }

    private void clearX(T a, Set<T> s) {
        if (s == null)
            return;
        s.forEach(value -> clear(a, value));
    }

    private static <T> void innerAdd(Map<T, Set<T>> s, T a, T b) {
        s.computeIfAbsent(a, k -> new HashSet<>()).add(b);
    }

    private static <T> void innerClear(Map<T, Set<T>> s, T a, T b) {
        Set<T> vals = s.get(a);
        if (vals != null) {
            vals.remove(b);
            if (vals.isEmpty()) {
                s.remove(a);
            }
        }
    }

    public boolean get(T a, T b) {
        Set<T> vals = rows.get(a);
        return vals != null && vals.contains(b);
    }

    synchronized public void transitiveClosure() {
        for (T oj : index) {
            Set<T> si = cols.get(oj);
            Set<T> sk = rows.get(oj);
            if (si != null && sk != null) {
                for (T oi : new HashSet<>(si)) {
                    if (!oi.equals(oj)) {
                        for (T ok : new HashSet<>(sk)) {
                            if (!ok.equals(oj)) {
                                set(oi, ok);
                            }
                        }
                    }
                }
            }
        }
    }

    synchronized public Set<T> getDiagonal() {
        Set<T> rslt = new HashSet<>();
        for (T o : index) {
            if (get(o, o)) {
                rslt.add(o);
            }
        }
        return rslt;
    }

    synchronized public CustomRelation<T> copy() {
        CustomRelation<T> rslt = new CustomRelation<>();
        iterator().forEachRemaining(e -> rslt.set(e.getKey(), e.getValue()));
        return rslt;
    }

    public Set<T> forward(T a) {
        return rows.get(a);
    }

    public Set<T> backward(T b) {
        return cols.get(b);
    }

    private static <T> Iterator<Map.Entry<T, T>> pairEntry(Map.Entry<T, Set<T>> pair) {
        T a = pair.getKey();
        Set<T> bs = pair.getValue();
        return new Map1Iterator<>(b -> new AbstractMap.SimpleEntry<>(a, b), bs.iterator());
    }

    public ExtendedIterator<Map.Entry<T, T>> iterator() {
        Map1Iterator<Map.Entry<T, Set<T>>, Iterator<Map.Entry<T, T>>> iter1 =
                new Map1Iterator<>(entry -> pairEntry(entry), rows.entrySet().iterator());
        return WrappedIterator.createIteratorIterator(iter1);
    }
}
