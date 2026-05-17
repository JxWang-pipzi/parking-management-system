package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
    
    @Select("SELECT * FROM payment_record WHERE out_trade_no = #{outTradeNo}")
    PaymentRecord selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

}