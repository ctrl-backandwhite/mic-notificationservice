package com.backandwhite;

import com.backandwhite.config.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestMicNotificationserviceApplication {

    public static void main(String[] args) {
        SpringApplication.from(MicNotificationserviceApplication::main)
                .with(TestContainersConfiguration.class)
                .run(args);
    }
}
