package com.electrocorp.electrocorpplatform.reporting.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.devicecontrol.application.services.DeviceControlApplicationService;
import com.electrocorp.electrocorpplatform.notifications.domain.services.AlertSourceOwnershipService;
import com.electrocorp.electrocorpplatform.reporting.application.services.ReportingApplicationService;
import com.electrocorp.electrocorpplatform.reporting.domain.model.aggregates.ConsumptionReport;
import com.electrocorp.electrocorpplatform.reporting.domain.model.aggregates.EnergyGoal;
import com.electrocorp.electrocorpplatform.reporting.domain.repositories.ConsumptionReportRepository;
import com.electrocorp.electrocorpplatform.reporting.domain.repositories.EnergyGoalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReportingRepositoryPortPersistenceTests {

    @Autowired
    private EnergyGoalRepository energyGoalRepository;

    @Autowired
    private ConsumptionReportRepository consumptionReportRepository;

    @Autowired
    private JpaEnergyGoalRepository jpaEnergyGoalRepository;

    @Autowired
    private JpaConsumptionReportRepository jpaConsumptionReportRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(energyGoalRepository);
        assertNotNull(consumptionReportRepository);
        assertNotNull(jpaEnergyGoalRepository);
        assertNotNull(jpaConsumptionReportRepository);
        assertInstanceOf(JpaEnergyGoalRepository.class, energyGoalRepository);
        assertInstanceOf(JpaConsumptionReportRepository.class, consumptionReportRepository);
    }

    @Test
    void energyGoalRepositoryPersistsFindsAndDeletesByPortMethods() {
        Long userId = 8501L;
        EnergyGoal savedGoal = energyGoalRepository.save(energyGoal(userId, "Reducir consumo"));
        energyGoalRepository.save(energyGoal(8502L, "Otro usuario"));

        assertNotNull(savedGoal.getId());
        assertTrue(energyGoalRepository.findById(savedGoal.getId()).isPresent());
        assertEquals(1, energyGoalRepository.findByUserId(userId).size());

        energyGoalRepository.delete(savedGoal);

        assertTrue(energyGoalRepository.findById(savedGoal.getId()).isEmpty());
    }

    @Test
    void consumptionReportRepositoryPersistsFindsCyclesAndDeletesByPortMethods() {
        Long userId = 8601L;
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        ConsumptionReport firstReport = consumptionReportRepository.save(consumptionReport(userId, start, end, BigDecimal.valueOf(100)));
        ConsumptionReport secondReport = consumptionReportRepository.save(consumptionReport(userId, start.plusMonths(1), end.plusMonths(1), BigDecimal.valueOf(200)));
        consumptionReportRepository.save(consumptionReport(8602L, start, end, BigDecimal.valueOf(300)));

        assertNotNull(firstReport.getId());
        assertTrue(consumptionReportRepository.findById(firstReport.getId()).isPresent());
        assertEquals(2, consumptionReportRepository.findByUserId(userId).size());
        assertEquals(
                firstReport.getId(),
                consumptionReportRepository.findByUserIdOrderByStartDateAscEndDateAscIdAsc(userId).get(0).getId()
        );
        assertEquals(
                firstReport.getId(),
                consumptionReportRepository.findByUserIdAndStartDateAndEndDateOrderByIdAsc(userId, start, end).get(0).getId()
        );
        assertEquals(secondReport.getId(), consumptionReportRepository.findById(secondReport.getId()).orElseThrow().getId());

        consumptionReportRepository.delete(firstReport);

        assertTrue(consumptionReportRepository.findById(firstReport.getId()).isEmpty());
    }

    @Test
    void consumersKeepDomainRepositoryPorts() throws NoSuchFieldException {
        assertFieldType(ReportingApplicationService.class, "reportRepository", ConsumptionReportRepository.class);
        assertFieldType(ReportingApplicationService.class, "goalRepository", EnergyGoalRepository.class);
        assertFieldType(DeviceControlApplicationService.class, "energyGoalRepository", EnergyGoalRepository.class);
        assertFieldType(AlertSourceOwnershipService.class, "energyGoalRepository", EnergyGoalRepository.class);
    }

    private void assertFieldType(Class<?> owner, String fieldName, Class<?> expectedType) throws NoSuchFieldException {
        Field field = owner.getDeclaredField(fieldName);

        assertEquals(expectedType, field.getType());
    }

    private EnergyGoal energyGoal(Long userId, String title) {
        EnergyGoal goal = new EnergyGoal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setTargetKilowattHours(BigDecimal.valueOf(5.000000));
        goal.setCurrentKilowattHours(BigDecimal.ZERO);
        goal.setDeadline(LocalDate.of(2026, 6, 30));
        goal.setStatus("ACTIVE");
        goal.setScopeType("GENERAL");
        return goal;
    }

    private ConsumptionReport consumptionReport(Long userId, LocalDate start, LocalDate end, BigDecimal totalWatts) {
        ConsumptionReport report = new ConsumptionReport();
        report.setUserId(userId);
        report.setTotalWatts(totalWatts);
        report.setAverageWatts(totalWatts.divide(BigDecimal.TEN));
        report.setHighestWatts(totalWatts);
        report.setStartDate(start);
        report.setEndDate(end);
        return report;
    }
}
