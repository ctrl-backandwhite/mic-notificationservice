package com.backandwhite.application.usecase;

import com.backandwhite.common.application.BaseUseCase;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;

import java.util.List;

public interface NotificationUseCase extends BaseUseCase<Notification, Notification, Long> {

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipient(String recipient);
}
