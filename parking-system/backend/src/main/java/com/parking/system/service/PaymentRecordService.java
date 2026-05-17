package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.PaymentRecord;

import java.util.List;

public interface PaymentRecordService extends IService<PaymentRecord> {

    List<PaymentRecord> getRecordsByUserId(Long userId);

    PaymentRecord getRecordByOrderId(Long orderId);

}