package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepositoryAdapter extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByStatus(NotificationStatus status);

    List<NotificationEntity> findByRecipient(String recipient);

    List<NotificationEntity> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
