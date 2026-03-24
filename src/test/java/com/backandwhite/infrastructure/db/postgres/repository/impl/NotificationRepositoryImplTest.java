package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.NotificationEntityMapper;
import com.backandwhite.infrastructure.db.postgres.repository.NotificationJpaRepositoryAdapter;
import com.backandwhite.provider.NotificationProvider;
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
class NotificationRepositoryImplTest {

    @Mock
    private NotificationEntityMapper notificationEntityMapper;

    @Mock
    private NotificationJpaRepositoryAdapter notificationJpaRepositoryAdapter;

    @InjectMocks
    private NotificationRepositoryImpl notificationRepository;

    @Test
    void save_persistsAndReturnsDomain() {
        Notification domain = NotificationProvider.notification();
        NotificationEntity entity = NotificationProvider.notificationEntity();
        NotificationEntity saved = NotificationProvider.notificationEntity();

        when(notificationEntityMapper.toEntity(domain)).thenReturn(entity);
        when(notificationJpaRepositoryAdapter.save(entity)).thenReturn(saved);
        when(notificationEntityMapper.toDomain(saved)).thenReturn(domain);

        Notification result = notificationRepository.save(domain);

        assertThat(result).isSameAs(domain);
        verify(notificationJpaRepositoryAdapter).save(entity);
    }

    @Test
    void findAll_returnsMappedList() {
        List<NotificationEntity> entities = List.of(NotificationProvider.notificationEntity());
        List<Notification> domains = List.of(NotificationProvider.notification());

        when(notificationJpaRepositoryAdapter.findAll()).thenReturn(entities);
        when(notificationEntityMapper.toDomainList(entities)).thenReturn(domains);

        List<Notification> result = notificationRepository.findAll();

        assertThat(result).isSameAs(domains);
    }

    @Test
    void getById_existingEntity_returnsDomain() {
        NotificationEntity entity = NotificationProvider.notificationEntity();
        Notification domain = NotificationProvider.notification();

        when(notificationJpaRepositoryAdapter.findById(NotificationProvider.NOTIFICATION_ID))
                .thenReturn(Optional.of(entity));
        when(notificationEntityMapper.toDomain(entity)).thenReturn(domain);

        Notification result = notificationRepository.getById(NotificationProvider.NOTIFICATION_ID);

        assertThat(result).isSameAs(domain);
    }

    @Test
    void delete_delegatesToAdapter() {
        notificationRepository.delete(NotificationProvider.NOTIFICATION_ID);
        verify(notificationJpaRepositoryAdapter).deleteById(NotificationProvider.NOTIFICATION_ID);
    }

    @Test
    void findByStatus_returnsMappedList() {
        List<NotificationEntity> entities = List.of(NotificationProvider.notificationEntity());
        List<Notification> domains = List.of(NotificationProvider.notification());

        when(notificationJpaRepositoryAdapter.findByStatus(NotificationStatus.SENT)).thenReturn(entities);
        when(notificationEntityMapper.toDomainList(entities)).thenReturn(domains);

        List<Notification> result = notificationRepository.findByStatus(NotificationStatus.SENT);

        assertThat(result).isSameAs(domains);
    }

    @Test
    void findByRecipient_returnsMappedList() {
        List<NotificationEntity> entities = List.of(NotificationProvider.notificationEntity());
        List<Notification> domains = List.of(NotificationProvider.notification());

        when(notificationJpaRepositoryAdapter.findByRecipient(NotificationProvider.RECIPIENT)).thenReturn(entities);
        when(notificationEntityMapper.toDomainList(entities)).thenReturn(domains);

        List<Notification> result = notificationRepository.findByRecipient(NotificationProvider.RECIPIENT);

        assertThat(result).isSameAs(domains);
    }
}
