package com.backandwhite;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backandwhite.config.BaseIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

class TestMicNotificationserviceApplicationTest extends BaseIntegration {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should have loaded");
    }
}
