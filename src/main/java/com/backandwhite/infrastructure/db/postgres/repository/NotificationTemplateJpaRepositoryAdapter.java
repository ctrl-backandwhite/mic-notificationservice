package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateJpaRepositoryAdapter extends JpaRepository<NotificationTemplateEntity, Long> {

    Optional<NotificationTemplateEntity> findByName(String name);
}
