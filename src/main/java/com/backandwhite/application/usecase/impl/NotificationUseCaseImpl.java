package com.backandwhite.application.usecase.impl;

import static com.backandwhite.common.exception.Message.ENTITY_NOT_FOUND;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.mapper.NotificationUpdateMapper;
import com.backandwhite.application.usecase.NotificationUseCase;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.repository.NotificationRepository;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@AllArgsConstructor
public class NotificationUseCaseImpl implements NotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationCommandHandler notificationCommandHandler;
    private final NotificationUpdateMapper notificationUpdateMapper;

    @Override
    @Transactional
    public Notification save(Notification model) {
        log.debug("::> Creating notification");
        notificationCommandHandler.validate(model);
        return notificationRepository.save(model);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        log.debug("::> Getting all notifications");
        return notificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getById(Long id) {
        log.debug("::> Getting notification with id {}", id);
        Notification model = notificationRepository.getById(id);
        if (Objects.isNull(model)) {
            throw ENTITY_NOT_FOUND.toEntityNotFound("Notification", id);
        }
        return model;
    }

    @Override
    @Transactional
    public Notification update(Notification model, Long id) {
        log.debug("::> Updating notification {}", id);
        Notification existing = findExistingById(id);
        notificationUpdateMapper.updateFromModel(model, existing);
        notificationCommandHandler.validate(existing);
        return notificationRepository.update(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findExistingById(id);
        log.debug("::> Deleting notification with id {}", id);
        notificationRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByStatus(NotificationStatus status) {
        log.debug("::> Getting notifications with status {}", status);
        return notificationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByRecipient(String recipient) {
        log.debug("::> Getting notifications by recipient");
        return notificationRepository.findByRecipient(recipient);
    }

    private Notification findExistingById(Long id) {
        Notification model = notificationRepository.getById(id);
        if (Objects.isNull(model)) {
            throw ENTITY_NOT_FOUND.toEntityNotFound("Notification", id);
        }
        return model;
    }
}
