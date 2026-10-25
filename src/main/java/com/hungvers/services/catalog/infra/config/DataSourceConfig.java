package com.hungvers.services.catalog.infra.config;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager primaryTxManager(
            @Qualifier("primaryDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    @Primary
    public DSLContext primaryDsl(@Qualifier("primaryDataSource") DataSource ds) {
        return DSL.using(new DefaultConfiguration()
                .set(new TransactionAwareDataSourceProxy(ds))
                .set(SQLDialect.POSTGRES)
        );
    }

    @Bean
    public DSLContext replicaDsl(@Qualifier("replicaDataSource") DataSource ds) {
        return DSL.using(new DefaultConfiguration()
                .set(new TransactionAwareDataSourceProxy(ds))
                .set(SQLDialect.POSTGRES)
        );
    }
}
