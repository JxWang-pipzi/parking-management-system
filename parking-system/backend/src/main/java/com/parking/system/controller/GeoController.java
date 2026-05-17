package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.service.MapRouteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/geo")
@Api(tags = "地理位置服务")
public class GeoController {

    @Resource
    private MapRouteService mapRouteService;

    @GetMapping("/location-summary")
    @ApiOperation("根据经纬度获取位置文案")
    public Response<Map<String, Object>> getLocationSummary(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        Map<String, Object> result = new HashMap<>();
        result.put("displayName", mapRouteService.resolveLocationText(latitude, longitude));
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return Response.success("获取成功", result);
    }
}
