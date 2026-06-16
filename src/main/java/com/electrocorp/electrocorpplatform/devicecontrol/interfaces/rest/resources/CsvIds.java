package com.electrocorp.electrocorpplatform.devicecontrol.interfaces.rest.resources;

import java.util.Arrays;
import java.util.List;

final class CsvIds {
    private CsvIds() {
    }

    static List<Long> parse(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
