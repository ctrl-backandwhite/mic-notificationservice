package com.backandwhite.application.usecase;

import com.backandwhite.common.application.BaseUseCase;
import com.backandwhite.domain.model.NotificationTemplate;

import java.util.Optional;

public interface NotificationTemplateUseCase extends BaseUseCase<NotificationTemplate, NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByName(String name);
}
