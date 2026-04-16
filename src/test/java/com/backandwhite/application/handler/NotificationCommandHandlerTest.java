package com.backandwhite.application.handler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backandwhite.common.exception.ArgumentException;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationType;
import org.junit.jupiter.api.Test;

class NotificationCommandHandlerTest {

    private final NotificationCommandHandler handler = new NotificationCommandHandler();

    @Test
    void validate_nullRecipient_throwsArgumentException() {
        Notification notification = Notification.builder().recipient(null).type(NotificationType.EMAIL).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("destinatario");
    }

    @Test
    void validate_blankRecipient_throwsArgumentException() {
        Notification notification = Notification.builder().recipient("   ").type(NotificationType.EMAIL).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("destinatario");
    }

    @Test
    void validate_emptyRecipient_throwsArgumentException() {
        Notification notification = Notification.builder().recipient("").type(NotificationType.EMAIL).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("destinatario");
    }

    @Test
    void validate_nullType_throwsArgumentException() {
        Notification notification = Notification.builder().recipient("user@test.com").type(null).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("tipo");
    }

    @Test
    void validate_validNotification_doesNotThrow() {
        Notification notification = Notification.builder().recipient("user@test.com").type(NotificationType.EMAIL)
                .build();

        assertThatCode(() -> handler.validate(notification)).doesNotThrowAnyException();
    }
}
