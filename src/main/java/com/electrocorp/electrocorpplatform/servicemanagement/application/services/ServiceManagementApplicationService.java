package com.electrocorp.electrocorpplatform.servicemanagement.application.services;

import com.electrocorp.electrocorpplatform.servicemanagement.domain.model.commands.*;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.factories.SupportTicketFactory;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.model.aggregates.*;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.repositories.*;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceManagementApplicationService {

    private final SupportTicketRepository supportTicketRepository;
    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final DeviceRepository deviceRepository;
    private final SupportTicketFactory supportTicketFactory = new SupportTicketFactory();

    @Transactional(readOnly = true)
    public List<SupportTicket> getSupportTickets(Long userId) {
        return supportTicketRepository.findByUserId(userId);
    }

    @Transactional
    public SupportTicket createSupportTicket(CreateSupportTicketCommand command) {
        SupportTicket ticket = supportTicketFactory.create(
                command.userId(),
                command.subject(),
                command.description(),
                command.priority()
        );
        return supportTicketRepository.save(ticket);
    }

    @Transactional
    public SupportTicket updateSupportStatus(Long userId, Long ticketId, UpdateTicketStatusCommand command) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Support ticket not found."));
        ticket.setStatus(command.status());
        return supportTicketRepository.save(ticket);
    }

    @Transactional
    public void deleteSupportTicket(DeleteSupportTicketCommand command) {
        SupportTicket ticket = supportTicketRepository.findById(command.ticketId())
                .filter(item -> item.getUserId().equals(command.userId()))
                .orElseThrow(() -> new IllegalArgumentException("Support ticket not found."));
        supportTicketRepository.delete(ticket);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTicket> getMaintenanceTickets(Long userId) {
        return maintenanceTicketRepository.findByUserId(userId);
    }

    @Transactional
    public MaintenanceTicket createMaintenanceTicket(CreateMaintenanceTicketCommand command) {
        var device = deviceRepository.findByIdAndUserId(command.deviceId(), command.userId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found."));

        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setUserId(command.userId());
        ticket.setDeviceId(device.getId());
        ticket.setDeviceName(device.getName());
        ticket.setType(command.type());
        ticket.setDescription(command.description());
        ticket.setScheduledDate(command.scheduledDate());
        ticket.setStatus("OPEN");
        return maintenanceTicketRepository.save(ticket);
    }

    @Transactional
    public MaintenanceTicket updateMaintenanceStatus(Long userId, Long ticketId, UpdateTicketStatusCommand command) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(ticketId)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Maintenance ticket not found."));
        ticket.setStatus(command.status());
        return maintenanceTicketRepository.save(ticket);
    }

    @Transactional
    public void deleteMaintenanceTicket(DeleteMaintenanceTicketCommand command) {
        MaintenanceTicket ticket = maintenanceTicketRepository.findById(command.ticketId())
                .filter(item -> item.getUserId().equals(command.userId()))
                .orElseThrow(() -> new IllegalArgumentException("Maintenance ticket not found."));
        maintenanceTicketRepository.delete(ticket);
    }
}
