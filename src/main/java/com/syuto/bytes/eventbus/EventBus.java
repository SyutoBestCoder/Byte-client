package com.syuto.bytes.eventbus;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Static — shared across all EventBus instances. A class is never scanned twice.
    private static final Map<Class<?>, CachedMethod[]> METHOD_CACHE = new ConcurrentHashMap<>();

    // Hot read path: plain array per event type, rebuilt only on register/unregister
    private final Map<Class<?>, BoundHandle[]> listenerMap = new ConcurrentHashMap<>();

    // Write path backing list — never touched by post()
    private final Map<Class<?>, List<BoundHandle>> mutableMap = new ConcurrentHashMap<>();

    private record CachedMethod(MethodHandle handle, Class<?> eventType) {}

    private record BoundHandle(Object target, MethodHandle handle) {
        void invoke(Object event) {
            try {
                handle.invoke(target, event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Scans a listener class exactly once, ever.
     * Every subsequent call for the same class is a single ConcurrentHashMap lookup.
     */
    private static CachedMethod[] scanClass(Class<?> clazz) {
        return METHOD_CACHE.computeIfAbsent(clazz, c -> {
            List<CachedMethod> found = new ArrayList<>();
            for (Method m : c.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(EventHandler.class)) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) continue;
                try {
                    m.setAccessible(true);
                    found.add(new CachedMethod(LOOKUP.unreflect(m), params[0]));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return found.toArray(new CachedMethod[0]);
        });
    }

    public void register(Object listener) {
        for (CachedMethod cm : scanClass(listener.getClass())) {
            mutableMap.computeIfAbsent(cm.eventType(), k -> new ArrayList<>())
                    .add(new BoundHandle(listener, cm.handle()));
            rebuildSnapshot(cm.eventType());
        }
    }

    public void unregister(Object listener) {
        CachedMethod[] methods = METHOD_CACHE.get(listener.getClass());
        if (methods == null) return;
        for (CachedMethod cm : methods) {
            List<BoundHandle> list = mutableMap.get(cm.eventType());
            if (list == null) continue;
            if (list.removeIf(bh -> bh.target() == listener)) {
                if (list.isEmpty()) {
                    mutableMap.remove(cm.eventType());
                    listenerMap.remove(cm.eventType());
                } else {
                    rebuildSnapshot(cm.eventType());
                }
            }
        }
    }

    public void post(@NotNull Event event) {
        BoundHandle[] handles = listenerMap.get(event.getClass());
        if (handles == null) return;
        for (BoundHandle bh : handles) {
            bh.invoke(event);
        }
    }

    private void rebuildSnapshot(Class<?> eventType) {
        List<BoundHandle> list = mutableMap.get(eventType);
        listenerMap.put(eventType, list.toArray(new BoundHandle[0]));
    }
}