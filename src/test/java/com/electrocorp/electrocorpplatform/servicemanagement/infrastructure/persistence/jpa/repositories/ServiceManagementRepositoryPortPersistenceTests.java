package com.electrocorp.electrocorpplatform.servicemanagement.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.servicemanagement.application.services.ServiceManagementApplicationService;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.model.aggregates.MaintenanceTicket;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.model.aggregates.SupportTicket;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.repositories.MaintenanceTicketRepository;
import com.electrocorp.electrocorpplatform.servicemanagement.domain.repositories.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ServiceManagementRepositoryPortPersistenceTests {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private MaintenanceTicketRepository maintenanceTicketRepository;

    @Autowired
    private JpaSupportTicketRepository jpaSupportTicketRepository;

    @Autowired
    private JpaMaintenanceTicketRepository jpaMaintenanceTicketRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(supportTicketRepository);
        assertNotNull(maintenanceTicketRepository);
        assertNotNull(jpaSupportTicketRepository);
        assertNotNull(jpaMaintenanceTicketRepository);
        assertInstanceOf(JpaSupportTicketRepository.class, supportTicketRepository);
        assertInstanceOf(JpaMaintenanceTicketRepository.class, maintenanceTicketRepository);
    }

    @Test
    void supportTicketRepositoryPersistsFindsAndDeletesByPortMethods() {
        Long userId = 7001L;
        SupportTicket savedTicket = supportTicketRepository.save(supportTicket(userId, "Equipo sin respuesta"));
        supportTicketRepository.save(supportTicket(8002L, "Consulta de servicio"));

        assertNotNull(savedTicket.getId());
        assertTrue(supportTicketRepository.findById(savedTicket.getId()).isPresent());
        assertEquals(1, supportTicketRepository.findByUserId(userId).size());

        supportTicketRepository.delete(savedTicket);

        assertTrue(supportTicketRepository.findById(savedTicket.getId()).isEmpty());
    }

    @Test
    void maintenanceTicketRepositoryPersistsFindsAndDeletesByPortMethods() {
        Long userId = 9003L;
        MaintenanceTicket savedTicket = maintenanceTicketRepository.save(
                maintenanceTicket(userId, 300L, "Televisor")
        );
        maintenanceTicketRepository.save(maintenanceTicket(9004L, 301L, "Microondas"));

        assertNotNull(savedTicket.getId());
        assertTrue(maintenanceTicketRepository.findById(savedTicket.getId()).isPresent());
        assertEquals(1, maintenanceTicketRepository.findByUserId(userId).size());

        maintenanceTicketRepository.delete(savedTicket);

        assertTrue(maintenanceTicketRepository.findById(savedTicket.getId()).isEmpty());
    }

    @Test
    void serviceManagementApplicationServiceKeepsDomainRepositoryPorts() throws NoSuchFieldException {
        assertEquals(
                SupportTicketRepository.class,
                ServiceManagementApplicationService.class.getDeclaredField("supportTicketRepository").getType()
        );
        assertEquals(
                MaintenanceTicketRepository.class,
                ServiceManagementApplicationService.class.getDeclaredField("maintenanceTicketRepository").getType()
        );
    }

    private SupportTicket supportTicket(Long userId, String subject) {
        return new SupportTicket(
                userId,
                subject,
                "Revision solicitada por el usuario.",
                "MEDIUM",
                "OPEN"
        );
    }

    private MaintenanceTicket maintenanceTicket(Long userId, Long deviceId, String deviceName) {
        return new MaintenanceTicket(
                userId,
                deviceId,
                deviceName,
                "INSPECTION",
                "Revision preventiva programada.",
                LocalDate.now().plusDays(1),
                "OPEN"
        );
    }
}
