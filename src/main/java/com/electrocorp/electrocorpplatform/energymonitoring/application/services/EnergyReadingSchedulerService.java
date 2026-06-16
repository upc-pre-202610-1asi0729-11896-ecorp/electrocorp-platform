package com.electrocorp.electrocorpplatform.energymonitoring.application.services;

import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.aggregates.Device;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.model.DeviceStatus;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyReadingSchedulerService implements SchedulingConfigurer {

    private final DeviceRepository deviceRepository;
    private final EnergyReadingRecorderService recorderService;
    private final EnergySamplingSettingsService settingsService;

    public void recordActiveDevicesConsumption() {
        List<Device> activeDevices = deviceRepository.findAll()
                .stream()
                .filter(device -> device.getStatus() == DeviceStatus.ON)
                .toList();

        activeDevices.forEach(recorderService::recordActiveDeviceInterval);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::recordActiveDevicesConsumption,
                context -> {
                    Instant lastCompletion = context.lastCompletion();
                    Instant nextRunBase = lastCompletion != null ? lastCompletion : Instant.now();
                    return nextRunBase.plusSeconds(settingsService.getSampleSeconds());
                }
        );
    }
}