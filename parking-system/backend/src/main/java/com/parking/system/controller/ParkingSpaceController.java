package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.config.ParkingWebSocketHandler;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.service.ParkingSpaceService;
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
@RequestMapping("/parking-spaces")
@Api(tags = "车位管理")
public class ParkingSpaceController {

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @GetMapping
    @ApiOperation("获取所有车位")
    public Response<List<ParkingSpace>> getAllParkingSpaces() {
        List<ParkingSpace> spaces = parkingSpaceService.list();
        return Response.success(spaces);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取车位详情")
    public Response<ParkingSpace> getParkingSpace(@PathVariable Long id) {
        ParkingSpace parkingSpace = parkingSpaceService.getById(id);
        if (parkingSpace != null) {
            return Response.success("获取成功", parkingSpace);
        }
        return Response.error("车位不存在");
    }

    @PostMapping("/{id}/reserve")
    @ApiOperation("预约车位")
    public Response<?> reserveParkingSpace(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof Long)) {
            log.warn("[失败][阶段2][预约车位] 时间：{} | 原因：未登录", System.currentTimeMillis());
            return Response.error("未登录");
        }
        Long userId = (Long) principal;
        log.info("[成功][阶段2][预约车位] 时间：{} | 参数：spaceId={}, userId={}", System.currentTimeMillis(), id, userId);
        if (parkingSpaceService.reserveParkingSpace(id, userId)) {
            return Response.success("预约成功");
        }
        return Response.error("预约失败，车位可能已被占用");
    }

    @PostMapping("/{id}/release")
    @ApiOperation("释放车位")
    public Response<?> releaseParkingSpace(@PathVariable Long id) {
        log.info("[成功][阶段2][释放车位] 时间：{} | 参数：spaceId={}", System.currentTimeMillis(), id);
        if (parkingSpaceService.releaseParkingSpace(id)) {
            return Response.success("释放成功");
        }
        return Response.error("释放失败");
    }

    @PostMapping
    @ApiOperation("添加车位")
    public Response<ParkingSpace> addParkingSpace(@RequestBody ParkingSpace parkingSpace) {
        if (parkingSpaceService.save(parkingSpace)) {
            ParkingWebSocketHandler.pushSpaceUpdate(parkingSpace.getId(), "created");
            return Response.success("添加成功", parkingSpace);
        }
        return Response.error("添加失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("更新车位信息")
    public Response<ParkingSpace> updateParkingSpace(@PathVariable Long id, @RequestBody ParkingSpace parkingSpace) {
        parkingSpace.setId(id);
        if (parkingSpaceService.updateById(parkingSpace)) {
            ParkingWebSocketHandler.pushSpaceUpdate(id, "updated");
            return Response.success("更新成功", parkingSpace);
        }
        return Response.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除车位")
    public Response<?> deleteParkingSpace(@PathVariable Long id) {
        if (parkingSpaceService.removeById(id)) {
            ParkingWebSocketHandler.pushSpaceUpdate(id, "deleted");
            return Response.success("删除成功");
        }
        return Response.error("删除失败");
    }

}
