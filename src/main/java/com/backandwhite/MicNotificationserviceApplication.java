package com.backandwhite;

import com.backandwhite.common.configuration.annotation.EnableCoreApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;

@EnableCoreApplication
@OpenAPIDefinition(servers = {
        @Server(url = "https://mic-notificationservice-des.up.railway.app", description = "Production Server."),
        @Server(url = "http://localhost:6003", description = "Local Server.")
})
public class MicNotificationserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicNotificationserviceApplication.class, args);
    }
}
