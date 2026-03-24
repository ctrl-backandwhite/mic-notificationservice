package com.backandwhite.integration;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.config.BaseIntegration;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

class NotificationTemplateControllerIT extends BaseIntegration {

    private static final String BASE_URL = "/api/v1/notification-templates";

    @Test
    void create_withValidToken_returnsCreated() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateDtoIn.builder()
                .name("test-template-it")
                .subject("Test Subject")
                .templateFile("email/default")
                .type(NotificationTemplateProvider.TEMPLATE_TYPE)
                .active(true)
                .build();

        webTestClient.post()
                .uri(BASE_URL)
                .header("Authorization", getToken(List.of("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dtoIn)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("test-template-it")
                .jsonPath("$.subject").isEqualTo("Test Subject")
                .jsonPath("$.active").isEqualTo(true);
    }

    @Test
    void create_withoutToken_returnsUnauthorized() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

        webTestClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dtoIn)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void findAll_withValidToken_returnsOk() {
        webTestClient.get()
                .uri(BASE_URL)
                .header("Authorization", getToken(List.of("ROLE_ADMIN")))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class);
    }

    @Test
    void getById_notFound_returns404() {
        webTestClient.get()
                .uri(BASE_URL + "/9999")
                .header("Authorization", getToken(List.of("ROLE_ADMIN")))
                .exchange()
                .expectStatus().isNotFound();
    }
}
