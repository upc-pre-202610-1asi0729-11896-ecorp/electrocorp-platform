package com.electrocorp.electrocorpplatform.workplace.application.services;

import com.electrocorp.electrocorpplatform.workplace.domain.model.commands.AssignDeviceCommand;
import com.electrocorp.electrocorpplatform.workplace.domain.model.commands.CreateLocationCommand;
import com.electrocorp.electrocorpplatform.workplace.domain.model.commands.CreateRoomCommand;
import com.electrocorp.electrocorpplatform.devicecontrol.domain.repositories.DeviceRepository;
import com.electrocorp.electrocorpplatform.workplace.domain.model.aggregates.*;
import com.electrocorp.electrocorpplatform.workplace.domain.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkplaceApplicationService {

    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public List<Location> getLocations(Long userId) {
        return locationRepository.findByUserId(userId);
    }

    @Transactional
    public Location createLocation(CreateLocationCommand command) {
        Location location = new Location();
        location.setUserId(command.userId());
        location.setName(command.name());
        location.setAddress(command.address());
        location.setType(command.type());
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public List<Room> getRooms(Long userId, Long locationId) {
        getLocationForUser(locationId, userId);
        return roomRepository.findByLocationId(locationId);
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomsByUserId(Long userId) {
        List<Long> locationIds = locationRepository.findByUserId(userId)
                .stream()
                .map(Location::getId)
                .toList();

        return locationIds.isEmpty() ? List.of() : roomRepository.findByLocationIdIn(locationIds);
    }

    @Transactional
    public Room createRoom(Long userId, CreateRoomCommand command) {
        getLocationForUser(command.locationId(), userId);

        Room room = new Room();
        room.setLocationId(command.locationId());
        room.setName(command.name());
        room.setFloor(command.floor());
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<DeviceAssignment> getAssignments(Long userId, Long locationId) {
        getLocationForUser(locationId, userId);
        return assignmentRepository.findByLocationId(locationId);
    }

    @Transactional(readOnly = true)
    public List<DeviceAssignment> getAssignmentsByUserId(Long userId) {
        List<Long> locationIds = locationRepository.findByUserId(userId)
                .stream()
                .map(Location::getId)
                .toList();

        return locationIds.isEmpty() ? List.of() : assignmentRepository.findByLocationIdIn(locationIds);
    }

    @Transactional(readOnly = true)
    public List<DeviceAssignment> getAssignmentsByDeviceId(Long userId, Long deviceId) {
        deviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found."));

        return assignmentRepository.findByDeviceId(deviceId)
                .stream()
                .filter(assignment -> locationRepository.findById(assignment.getLocationId())
                        .map(location -> location.getUserId().equals(userId))
                        .orElse(false))
                .toList();
    }

    @Transactional
    public DeviceAssignment assignDevice(Long userId, AssignDeviceCommand command) {
        getLocationForUser(command.locationId(), userId);
        deviceRepository.findByIdAndUserId(command.deviceId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found."));

        validateRoomBelongsToLocation(command.roomId(), command.locationId());

        if (!assignmentRepository.findByDeviceId(command.deviceId()).isEmpty()) {
            throw new IllegalArgumentException("Device is already assigned.");
        }

        DeviceAssignment assignment = new DeviceAssignment();
        assignment.setDeviceId(command.deviceId());
        assignment.setRoomId(command.roomId());
        assignment.setLocationId(command.locationId());
        assignment.setAssignedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Location updateLocation(Long userId, Long locationId, String name, String address, String type) {
        Location location = getLocationForUser(locationId, userId);

        if (name != null && !name.isBlank()) location.setName(name.trim());
        if (address != null) location.setAddress(address.trim());
        if (type != null) location.setType(type);

        return locationRepository.save(location);
    }

    @Transactional
    public void deleteLocation(Long userId, Long locationId) {
        Location location = getLocationForUser(locationId, userId);
        assignmentRepository.deleteByLocationId(locationId);
        locationRepository.delete(location);
    }

    @Transactional
    public Room updateRoom(Long userId, Long roomId, Long locationId, String name, String floor) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        getLocationForUser(room.getLocationId(), userId);
        if (locationId != null) {
            getLocationForUser(locationId, userId);
            room.setLocationId(locationId);
        }
        if (name != null && !name.isBlank()) room.setName(name.trim());
        if (floor != null) room.setFloor(floor.trim());

        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long userId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        getLocationForUser(room.getLocationId(), userId);

        assignmentRepository.findByRoomId(roomId)
                .forEach(assignment -> {
                    assignment.setRoomId(null);
                    assignmentRepository.save(assignment);
                });

        roomRepository.delete(room);
    }

    @Transactional
    public DeviceAssignment moveAssignment(Long userId, Long assignmentId, Long locationId, Long roomId) {
        DeviceAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Device assignment not found."));

        getLocationForUser(assignment.getLocationId(), userId);
        Long targetLocationId = locationId != null ? locationId : assignment.getLocationId();
        if (locationId != null) {
            getLocationForUser(locationId, userId);
            assignment.setLocationId(locationId);
        }

        validateRoomBelongsToLocation(roomId, targetLocationId);
        assignment.setRoomId(roomId);

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignment(Long userId, Long assignmentId) {
        DeviceAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Device assignment not found."));
        getLocationForUser(assignment.getLocationId(), userId);
        assignmentRepository.delete(assignment);
    }

    private Location getLocationForUser(Long locationId, Long userId) {
        return locationRepository.findById(locationId)
                .filter(location -> location.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Location does not belong to current user."));
    }

    private void validateRoomBelongsToLocation(Long roomId, Long locationId) {
        if (roomId == null) {
            return;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        if (!room.getLocationId().equals(locationId)) {
            throw new IllegalArgumentException("Room does not belong to the selected location.");
        }
    }
}
