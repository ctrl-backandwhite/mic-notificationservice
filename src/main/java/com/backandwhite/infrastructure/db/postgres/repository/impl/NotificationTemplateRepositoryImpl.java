package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.repository.NotificationTemplateRepository;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.NotificationTemplateEntityMapper;
import com.backandwhite.infrastructure.db.postgres.repository.NotificationTemplateJpaRepositoryAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.backandwhite.common.exception.Message.ENTITY_NOT_FOUND;

@Log4j2
@Repository
@AllArgsConstructor
public class NotificationTemplateRepositoryImpl implements NotificationTemplateRepository {

    private final NotificationTemplateEntityMapper notificationTemplateEntityMapper;
    private final NotificationTemplateJpaRepositoryAdapter notificationTemplateJpaRepositoryAdapter;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        NotificationTemplateEntity entity = notificationTemplateJpaRepositoryAdapter.save(
                notificationTemplateEntityMapper.toEntity(template));
        return notificationTemplateEntityMapper.toDomain(entity);
    }

    @Override
    public List<NotificationTemplate> findAll() {
        return notificationTemplateEntityMapper.toDomainList(
                notificationTemplateJpaRepositoryAdapter.findAll());
    }

    @Override
    public NotificationTemplate getById(Long id) {
        NotificationTemplateEntity entity = notificationTemplateJpaRepositoryAdapter.findById(id).orElse(null);
        if (Objects.isNull(entity)) {
            ENTITY_NOT_FOUND.toEntityNotFound("NotificationTemplate", id);
        }
        return notificationTemplateEntityMapper.toDomain(entity);
    }

    @Override
    public NotificationTemplate update(NotificationTemplate template) {
        return this.save(template);
    }

    @Override
    public void delete(Long id) {
        notificationTemplateJpaRepositoryAdapter.deleteById(id);
    }

    @Override
    public Optional<NotificationTemplate> findByName(String name) {
        return notificationTemplateJpaRepositoryAdapter.findByName(name)
                .map(notificationTemplateEntityMapper::toDomain);
    }
}
