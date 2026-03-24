package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepositoryAdapter extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByStatus(NotificationStatus status);

    List<NotificationEntity> findByRecipient(String recipient);
}
