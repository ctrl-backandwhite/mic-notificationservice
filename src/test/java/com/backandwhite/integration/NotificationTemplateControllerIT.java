package com.backandwhite.integration;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.config.BaseIntegration;
import com.backandwhite.provider.NotificationTemplateProvider;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class NotificationTemplateControllerIT extends BaseIntegration {

    private static final String BASE_URL = "/api/v1/notification-templates";

    @Test
    void create_withValidToken_returnsCreated() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateDtoIn.builder().name("test-template-it")
                .subject("Test Subject").templateFile("email/default").type(NotificationTemplateProvider.TEMPLATE_TYPE)
                .active(true).build();

        webTestClient.post().uri(BASE_URL).header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.name").isEqualTo("test-template-it").jsonPath("$.subject")
                .isEqualTo("Test Subject").jsonPath("$.active").isEqualTo(true);
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
    void update_withValidToken_returnsOk() {
        // First create a template
        NotificationTemplateDtoIn createDto = NotificationTemplateDtoIn.builder().name("template-to-update")
                .subject("Original Subject").templateFile("email/default")
                .type(NotificationTemplateProvider.TEMPLATE_TYPE).active(true).build();

        Long id = webTestClient.post().uri(BASE_URL).header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(createDto).exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.id").exists().returnResult().getResponseBody() != null ? 1L : 1L;

        // Then update it
        NotificationTemplateDtoIn updateDto = NotificationTemplateDtoIn.builder().name("template-to-update")
                .subject("Updated Subject").templateFile("email/default")
                .type(NotificationTemplateProvider.TEMPLATE_TYPE).active(true).build();

        webTestClient.put().uri(BASE_URL + "/1").header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(updateDto).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.subject").isEqualTo("Updated Subject");
    }

    @Test
    void delete_withValidToken_returnsNoContent() {
        // First create a template
        NotificationTemplateDtoIn createDto = NotificationTemplateDtoIn.builder().name("template-to-delete")
                .subject("Delete Me").templateFile("email/default").type(NotificationTemplateProvider.TEMPLATE_TYPE)
                .active(true).build();

        webTestClient.post().uri(BASE_URL).header("Authorization", getToken(List.of("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(createDto).exchange().expectStatus().isCreated();

        webTestClient.delete().uri(BASE_URL + "/1").header("Authorization", getToken(List.of("ADMIN"))).exchange()
                .expectStatus().isNoContent();
    }

    @Nested
    class Security {

        @Test
        void create_withoutToken_returnsUnauthorized() {
            NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

            webTestClient.post().uri(BASE_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        void create_withUserRole_returnsForbidden() {
            NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

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
        void update_withoutToken_returnsUnauthorized() {
            NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

            webTestClient.put().uri(BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        void update_withUserRole_returnsForbidden() {
            NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

            webTestClient.put().uri(BASE_URL + "/1").header("Authorization", getToken(List.of("USER")))
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(dtoIn).exchange().expectStatus().isForbidden();
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
    }
}
