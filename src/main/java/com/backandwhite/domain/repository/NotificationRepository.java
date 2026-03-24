package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;

import java.util.List;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findAll();

    Notification getById(Long id);

    Notification update(Notification notification);

    void delete(Long id);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipient(String recipient);
}
