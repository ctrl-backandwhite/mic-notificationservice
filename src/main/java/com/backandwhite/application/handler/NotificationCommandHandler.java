package com.backandwhite.application.handler;

import static com.backandwhite.common.exception.Message.VALIDATION_ERROR;

import com.backandwhite.common.exception.ArgumentException;
import com.backandwhite.domain.model.Notification;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class NotificationCommandHandler {

    public void validate(Notification notification) {
        if (Objects.isNull(notification.getRecipient()) || notification.getRecipient().isBlank()) {
            throw new ArgumentException(VALIDATION_ERROR.getCode(),
                    "El destinatario de la notificación es obligatorio.");
        }
        if (Objects.isNull(notification.getType())) {
            throw new ArgumentException(VALIDATION_ERROR.getCode(), "El tipo de notificación es obligatorio.");
        }
        log.debug("::> Notification validated successfully");
    }
}
