package no.cantara.docsite.cache;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface CacheService<K,V> {

    V get(K key);

    Iterator<Cache.Entry<K,V>> getAll();

    Set<K> keySet();

    Map<K,V> entrySet();

}
