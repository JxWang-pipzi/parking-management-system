package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.config.ParkingWebSocketHandler;
import com.parking.system.entity.ParkingLot;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking-lots")
@Api(tags = "停车场管理")
public class ParkingLotController {

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @GetMapping
    @ApiOperation("获取停车场列表")
    public Response<List<ParkingLot>> getParkingLots(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        List<ParkingLot> parkingLots = parkingLotService.listParkingLots(latitude, longitude);
        return Response.success("获取成功", parkingLots);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取停车场详情")
    public Response<ParkingLot> getParkingLot(@PathVariable Long id) {
        ParkingLot parkingLot = parkingLotService.getParkingLotWithSpaces(id);
        if (parkingLot != null) {
            return Response.success("获取成功", parkingLot);
        }
        return Response.error("停车场不存在");
    }

    @GetMapping("/{id}/spaces")
    @ApiOperation("获取停车场车位列表")
    public Response<?> getParkingSpaces(@PathVariable Long id, @RequestParam(required = false) Integer status) {
        return Response.success("获取成功", parkingSpaceService.getSpacesByParkingLotId(id, status));
    }

    @GetMapping("/nearby")
    @ApiOperation("搜索附近的停车场")
    public Response<List<ParkingLot>> getNearbyParkingLots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5000") Double radius) {
        List<ParkingLot> parkingLots = parkingLotService.getNearbyParkingLots(latitude, longitude, radius);
        return Response.success("获取成功", parkingLots);
    }

    @PostMapping
    @ApiOperation("添加停车场")
    public Response<ParkingLot> addParkingLot(@RequestBody ParkingLot parkingLot) {
        if (parkingLotService.createParkingLot(parkingLot)) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "create");
            payload.put("lot", parkingLot);
            ParkingWebSocketHandler.pushParkingLotUpdate(parkingLot.getId(), payload);
            return Response.success("添加成功", parkingLot);
        }
        return Response.error("添加失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("更新停车场信息")
    public Response<ParkingLot> updateParkingLot(@PathVariable Long id, @RequestBody ParkingLot parkingLot) {
        parkingLot.setId(id);
        if (parkingLotService.updateParkingLotInfo(parkingLot)) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "update");
            payload.put("lot", parkingLotService.getById(id));
            ParkingWebSocketHandler.pushParkingLotUpdate(id, payload);
            return Response.success("更新成功", parkingLot);
        }
        return Response.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除停车场")
    public Response<?> deleteParkingLot(@PathVariable Long id) {
        if (parkingLotService.removeById(id)) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "delete");
            payload.put("lotId", id);
            ParkingWebSocketHandler.pushParkingLotUpdate(id, payload);
            return Response.success("删除成功");
        }
        return Response.error("删除失败");
    }

}
