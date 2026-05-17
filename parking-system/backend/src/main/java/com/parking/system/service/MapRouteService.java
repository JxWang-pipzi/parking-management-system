package com.parking.system.service;

import com.parking.system.entity.ParkingLot;

public interface MapRouteService {

    void enrichParkingLotRoute(ParkingLot parkingLot, Double originLatitude, Double originLongitude);

    String resolveLocationText(Double latitude, Double longitude);
}
