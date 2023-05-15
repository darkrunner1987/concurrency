package org.example;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class NumberProducer {
    private static final long LIMIT = 100;

    private final AtomicReference<Long> value;

    public NumberProducer() {
        value = new AtomicReference<>(2L);
    }

    @Nullable
    public Number produce() {
        return value.getAndUpdate(curr -> {
            if (curr == null || curr >= LIMIT) {
                return null;
            }
            return curr + 1;
        });
    }
}
