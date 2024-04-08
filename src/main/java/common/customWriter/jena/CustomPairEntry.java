package common.customWriter.jena;

import java.util.Map;

/**
 * Updated version of org.apache.jena.rdfxml.xmloutput.impl.PairEntry
 *
 * Has to be copied because the original class is package private and {@link common.customWriter.CustomBaseXMLWriter} needs it.
 */
public class CustomPairEntry<K, V> implements Map.Entry<K, V> {
    private final K key;
    private V value;

    public CustomPairEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map.Entry)) return false;

        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;

        if (key != null ? !key.equals(entry.getKey()) : entry.getKey() != null) return false;
        return value != null ? value.equals(entry.getValue()) : entry.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
