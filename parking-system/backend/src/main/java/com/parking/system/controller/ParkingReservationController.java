package com.parking.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.system.common.Response;
import com.parking.system.entity.ParkingReservation;
import com.parking.system.service.ParkingReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation")
public class ParkingReservationController {

    @Autowired
    private ParkingReservationService reservationService;

    @GetMapping("/all")
    public Response<IPage<ParkingReservation>> getAllReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Response.success(reservationService.page(new Page<>(page, size)));
    }

    @PostMapping("/create")
    public Response<Boolean> createReservation(@RequestBody ParkingReservation reservation) {
        return Response.success(reservationService.createReservation(reservation));
    }

    @PostMapping("/cancel/{id}")
    public Response<Boolean> cancelReservation(@PathVariable Long id, @RequestParam String reason) {
        return Response.success(reservationService.cancelReservation(id, reason));
    }

    @PostMapping("/confirm/{id}")
    public Response<Boolean> confirmReservation(@PathVariable Long id) {
        return Response.success(reservationService.confirmReservation(id));
    }
    
    @GetMapping("/{id}")
    public Response<ParkingReservation> getReservation(@PathVariable Long id) {
        return Response.success(reservationService.getById(id));
    }
    
    @PutMapping("/update")
    public Response<Boolean> updateReservation(@RequestBody ParkingReservation reservation) {
        return Response.success(reservationService.updateById(reservation));
    }
    
    @DeleteMapping("/{id}")
    public Response<Boolean> deleteReservation(@PathVariable Long id) {
        return Response.success(reservationService.removeById(id));
    }
}
