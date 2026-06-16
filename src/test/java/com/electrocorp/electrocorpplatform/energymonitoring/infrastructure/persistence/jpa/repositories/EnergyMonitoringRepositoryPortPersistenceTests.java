package com.electrocorp.electrocorpplatform.energymonitoring.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.energymonitoring.domain.model.EnergyDeviceUsageState;
import com.electrocorp.electrocorpplatform.energymonitoring.domain.model.EnergyReadingStatus;
import com.electrocorp.electrocorpplatform.energymonitoring.domain.model.aggregates.EnergyReading;
import com.electrocorp.electrocorpplatform.energymonitoring.domain.repositories.EnergyDeviceUsageStateRepository;
import com.electrocorp.electrocorpplatform.energymonitoring.domain.repositories.EnergyReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class EnergyMonitoringRepositoryPortPersistenceTests {

    @Autowired
    private EnergyReadingRepository energyReadingRepository;

    @Autowired
    private EnergyDeviceUsageStateRepository energyDeviceUsageStateRepository;

    @Autowired
    private JpaEnergyReadingRepository jpaEnergyReadingRepository;

    @Autowired
    private JpaEnergyDeviceUsageStateRepository jpaEnergyDeviceUsageStateRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(energyReadingRepository);
        assertNotNull(energyDeviceUsageStateRepository);
        assertNotNull(jpaEnergyReadingRepository);
        assertNotNull(jpaEnergyDeviceUsageStateRepository);
        assertInstanceOf(JpaEnergyReadingRepository.class, energyReadingRepository);
        assertInstanceOf(JpaEnergyDeviceUsageStateRepository.class, energyDeviceUsageStateRepository);
    }

    @Test
    void energyReadingRepositoryPersistsAndFindsByRangeAndOrder() {
        Long userId = 8301L;
        LocalDateTime base = LocalDateTime.of(2026, 6, 12, 8, 0);
        EnergyReading firstReading = energyReadingRepository.save(energyReading(userId, 101L, base.plusMinutes(1), BigDecimal.valueOf(120)));
        EnergyReading secondReading = energyReadingRepository.save(energyReading(userId, 102L, base.plusMinutes(5), BigDecimal.valueOf(220)));
        energyReadingRepository.save(energyReading(8302L, 103L, base.plusMinutes(3), BigDecimal.valueOf(320)));

        assertNotNull(firstReading.getId());
        assertEquals(2, energyReadingRepository.findByUserIdAndRecordedAtBetween(userId, base, base.plusMinutes(10)).size());
        assertEquals(
                secondReading.getId(),
                energyReadingRepository.findByUserIdOrderByRecordedAtDesc(userId).get(0).getId()
        );
        assertEquals(
                secondReading.getId(),
                energyReadingRepository
                        .findByUserIdAndRecordedAtBetweenOrderByRecordedAtDesc(userId, base, base.plusMinutes(10))
                        .get(0)
                        .getId()
        );
    }

    @Test
    void usageStateRepositoryPersistsFindsAndDeletesByDevice() {
        EnergyDeviceUsageState state = energyDeviceUsageStateRepository.save(usageState(8401L, 201L));
        EnergyDeviceUsageState deleteByDeviceState = energyDeviceUsageStateRepository.save(usageState(8401L, 202L));

        assertNotNull(state.getId());
        assertTrue(energyDeviceUsageStateRepository.findByDeviceId(201L).isPresent());

        energyDeviceUsageStateRepository.delete(state);
        assertTrue(energyDeviceUsageStateRepository.findByDeviceId(201L).isEmpty());

        energyDeviceUsageStateRepository.deleteByDeviceId(deleteByDeviceState.getDeviceId());
        assertTrue(energyDeviceUsageStateRepository.findByDeviceId(deleteByDeviceState.getDeviceId()).isEmpty());
    }

    private EnergyReading energyReading(Long userId, Long deviceId, LocalDateTime recordedAt, BigDecimal watts) {
        EnergyReading reading = new EnergyReading();
        reading.setUserId(userId);
        reading.setDeviceId(deviceId);
        reading.setDeviceName("Equipo " + deviceId);
        reading.setRecordedAt(recordedAt);
        reading.setWatts(watts);
        reading.setKilowattHours(BigDecimal.valueOf(0.050000));
        reading.setEstimatedCost(BigDecimal.valueOf(0.037500));
        reading.setSampleSeconds(15);
        reading.setStatus(EnergyReadingStatus.NORMAL);
        return reading;
    }

    private EnergyDeviceUsageState usageState(Long userId, Long deviceId) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 12, 8, 0);
        EnergyDeviceUsageState state = new EnergyDeviceUsageState();
        state.setUserId(userId);
        state.setDeviceId(deviceId);
        state.setStartedAt(now);
        state.setLastRecordedAt(now.plusMinutes(1));
        return state;
    }
}
