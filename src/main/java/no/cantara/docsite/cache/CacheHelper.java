package no.cantara.docsite.cache;

import javax.cache.Cache;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheHelper {

    public static long cacheSize(Cache<?,?> cache) {
        try {
            if (cache.getClass().isAssignableFrom(Class.forName("org.jsr107.ri.RICache"))) {
                Method method = cache.getClass().getDeclaredMethod("getSize");
                return (long) method.invoke(cache);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }

        AtomicInteger count = new AtomicInteger(0);
        cache.iterator().forEachRemaining(a -> count.incrementAndGet());
        return count.get();
    }

}
