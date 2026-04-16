package com.backandwhite.integration;

import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.config.BaseIntegration;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.provider.NotificationTemplateProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class NotificationControllerIT extends BaseIntegration {

    private static final String BASE_URL = "/api/v1/notifications";
    private static final String TEMPLATES_URL = "/api/v1/notification-templates";

    private Long createTemplate() {
        NotificationTemplateDtoIn templateDto = NotificationTemplateDtoIn.builder().name("default-it")
                .subject("Default Subject").templateFile("email/default")
                .type(NotificationTemplateProvider.TEMPLATE_TYPE).active(true).build();

        byte[] body = webTestClient.post().uri(TEMPLATES_URL).header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(templateDto).exchange().expectStatus().isCreated()
                .expectBody().returnResult().getResponseBody();

        return body != null ? 1L : 1L;
    }

    @Test
    void create_withValidToken_returnsCreated() {
        createTemplate();

        NotificationDtoIn dtoIn = NotificationDtoIn.builder().recipient("test-it@example.com")
                .subject("IT Test Subject").type(NotificationType.EMAIL).templateId(1L)
                .variables(Map.of("subject", "Test", "body", "Integration test body")).build();

        webTestClient.post().uri(BASE_URL).header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.recipient").isEqualTo("test-it@example.com").jsonPath("$.type")
                .isEqualTo("EMAIL");
    }

    @Test
    void findAll_withValidToken_returnsOk() {
        webTestClient.get().uri(BASE_URL).header("Authorization", getToken(List.of("ADMIN"))).exchange().expectStatus()
                .isOk().expectBodyList(Object.class);
    }

    @Test
    void getById_notFound_returns404() {
        webTestClient.get().uri(BASE_URL + "/9999").header("Authorization", getToken(List.of("ADMIN"))).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findByStatus_withValidToken_returnsOk() {
        webTestClient.get().uri(BASE_URL + "/status/PENDING").header("Authorization", getToken(List.of("ADMIN")))
                .exchange().expectStatus().isOk().expectBodyList(Object.class);
    }

    @Test
    void findByRecipient_withValidToken_returnsOk() {
        webTestClient.get().uri(BASE_URL + "/recipient/test@example.com")
                .header("Authorization", getToken(List.of("ADMIN"))).exchange().expectStatus().isOk()
                .expectBodyList(Object.class);
    }

    @Nested
    class Security {

        @Test
        void create_withoutToken_returnsUnauthorized() {
            NotificationDtoIn dtoIn = NotificationDtoIn.builder().recipient("test@example.com").subject("Test")
                    .type(NotificationType.EMAIL).build();

            webTestClient.post().uri(BASE_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        void create_withUserRole_returnsForbidden() {
            NotificationDtoIn dtoIn = NotificationDtoIn.builder().recipient("test@example.com").subject("Test")
                    .type(NotificationType.EMAIL).build();

            webTestClient.post().uri(BASE_URL).header("Authorization", getToken(List.of("USER")))
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange().expectStatus().isForbidden();
        }

        @Test
        void findAll_withoutToken_returnsUnauthorized() {
            webTestClient.get().uri(BASE_URL).exchange().expectStatus().isUnauthorized();
        }

        @Test
        void findAll_withUserRole_returnsForbidden() {
            webTestClient.get().uri(BASE_URL).header("Authorization", getToken(List.of("USER"))).exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        void getById_withoutToken_returnsUnauthorized() {
            webTestClient.get().uri(BASE_URL + "/1").exchange().expectStatus().isUnauthorized();
        }

        @Test
        void delete_withoutToken_returnsUnauthorized() {
            webTestClient.delete().uri(BASE_URL + "/1").exchange().expectStatus().isUnauthorized();
        }

        @Test
        void delete_withUserRole_returnsForbidden() {
            webTestClient.delete().uri(BASE_URL + "/1").header("Authorization", getToken(List.of("USER"))).exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        void findByStatus_withoutToken_returnsUnauthorized() {
            webTestClient.get().uri(BASE_URL + "/status/PENDING").exchange().expectStatus().isUnauthorized();
        }

        @Test
        void findByStatus_withUserRole_returnsForbidden() {
            webTestClient.get().uri(BASE_URL + "/status/PENDING").header("Authorization", getToken(List.of("USER")))
                    .exchange().expectStatus().isForbidden();
        }

        @Test
        void findByRecipient_withoutToken_returnsUnauthorized() {
            webTestClient.get().uri(BASE_URL + "/recipient/test@example.com").exchange().expectStatus()
                    .isUnauthorized();
        }

        @Test
        void findByRecipient_withUserRole_returnsForbidden() {
            webTestClient.get().uri(BASE_URL + "/recipient/test@example.com")
                    .header("Authorization", getToken(List.of("USER"))).exchange().expectStatus().isForbidden();
        }
    }
}
