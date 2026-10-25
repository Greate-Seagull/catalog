package com.hungvers.services.catalog.infra.projection.jooq;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.function.Function;

public final class JooqCondition {
    private JooqCondition() {}

    public static Condition ilike(Field<String> f, String v) {
        return (v == null || v.isBlank()) ? DSL.noCondition() : f.containsIgnoreCase(v);
    }

    public static <T> Condition skipWhen(Function<T, Condition> cond, T value) {
        return value == null ? DSL.noCondition() : cond.apply(value);
    }

    public static Condition skipWhenEnum(Function<String, Condition> cond, Enum<?> value) {
        return value == null ? DSL.noCondition() : cond.apply(value.name());
    }
}
