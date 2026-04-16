package com.backandwhite.application.handler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backandwhite.common.exception.ArgumentException;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NotificationCommandHandlerTest {

    private final NotificationCommandHandler handler = new NotificationCommandHandler();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void validate_invalidRecipient_throwsArgumentException(String recipient) {
        Notification notification = Notification.builder().recipient(recipient).type(NotificationType.EMAIL).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("recipient");
    }

    @Test
    void validate_nullType_throwsArgumentException() {
        Notification notification = Notification.builder().recipient("user@test.com").type(null).build();

        assertThatThrownBy(() -> handler.validate(notification)).isInstanceOf(ArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void validate_validNotification_doesNotThrow() {
        Notification notification = Notification.builder().recipient("user@test.com").type(NotificationType.EMAIL)
                .build();

        assertThatCode(() -> handler.validate(notification)).doesNotThrowAnyException();
    }
}
