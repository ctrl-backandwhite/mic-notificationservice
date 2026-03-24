package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.NotificationTemplate;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository {

    NotificationTemplate save(NotificationTemplate template);

    List<NotificationTemplate> findAll();

    NotificationTemplate getById(Long id);

    NotificationTemplate update(NotificationTemplate template);

    void delete(Long id);

    Optional<NotificationTemplate> findByName(String name);
}
