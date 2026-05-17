package com.parking.system.service;

import com.parking.system.entity.UserVehicle;
import java.util.List;

public interface UserVehicleService {

    UserVehicle getById(Long id);

    List<UserVehicle> getByUserId(Long userId);

    UserVehicle getDefaultByUserId(Long userId);

    boolean addVehicle(UserVehicle vehicle);

    boolean updateVehicle(UserVehicle vehicle);

    boolean deleteVehicle(Long id, Long userId);

    boolean setDefault(Long id, Long userId);
}
