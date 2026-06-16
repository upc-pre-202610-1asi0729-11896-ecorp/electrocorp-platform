package com.electrocorp.electrocorpplatform.devicecontrol.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.devicecontrol.application.services.DeviceControlApplicationService;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.DeviceStatus;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.RoutineAction;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.RoutineRepeatType;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.RoutineTargetType;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.aggregates.Device;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.aggregates.Routine;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.repositories.DeviceRepository;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.repositories.RoutineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class DeviceControlRepositoryPortPersistenceTests {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private JpaDeviceRepository jpaDeviceRepository;

    @Autowired
    private JpaRoutineRepository jpaRoutineRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(deviceRepository);
        assertNotNull(routineRepository);
        assertNotNull(jpaDeviceRepository);
        assertNotNull(jpaRoutineRepository);
        assertInstanceOf(JpaDeviceRepository.class, deviceRepository);
        assertInstanceOf(JpaRoutineRepository.class, routineRepository);
    }

    @Test
    void deviceRepositoryPersistsAndFindsByPortMethods() {
        Long userId = 1001L;
        Device savedDevice = deviceRepository.save(device(userId, "Televisor"));
        deviceRepository.save(device(2002L, "Microondas"));

        assertNotNull(savedDevice.getId());
        assertTrue(deviceRepository.findById(savedDevice.getId()).isPresent());
        assertEquals(1, deviceRepository.findByUserId(userId).size());
        assertTrue(deviceRepository.findByIdAndUserId(savedDevice.getId(), userId).isPresent());
        assertTrue(deviceRepository.findByIdAndUserId(savedDevice.getId(), 9999L).isEmpty());
    }

    @Test
    void routineRepositoryPersistsFindsAndDeletesByPortMethods() {
        Long userId = 3003L;
        Device targetDevice = deviceRepository.save(device(userId, "Enchufe dormitorio"));
        Routine savedRoutine = routineRepository.save(routine(userId, targetDevice.getId()));

        assertNotNull(savedRoutine.getId());
        assertEquals(1, routineRepository.findByUserId(userId).size());
        assertTrue(routineRepository.findByIdAndUserId(savedRoutine.getId(), userId).isPresent());
        assertTrue(routineRepository.findByIdAndUserId(savedRoutine.getId(), 9999L).isEmpty());

        routineRepository.delete(savedRoutine);

        assertTrue(routineRepository.findByIdAndUserId(savedRoutine.getId(), userId).isEmpty());
    }

    @Test
    void deviceControlApplicationServiceKeepsDomainRepositoryPorts() throws NoSuchFieldException {
        assertEquals(
                DeviceRepository.class,
                DeviceControlApplicationService.class.getDeclaredField("deviceRepository").getType()
        );
        assertEquals(
                RoutineRepository.class,
                DeviceControlApplicationService.class.getDeclaredField("routineRepository").getType()
        );
    }

    private Device device(Long userId, String name) {
        return new Device(
                userId,
                name,
                "Dormitorio",
                "Enchufe",
                BigDecimal.valueOf(120),
                DeviceStatus.OFF
        );
    }

    private Routine routine(Long userId, Long deviceId) {
        return new Routine(
                userId,
                deviceId,
                null,
                RoutineTargetType.DEVICE,
                deviceId,
                "Apagar dispositivo",
                RoutineAction.TURN_OFF,
                "22:00",
                RoutineRepeatType.DAILY,
                null,
                1,
                null,
                true
        );
    }
}
