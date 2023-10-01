import java.util.List;

interface RangeMap<K,V> {
    int size();
    boolean isEmpty();

    /**
     * insert new item into the map
     */
    void add(K key, V value);

    /**
     * check if a key is present
     */
    boolean contains(K key);

    /**
     * lookup a value by the key
     */
    V lookup(K key);

    /**
     * lookup values for a range of keys
     */
    List<V> lookupRange(K from, K to);

    /**
     * remove an item from a map
     */
    Object remove(K key);
}