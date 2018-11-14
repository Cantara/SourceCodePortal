package no.cantara.docsite.cache;

import no.ssb.config.DynamicConfiguration;
import no.ssb.config.StoreBasedDynamicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.cantara.docsite.executor.ExecutorService.WAIT_FOR_TERMINATION;
import static org.testng.Assert.assertNotNull;

// https://www.baeldung.com/jcache
// https://github.com/eugenp/tutorials/tree/master/libraries-data/src/main/java/com/baeldung/jcache
// https://github.com/eugenp/tutorials/tree/master/libraries-data/src/test/java/com/baeldung/jcache

public class EvctionCache {

    private static final Logger LOG = LoggerFactory.getLogger(EvctionCache.class);

    static DynamicConfiguration configuration() {
        return new StoreBasedDynamicConfiguration.Builder()
                .propertiesResource("application-defaults.properties")
                .propertiesResource("security.properties")
                .propertiesResource("application.properties")
                .build();
    }

    static CacheStore cacheStore(DynamicConfiguration configuration) {
        return CacheInitializer.initialize(configuration);
    }

    public static class CreatedCacheEntryListener implements CacheEntryCreatedListener<String, String>, Serializable {
        private final CacheObserver cacheObserver;

        public CreatedCacheEntryListener(CacheObserver cacheObserver) {
            this.cacheObserver = cacheObserver;
        }

        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents) throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends String> event : cacheEntryEvents) {
                Cache<String, String> cache = event.getSource();
                LOG.trace("Created: {}", event.getValue());
                cacheObserver.queue(cache);
            }
        }

        private <E> int comparator(E e, E e1) {
            return 0;
        }
    }

    public static class FilterCacheEntryListener implements CacheEntryEventFilter<String, String> {
        @Override
        public boolean evaluate(CacheEntryEvent<? extends String, ? extends String> event) throws CacheEntryListenerException {
            LOG.trace("filter: {} -- {} -- {} -- {}", event.getEventType(), event.getSource(), event.getOldValue(), event.getValue());
//            Set<String> evictionKeys = evictionKeys(event.getSource());
//            evictionKeys.forEach(key -> System.out.println("remove: " + key));
            return true;
        }
    }

    static Set<String> evictionKeys(Cache<String,String> cache) {
        Set<String> evictionKeys = new LinkedHashSet<>();
        long evictOnMaxEntries = Long.valueOf(cache.getCacheManager().getProperties().getProperty("commit.cache.max.entries"));
        long cacheSize = CacheHelper.cacheSize(cache);
        if (evictOnMaxEntries < cacheSize) {
            TreeSet<String> sortedKeys = new TreeSet<>(Comparator.comparing(String::toString, Comparator.reverseOrder()));
            cache.forEach(entry -> sortedKeys.add(entry.getKey()));
            evictionKeys = sortedKeys.stream().limit(cacheSize - evictOnMaxEntries).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return evictionKeys;
    }

    static class CacheObserver {
        final BlockingQueue<Cache<String,String>> createdKeys = new ArrayBlockingQueue<>(500);
        final java.util.concurrent.ExecutorService executorService = Executors.newFixedThreadPool(1);

        public CacheObserver() {
        }

        public void queue(Cache<String,String> cache) {
            createdKeys.add(cache);
        }

        public void start() {
            executorService.execute(() -> {
                while (!executorService.isTerminated()) {
                    try {
                        Cache<String, String> cache = createdKeys.take();

                        executorService.execute(() -> {
                            Set<String> keys = evictionKeys(cache);
                            keys.forEach(key -> {
                                if (cache.containsKey(key)) {
                                    cache.remove(key);
                                }
                            });
                        });

                    } catch (InterruptedException e) {
                    }
                }
            });
        }

        public void shutdown() {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(WAIT_FOR_TERMINATION, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
                LOG.info("ExecutorService shutdown success");
            } catch (InterruptedException e) {
                LOG.error("ExecutorService shutdown failed", e);
            }
        }
    }

    @Ignore
    @Test
    public void testName() {
        DynamicConfiguration configuration = configuration();

        CacheObserver cacheObserver = new CacheObserver();
        cacheObserver.start();

        CachingProvider cachingProvider = Caching.getCachingProvider();
        LOG.info("Initializing JCache ScmProvider: {}", cachingProvider.getDefaultURI());

        CacheManager cacheManager = cachingProvider.getCacheManager();


        MutableConfiguration<String, String> cacheConfig = new MutableConfiguration<>();

        CreatedCacheEntryListener createdCacheEntryListener = new CreatedCacheEntryListener(cacheObserver);

        Factory<CreatedCacheEntryListener> changesCacheEntryListenerFactory = FactoryBuilder.factoryOf(createdCacheEntryListener);
        Factory<FilterCacheEntryListener> filterCacheEntryListenerFactory = FactoryBuilder.factoryOf(FilterCacheEntryListener.class);

        MutableCacheEntryListenerConfiguration<String, String> entryListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                changesCacheEntryListenerFactory, filterCacheEntryListenerFactory, false, false);
//        cacheConfig.addCacheEntryListenerConfiguration(entryListenerConfiguration);


        cacheManager.getProperties().put("commit.cache.max.entries", configuration.evaluateToString("commit.cache.max.entries"));

        cacheConfig.setTypes(String.class, String.class);
        Cache<String, String> createdCache = cacheManager.createCache("evictionCache", cacheConfig);

        createdCache.registerCacheEntryListener(entryListenerConfiguration);

        Cache<String, String> cache = cacheManager.getCache("evictionCache");
//        cache.registerCacheEntryListener(entryListenerConfiguration);
        assertNotNull(cache);

        cache.put("/foo/1", "bar1");
        cache.put("/foo/2", "bar2");
        cache.put("/foo/3", "bar3");
        cache.put("/foo/4", "bar4");
        cache.put("/foo/5", "bar5");

        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cacheObserver.shutdown();

        cache.iterator().forEachRemaining(c -> System.out.println(c.getKey()));


    }

}
