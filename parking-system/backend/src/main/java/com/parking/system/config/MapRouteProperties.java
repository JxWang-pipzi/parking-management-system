package com.parking.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "map.route")
public class MapRouteProperties {

    private boolean enabled = false;

    private String provider = "tencent";

    private String key;

    private double estimatedWalkingSpeedMetersPerMinute = 72.0;
}
