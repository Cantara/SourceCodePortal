package no.cantara.docsite.cache;

import java.util.Map;
import java.util.Set;

public interface CacheService<K,V> {

    V get(K key);

    Set<K> keySet();

    Map<K,V> entrySet();

}
