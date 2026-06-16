package com.electrocorp.electrocorpplatform.energymonitoring.domain.factories;

import com.electrocorp.electrocorpplatform.energymonitoring.domain.model.EnergyReadingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EnergyReadingFactoryTests {

    private final EnergyReadingFactory factory = new EnergyReadingFactory();

    @Test
    void shouldCreateNormalReading() {
        var reading = factory.create(
                1L,
                10L,
                "Lampara",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(0.001),
                BigDecimal.valueOf(0.00075),
                30,
                LocalDateTime.now()
        );

        assertEquals(EnergyReadingStatus.NORMAL, reading.getStatus());
        assertEquals(BigDecimal.valueOf(100).setScale(2), reading.getWatts());
        assertEquals(30, reading.getSampleSeconds());
    }

    @Test
    void shouldMarkHighConsumptionReading() {
        var reading = factory.create(
                1L,
                10L,
                "Refrigeradora",
                BigDecimal.valueOf(1850),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                5,
                LocalDateTime.now()
        );

        assertEquals(EnergyReadingStatus.HIGH, reading.getStatus());
    }
}