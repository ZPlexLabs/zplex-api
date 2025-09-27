package zechs.zplex.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void put(K key, V value, long duration, TimeUnit unit) {
        cache.put(key, value);
        scheduler.schedule(() -> cache.remove(key), duration, unit);
    }

    public V get(K key) {
        return cache.get(key);
    }
}
