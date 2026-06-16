package com.electrocorp.electrocorpplatform.notifications.infrastructure.delivery;

import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.Alert;
import com.electrocorp.electrocorpplatform.notifications.domain.model.aggregates.NotificationPreference;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationDeliveryAdapter implements NotificationDeliveryPort {

    @Override
    public void send(Alert alert, NotificationPreference preference) {
        if (alert == null) {
            return;
        }

        System.out.println("[ElectroCorp Notification] " + alert.getLevel() + " - " + alert.getTitle());
    }
}