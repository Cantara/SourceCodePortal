package no.cantara.docsite.domain.config;

import java.lang.reflect.InvocationTargetException;

public interface ExternalBuilder<T> {

    static ExternalBuilder<?> create(Class<? extends ExternalBuilder<?>> clazz) {
        try {
            return clazz.getDeclaredConstructor(null).newInstance(null);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    String getConfigKey(); // the key that is found in the repo config.json

    ExternalBuilder<T> set(String key, String value);

    T build();
}

