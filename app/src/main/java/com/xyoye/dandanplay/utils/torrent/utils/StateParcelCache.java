package com.xyoye.dandanplay.utils.torrent.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StateParcelCache<T extends AbstractStateParcel> {
    private ConcurrentHashMap<String, T> cache = new ConcurrentHashMap<>();

    public void put(T state) {
        if (state == null)
            return;

        cache.put(state.parcelId, state);
    }

    public void putAll(Collection<T> states) {
        if (states == null)
            return;

        for (T state : states)
            cache.put(state.parcelId, state);
    }

    public void remove(String parcelId) {
        cache.remove(parcelId);
    }

    public void removeAll(Collection<T> states) {
        Set<String> keys = new HashSet<>(states.size());

        for (T state : states)
            keys.add(state.parcelId);

        cache.keySet().removeAll(keys);
    }

    public T get(String key) {
        return cache.get(key);
    }

    public List<T> getAll() {
        return new ArrayList<>(cache.values());
    }

    public boolean contains(String parcelId) {
        return cache.containsKey(parcelId);
    }

    public boolean contains(T state) {
        return cache.containsValue(state);
    }

    public boolean containsAll(List<T> states) {
        return cache.values().containsAll(states);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    @Override
    public String toString() {
        return "StateParcelCache{" +
                "cache=" + cache +
                '}';
    }
}
