package com.backandwhite.application.usecase.impl;

import static com.backandwhite.common.exception.Message.ENTITY_NOT_FOUND;

import com.backandwhite.application.mapper.NotificationTemplateUpdateMapper;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.repository.NotificationTemplateRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@AllArgsConstructor
public class NotificationTemplateUseCaseImpl implements NotificationTemplateUseCase {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTemplateUpdateMapper notificationTemplateUpdateMapper;

    @Override
    @Transactional
    public NotificationTemplate save(NotificationTemplate model) {
        log.debug("::> Creating notification template: {}", model.getName());
        return notificationTemplateRepository.save(model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> findAll() {
        log.debug("::> Getting all notification templates");
        return notificationTemplateRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getById(Long id) {
        log.debug("::> Getting notification template with id {}", id);
        NotificationTemplate model = notificationTemplateRepository.getById(id);
        if (Objects.isNull(model)) {
            throw ENTITY_NOT_FOUND.toEntityNotFound("NotificationTemplate", id);
        }
        return model;
    }

    @Override
    @Transactional
    public NotificationTemplate update(NotificationTemplate model, Long id) {
        log.debug("::> Updating notification template {}", id);
        NotificationTemplate existing = findExistingById(id);
        notificationTemplateUpdateMapper.updateFromModel(model, existing);
        return notificationTemplateRepository.update(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findExistingById(id);
        log.debug("::> Deleting notification template with id {}", id);
        notificationTemplateRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationTemplate> findByName(String name) {
        log.debug("::> Getting notification template by name: {}", name);
        return notificationTemplateRepository.findByName(name);
    }

    private NotificationTemplate findExistingById(Long id) {
        NotificationTemplate model = notificationTemplateRepository.getById(id);
        if (Objects.isNull(model)) {
            throw ENTITY_NOT_FOUND.toEntityNotFound("NotificationTemplate", id);
        }
        return model;
    }
}
