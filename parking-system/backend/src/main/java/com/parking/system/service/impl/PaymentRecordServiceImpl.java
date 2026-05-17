package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.mapper.PaymentRecordMapper;
import com.parking.system.service.PaymentRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {

    @Override
    public List<PaymentRecord> getRecordsByUserId(Long userId) {
        QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return list(wrapper);
    }

    @Override
    public PaymentRecord getRecordByOrderId(Long orderId) {
        QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return getOne(wrapper);
    }

}