package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.NotificationEntityMapper;
import com.backandwhite.infrastructure.db.postgres.repository.NotificationJpaRepositoryAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.backandwhite.common.exception.Message.ENTITY_NOT_FOUND;

@Log4j2
@Repository
@AllArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationEntityMapper notificationEntityMapper;
    private final NotificationJpaRepositoryAdapter notificationJpaRepositoryAdapter;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = notificationJpaRepositoryAdapter.save(
                notificationEntityMapper.toEntity(notification));
        return notificationEntityMapper.toDomain(entity);
    }

    @Override
    public List<Notification> findAll() {
        return notificationEntityMapper.toDomainList(notificationJpaRepositoryAdapter.findAll());
    }

    @Override
    public Notification getById(Long id) {
        NotificationEntity entity = notificationJpaRepositoryAdapter.findById(id).orElse(null);
        if (Objects.isNull(entity)) {
            ENTITY_NOT_FOUND.toEntityNotFound("Notification", id);
        }
        return notificationEntityMapper.toDomain(entity);
    }

    @Override
    public Notification update(Notification notification) {
        return this.save(notification);
    }

    @Override
    public void delete(Long id) {
        notificationJpaRepositoryAdapter.deleteById(id);
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return notificationEntityMapper.toDomainList(
                notificationJpaRepositoryAdapter.findByStatus(status));
    }

    @Override
    public List<Notification> findByRecipient(String recipient) {
        return notificationEntityMapper.toDomainList(
                notificationJpaRepositoryAdapter.findByRecipient(recipient));
    }
}
