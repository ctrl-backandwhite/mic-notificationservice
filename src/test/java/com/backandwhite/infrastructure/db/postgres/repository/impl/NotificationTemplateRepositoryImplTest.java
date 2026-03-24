package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.NotificationTemplateEntityMapper;
import com.backandwhite.infrastructure.db.postgres.repository.NotificationTemplateJpaRepositoryAdapter;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateRepositoryImplTest {

    @Mock
    private NotificationTemplateEntityMapper notificationTemplateEntityMapper;

    @Mock
    private NotificationTemplateJpaRepositoryAdapter notificationTemplateJpaRepositoryAdapter;

    @InjectMocks
    private NotificationTemplateRepositoryImpl notificationTemplateRepository;

    @Test
    void save_persistsAndReturnsDomain() {
        NotificationTemplate domain = NotificationTemplateProvider.template();
        NotificationTemplateEntity entity = NotificationTemplateProvider.templateEntity();

        when(notificationTemplateEntityMapper.toEntity(domain)).thenReturn(entity);
        when(notificationTemplateJpaRepositoryAdapter.save(entity)).thenReturn(entity);
        when(notificationTemplateEntityMapper.toDomain(entity)).thenReturn(domain);

        NotificationTemplate result = notificationTemplateRepository.save(domain);

        assertThat(result).isSameAs(domain);
        verify(notificationTemplateJpaRepositoryAdapter).save(entity);
    }

    @Test
    void findAll_returnsMappedList() {
        List<NotificationTemplateEntity> entities = List.of(NotificationTemplateProvider.templateEntity());
        List<NotificationTemplate> domains = List.of(NotificationTemplateProvider.template());

        when(notificationTemplateJpaRepositoryAdapter.findAll()).thenReturn(entities);
        when(notificationTemplateEntityMapper.toDomainList(entities)).thenReturn(domains);

        List<NotificationTemplate> result = notificationTemplateRepository.findAll();

        assertThat(result).isSameAs(domains);
    }

    @Test
    void getById_existingEntity_returnsDomain() {
        NotificationTemplateEntity entity = NotificationTemplateProvider.templateEntity();
        NotificationTemplate domain = NotificationTemplateProvider.template();

        when(notificationTemplateJpaRepositoryAdapter.findById(NotificationTemplateProvider.TEMPLATE_ID))
                .thenReturn(Optional.of(entity));
        when(notificationTemplateEntityMapper.toDomain(entity)).thenReturn(domain);

        NotificationTemplate result = notificationTemplateRepository.getById(NotificationTemplateProvider.TEMPLATE_ID);

        assertThat(result).isSameAs(domain);
    }

    @Test
    void delete_delegatesToAdapter() {
        notificationTemplateRepository.delete(NotificationTemplateProvider.TEMPLATE_ID);
        verify(notificationTemplateJpaRepositoryAdapter).deleteById(NotificationTemplateProvider.TEMPLATE_ID);
    }

    @Test
    void findByName_existingTemplate_returnsMapped() {
        NotificationTemplateEntity entity = NotificationTemplateProvider.templateEntity();
        NotificationTemplate domain = NotificationTemplateProvider.template();

        when(notificationTemplateJpaRepositoryAdapter.findByName(NotificationTemplateProvider.TEMPLATE_NAME))
                .thenReturn(Optional.of(entity));
        when(notificationTemplateEntityMapper.toDomain(entity)).thenReturn(domain);

        Optional<NotificationTemplate> result = notificationTemplateRepository
                .findByName(NotificationTemplateProvider.TEMPLATE_NAME);

        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findByName_missingTemplate_returnsEmpty() {
        when(notificationTemplateJpaRepositoryAdapter.findByName("nonexistent"))
                .thenReturn(Optional.empty());

        Optional<NotificationTemplate> result = notificationTemplateRepository.findByName("nonexistent");

        assertThat(result).isEmpty();
    }
}
