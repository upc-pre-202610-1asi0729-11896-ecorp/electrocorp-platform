package com.electrocorp.electrocorpplatform.devicecontrol.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PowerWattsTests {

    @Test
    void shouldRejectZeroOrNegativePower() {
        assertThrows(IllegalArgumentException.class, () -> new PowerWatts(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new PowerWatts(BigDecimal.valueOf(-5)));
    }

    @Test
    void shouldNormalizePowerScale() {
        PowerWatts watts = new PowerWatts(BigDecimal.valueOf(120.456));

        assertEquals(BigDecimal.valueOf(120.46), watts.value());
    }
}