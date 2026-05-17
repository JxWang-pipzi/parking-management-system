package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.UserVehicle;
import com.parking.system.service.UserVehicleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vehicles")
@Api(tags = "我的车辆")
public class UserVehicleController {

    @Resource
    private UserVehicleService userVehicleService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping
    @ApiOperation("获取我的车辆列表")
    public Response<List<UserVehicle>> getMyVehicles() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][获取车辆列表] 时间：{} | 参数：userId={}", System.currentTimeMillis(), userId);
        List<UserVehicle> vehicles = userVehicleService.getByUserId(userId);
        return Response.success("获取成功", vehicles);
    }

    @GetMapping("/default")
    @ApiOperation("获取默认车辆")
    public Response<UserVehicle> getDefaultVehicle() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][获取默认车辆] 时间：{} | 参数：userId={}", System.currentTimeMillis(), userId);
        UserVehicle vehicle = userVehicleService.getDefaultByUserId(userId);
        return Response.success("获取成功", vehicle);
    }

    @PostMapping
    @ApiOperation("添加车辆")
    public Response<UserVehicle> addVehicle(@RequestBody UserVehicle vehicle) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][添加车辆] 时间：{} | 参数：plateNumber={},brand={},color={}",
                System.currentTimeMillis(), vehicle.getPlateNumber(), vehicle.getBrand(), vehicle.getColor());

        if (vehicle.getPlateNumber() == null || vehicle.getPlateNumber().trim().isEmpty()) {
            return Response.error("车牌号不能为空");
        }
        String plateNumber = vehicle.getPlateNumber().trim().toUpperCase();
        if (!plateNumber.matches("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5,6}$")) {
            return Response.error("车牌号格式不正确");
        }

        vehicle.setUserId(userId);
        vehicle.setPlateNumber(plateNumber);

        if (userVehicleService.addVehicle(vehicle)) {
            log.info("[成功][阶段4][添加车辆] 结果：成功，id={}", vehicle.getId());
            return Response.success("添加成功", vehicle);
        }
        return Response.error("添加失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("更新车辆信息")
    public Response<UserVehicle> updateVehicle(@PathVariable Long id, @RequestBody UserVehicle vehicle) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][更新车辆] 时间：{} | 参数：id={}", System.currentTimeMillis(), id);

        if (vehicle.getPlateNumber() == null || vehicle.getPlateNumber().trim().isEmpty()) {
            return Response.error("车牌号不能为空");
        }
        String plateNumber = vehicle.getPlateNumber().trim().toUpperCase();
        if (!plateNumber.matches("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5,6}$")) {
            return Response.error("车牌号格式不正确");
        }

        vehicle.setId(id);
        vehicle.setUserId(userId);
        vehicle.setPlateNumber(plateNumber);
        if (userVehicleService.updateVehicle(vehicle)) {
            UserVehicle updated = userVehicleService.getById(id);
            return Response.success("更新成功", updated);
        }
        return Response.error("更新失败");
    }

    @PutMapping("/{id}/default")
    @ApiOperation("设为默认车辆")
    public Response<?> setDefault(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][设为默认车辆] 时间：{} | 参数：id={},userId={}", System.currentTimeMillis(), id, userId);
        if (userVehicleService.setDefault(id, userId)) {
            return Response.success("设置成功");
        }
        return Response.error("设置失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除车辆")
    public Response<?> deleteVehicle(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Response.error("未登录");
        }
        log.info("[成功][阶段2][删除车辆] 时间：{} | 参数：id={},userId={}", System.currentTimeMillis(), id, userId);
        if (userVehicleService.deleteVehicle(id, userId)) {
            return Response.success("删除成功");
        }
        return Response.error("删除失败");
    }
}
