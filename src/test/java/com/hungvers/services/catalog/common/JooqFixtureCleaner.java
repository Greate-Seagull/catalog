package com.hungvers.services.catalog.common;

import com.hungvers.services.catalog.common.test.FixtureCleaner;
import com.hungvers.services.catalog.infra.jooq.Service;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.springframework.stereotype.Component;

@Component
public class JooqFixtureCleaner implements FixtureCleaner {
    private final DSLContext dsl;

    public JooqFixtureCleaner(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void cleanAll() {
        for (Table<?> t : Service.SERVICE.getTables()) {
            dsl.truncate(t).restartIdentity().cascade().execute();
        }
    }
}
