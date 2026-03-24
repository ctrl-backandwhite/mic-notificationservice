package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.api.mapper.NotificationTemplateDtoMapper;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateControllerTest {

    @Mock
    private NotificationTemplateDtoMapper mapper;
    @Mock
    private NotificationTemplateUseCase useCase;

    @InjectMocks
    private NotificationTemplateController controller;

    @Test
    void create_returnsCreated() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();
        NotificationTemplate domain = NotificationTemplateProvider.template();
        NotificationTemplateDtoOut dtoOut = NotificationTemplateProvider.templateDtoOut(
                NotificationTemplateProvider.TEMPLATE_ID);

        when(mapper.toDomain(dtoIn)).thenReturn(domain);
        when(useCase.save(domain)).thenReturn(domain);
        when(mapper.toDtoOut(domain)).thenReturn(dtoOut);

        ResponseEntity<NotificationTemplateDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
    }

    @Test
    void getById_returnsTemplate() {
        NotificationTemplate domain = NotificationTemplateProvider.template();
        NotificationTemplateDtoOut dtoOut = NotificationTemplateProvider.templateDtoOut(
                NotificationTemplateProvider.TEMPLATE_ID);

        when(useCase.getById(NotificationTemplateProvider.TEMPLATE_ID)).thenReturn(domain);
        when(mapper.toDtoOut(domain)).thenReturn(dtoOut);

        ResponseEntity<NotificationTemplateDtoOut> response = controller.getById(
                NotificationTemplateProvider.TEMPLATE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
    }

    @Test
    void findAll_returnsList() {
        List<NotificationTemplate> templates = List.of(NotificationTemplateProvider.template());
        List<NotificationTemplateDtoOut> dtoOuts = List.of(
                NotificationTemplateProvider.templateDtoOut(NotificationTemplateProvider.TEMPLATE_ID));

        when(useCase.findAll()).thenReturn(templates);
        when(mapper.toDtoOutList(templates)).thenReturn(dtoOuts);

        ResponseEntity<List<NotificationTemplateDtoOut>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(NotificationTemplateProvider.TEMPLATE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).delete(NotificationTemplateProvider.TEMPLATE_ID);
    }

    @Test
    void update_returnsUpdatedTemplate() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();
        NotificationTemplate domain = NotificationTemplateProvider.template();
        NotificationTemplateDtoOut dtoOut = NotificationTemplateProvider.templateDtoOut(
                NotificationTemplateProvider.TEMPLATE_ID);

        when(mapper.toDomain(dtoIn)).thenReturn(domain);
        when(useCase.update(domain, NotificationTemplateProvider.TEMPLATE_ID)).thenReturn(domain);
        when(mapper.toDtoOut(domain)).thenReturn(dtoOut);

        ResponseEntity<NotificationTemplateDtoOut> response = controller.update(dtoIn,
                NotificationTemplateProvider.TEMPLATE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
    }
}
