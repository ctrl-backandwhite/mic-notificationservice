package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.NotificationTemplateProvider.otherTemplate;
import static com.backandwhite.provider.NotificationTemplateProvider.template;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.mapper.NotificationTemplateUpdateMapper;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.repository.NotificationTemplateRepository;
import com.backandwhite.provider.NotificationTemplateProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateUseCaseImplTest {

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private NotificationTemplateUpdateMapper notificationTemplateUpdateMapper;

    @InjectMocks
    private NotificationTemplateUseCaseImpl notificationTemplateUseCase;

    @Test
    void save_validTemplate_delegatesToRepository() {
        NotificationTemplate input = template().withId(null);
        NotificationTemplate saved = template();

        when(notificationTemplateRepository.save(any(NotificationTemplate.class))).thenReturn(saved);

        NotificationTemplate result = notificationTemplateUseCase.save(input);

        assertSame(saved, result);
        verify(notificationTemplateRepository).save(input);
    }

    @Test
    void findAll_returnsRepositoryList() {
        List<NotificationTemplate> templates = List.of(template(), otherTemplate());

        when(notificationTemplateRepository.findAll()).thenReturn(templates);

        List<NotificationTemplate> result = notificationTemplateUseCase.findAll();

        assertSame(templates, result);
        verify(notificationTemplateRepository).findAll();
    }

    @Test
    void getById_existingTemplate_returnsTemplate() {
        NotificationTemplate model = template();

        when(notificationTemplateRepository.getById(NotificationTemplateProvider.TEMPLATE_ID)).thenReturn(model);

        NotificationTemplate result = notificationTemplateUseCase.getById(NotificationTemplateProvider.TEMPLATE_ID);

        assertSame(model, result);
        verify(notificationTemplateRepository).getById(NotificationTemplateProvider.TEMPLATE_ID);
    }

    @Test
    void getById_missingTemplate_throwsEntityNotFound() {
        when(notificationTemplateRepository.getById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> notificationTemplateUseCase.getById(99L));
    }

    @Test
    void delete_existingTemplate_delegatesToRepository() {
        when(notificationTemplateRepository.getById(NotificationTemplateProvider.TEMPLATE_ID)).thenReturn(template());

        notificationTemplateUseCase.delete(NotificationTemplateProvider.TEMPLATE_ID);

        verify(notificationTemplateRepository).delete(NotificationTemplateProvider.TEMPLATE_ID);
    }

    @Test
    void findByName_existingTemplate_returnsOptional() {
        when(notificationTemplateRepository.findByName(NotificationTemplateProvider.TEMPLATE_NAME))
                .thenReturn(Optional.of(template()));

        Optional<NotificationTemplate> result = notificationTemplateUseCase
                .findByName(NotificationTemplateProvider.TEMPLATE_NAME);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(NotificationTemplateProvider.TEMPLATE_NAME);
    }

    @Test
    void findByName_missingTemplate_returnsEmpty() {
        when(notificationTemplateRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        Optional<NotificationTemplate> result = notificationTemplateUseCase.findByName("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void delete_missingTemplate_throwsEntityNotFound() {
        when(notificationTemplateRepository.getById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> notificationTemplateUseCase.delete(99L));
    }

    @Test
    void update_existingTemplate_appliesMapperAndPersists() {
        NotificationTemplate existing = template();
        NotificationTemplate patch = otherTemplate();
        NotificationTemplate updated = otherTemplate();

        when(notificationTemplateRepository.getById(NotificationTemplateProvider.TEMPLATE_ID)).thenReturn(existing);
        when(notificationTemplateRepository.update(existing)).thenReturn(updated);

        NotificationTemplate result = notificationTemplateUseCase.update(patch,
                NotificationTemplateProvider.TEMPLATE_ID);

        assertSame(updated, result);
        verify(notificationTemplateUpdateMapper).updateFromModel(patch, existing);
        verify(notificationTemplateRepository).update(existing);
    }

    @Test
    void update_missingTemplate_throwsEntityNotFound() {
        NotificationTemplate patch = template();
        when(notificationTemplateRepository.getById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> notificationTemplateUseCase.update(patch, 99L));
    }
}
