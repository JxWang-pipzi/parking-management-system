package com.parking.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.parking.system.config.MapRouteProperties;
import com.parking.system.entity.ParkingLot;
import com.parking.system.service.MapRouteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class MapRouteServiceImpl implements MapRouteService {

    private static final double EARTH_RADIUS_METERS = 6378137.0;

    private final MapRouteProperties mapRouteProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public MapRouteServiceImpl(MapRouteProperties mapRouteProperties) {
        this.mapRouteProperties = mapRouteProperties;
    }

    @Override
    public void enrichParkingLotRoute(ParkingLot parkingLot, Double originLatitude, Double originLongitude) {
        if (parkingLot == null) {
            return;
        }
        if (!isValidCoordinate(originLatitude, originLongitude) || !isValidCoordinate(parkingLot.getLatitude(), parkingLot.getLongitude())) {
            parkingLot.setDistance(null);
            parkingLot.setRouteDistanceMeters(null);
            parkingLot.setRouteDurationSeconds(null);
            parkingLot.setRouteDistanceText("未知距离");
            parkingLot.setRouteDurationText("");
            parkingLot.setRouteStrategy("missing_coordinate");
            parkingLot.setRouteProvider("none");
            return;
        }

        if (canUseTencentMap()) {
            try {
                if (applyTencentRoute(parkingLot, originLatitude, originLongitude)) {
                    return;
                }
            } catch (Exception ex) {
                log.warn("腾讯地图步行路线获取失败，使用估算策略: lotId={}, reason={}", parkingLot.getId(), ex.getMessage());
            }
        }

        applyEstimatedRoute(parkingLot, originLatitude, originLongitude);
    }

    @Override
    public String resolveLocationText(Double latitude, Double longitude) {
        if (!isValidCoordinate(latitude, longitude)) {
            return "当前位置";
        }
        if (canUseTencentMap()) {
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl("https://apis.map.qq.com/ws/geocoder/v1/")
                        .queryParam("location", latitude + "," + longitude)
                        .queryParam("key", mapRouteProperties.getKey())
                        .toUriString();
                String response = restTemplate.getForObject(url, String.class);
                JSONObject root = JSONObject.parseObject(response);
                if (root != null && root.getInteger("status") != null && root.getInteger("status") == 0) {
                    JSONObject result = root.getJSONObject("result");
                    if (result != null) {
                        JSONObject addressComponent = result.getJSONObject("address_component");
                        if (addressComponent != null) {
                            String city = addressComponent.getString("city");
                            String district = addressComponent.getString("district");
                            String street = addressComponent.getString("street");
                            StringBuilder builder = new StringBuilder();
                            if (StringUtils.isNotBlank(city)) {
                                builder.append(city);
                            }
                            if (StringUtils.isNotBlank(district)) {
                                if (builder.length() > 0) {
                                    builder.append(" ");
                                }
                                builder.append(district);
                            }
                            if (StringUtils.isNotBlank(street)) {
                                if (builder.length() > 0) {
                                    builder.append(" ");
                                }
                                builder.append(street);
                            }
                            if (builder.length() > 0) {
                                return builder.toString();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("腾讯地图逆地理编码失败，使用兜底文案: {}", ex.getMessage());
            }
        }
        return String.format("纬度%.4f 经度%.4f", latitude, longitude);
    }

    private boolean applyTencentRoute(ParkingLot parkingLot, Double originLatitude, Double originLongitude) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://apis.map.qq.com/ws/distance/v1/matrix")
                .queryParam("mode", "walking")
                .queryParam("from", originLatitude + "," + originLongitude)
                .queryParam("to", parkingLot.getLatitude() + "," + parkingLot.getLongitude())
                .queryParam("key", mapRouteProperties.getKey())
                .toUriString();
        String response = restTemplate.getForObject(url, String.class);
        JSONObject root = JSONObject.parseObject(response);
        if (root == null || root.getInteger("status") == null || root.getInteger("status") != 0) {
            return false;
        }
        JSONObject result = root.getJSONObject("result");
        if (result == null) {
            return false;
        }
        JSONArray rows = result.getJSONArray("rows");
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        JSONObject row = rows.getJSONObject(0);
        JSONArray elements = row.getJSONArray("elements");
        if (elements == null || elements.isEmpty()) {
            return false;
        }
        JSONObject element = elements.getJSONObject(0);
        Integer distanceMeters = element.getInteger("distance");
        Integer durationSeconds = element.getInteger("duration");
        if (distanceMeters == null || durationSeconds == null) {
            return false;
        }
        parkingLot.setDistance(distanceMeters.doubleValue());
        parkingLot.setRouteDistanceMeters(distanceMeters);
        parkingLot.setRouteDurationSeconds(durationSeconds);
        parkingLot.setRouteDistanceText(formatDistance(distanceMeters));
        parkingLot.setRouteDurationText(formatDuration(durationSeconds));
        parkingLot.setRouteStrategy("walking_route");
        parkingLot.setRouteProvider("tencent");
        return true;
    }

    private void applyEstimatedRoute(ParkingLot parkingLot, Double originLatitude, Double originLongitude) {
        int distanceMeters = (int) Math.round(calculateStraightLineDistance(originLatitude, originLongitude, parkingLot.getLatitude(), parkingLot.getLongitude()));
        int durationSeconds = (int) Math.max(60, Math.round(distanceMeters / mapRouteProperties.getEstimatedWalkingSpeedMetersPerMinute() * 60.0));
        parkingLot.setDistance((double) distanceMeters);
        parkingLot.setRouteDistanceMeters(distanceMeters);
        parkingLot.setRouteDurationSeconds(durationSeconds);
        parkingLot.setRouteDistanceText(formatDistance(distanceMeters));
        parkingLot.setRouteDurationText(formatDuration(durationSeconds));
        parkingLot.setRouteStrategy("estimated_walking");
        parkingLot.setRouteProvider("estimate");
    }

    private boolean canUseTencentMap() {
        return mapRouteProperties.isEnabled()
                && "tencent".equalsIgnoreCase(mapRouteProperties.getProvider())
                && StringUtils.isNotBlank(mapRouteProperties.getKey());
    }

    private boolean isValidCoordinate(Double latitude, Double longitude) {
        return latitude != null
                && longitude != null
                && Math.abs(latitude) > 0.000001
                && Math.abs(longitude) > 0.000001;
    }

    private double calculateStraightLineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private String formatDistance(int distanceMeters) {
        if (distanceMeters < 1000) {
            return distanceMeters + "米";
        }
        return String.format("%.1f公里", distanceMeters / 1000.0);
    }

    private String formatDuration(int durationSeconds) {
        int totalMinutes = (int) Math.max(1, Math.ceil(durationSeconds / 60.0));
        if (totalMinutes < 60) {
            return totalMinutes + "分钟";
        }
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (minutes == 0) {
            return hours + "小时";
        }
        return hours + "小时" + minutes + "分钟";
    }
}
