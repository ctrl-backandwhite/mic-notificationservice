package com.backandwhite.domain.port;

import com.backandwhite.domain.model.Notification;

public interface NotificationSender {

    void send(Notification notification);
}
