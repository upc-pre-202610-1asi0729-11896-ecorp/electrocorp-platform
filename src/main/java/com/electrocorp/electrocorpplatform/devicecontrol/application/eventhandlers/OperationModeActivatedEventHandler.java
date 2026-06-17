package com.electrocorp.electrocorpplatform.devicecontrol.application.eventhandlers;

import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.events.OperationModeActivatedEvent;
import com.electrocorp.electrocorpplatform.notifications.application.services.NotificationApplicationService;
import com.electrocorp.electrocorpplatform.notifications.domain.model.commands.CreateAlertCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OperationModeActivatedEventHandler {

    private final NotificationApplicationService notificationApplicationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OperationModeActivatedEvent event) {
        notificationApplicationService.createAlert(new CreateAlertCommand(
                event.userId(),
                "Modo activado: " + event.modeName(),
                "El plan operativo se aplico sobre la sede seleccionada.",
                "INFO",
                "MODE",
                String.valueOf(event.modeId()),
                event.modeName(),
                "MODE_ACTIVITY",
                "MODE:%d:ACTIVATION".formatted(event.modeId()),
                event.evidence(),
                event.explanation(),
                event.recommendedAction(),
        ));
    }
}
