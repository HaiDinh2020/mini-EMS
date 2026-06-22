package com.vht.ems;

import com.vht.ems.config.AsyncSyncConfiguration;
import com.vht.ems.config.JacksonConfiguration;
import com.vht.ems.config.MongoDbTestContainer;
import com.vht.ems.config.RedisTestContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { EmsApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@ImportTestcontainers({ MongoDbTestContainer.class, RedisTestContainer.class })
public @interface IntegrationTest {}
