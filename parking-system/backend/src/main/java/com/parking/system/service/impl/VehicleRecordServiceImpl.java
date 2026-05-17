package com.parking.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.entity.VehicleRecord;
import com.parking.system.mapper.VehicleRecordMapper;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import com.parking.system.service.VehicleRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Slf4j
@Service
public class VehicleRecordServiceImpl extends ServiceImpl<VehicleRecordMapper, VehicleRecord> implements VehicleRecordService {

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Override
    public List<VehicleRecord> getAllWithDetails() {
        log.info("[成功][阶段2][查询所有车辆记录] 时间：{} | 参数：无 | 结果：查询完成", new Date());
        return baseMapper.findAllWithDetails();
    }

    @Override
    public List<VehicleRecord> getByStatus(Integer status) {
        log.info("[成功][阶段2][按状态查询车辆记录] 时间：{} | 参数：status={} | 结果：查询完成", new Date(), status);
        return baseMapper.findByStatus(status);
    }

    @Override
    @Transactional
    public VehicleRecord vehicleEntry(Long parkingLotId, String plateNumber, String plateImageUrl, BigDecimal confidence) {
        log.info("[成功][阶段1][车辆入场] 时间：{} | 参数：parkingLotId={}, plateNumber={}", new Date(), parkingLotId, plateNumber);

        ParkingLot lot = parkingLotService.getById(parkingLotId);
        if (lot == null) {
            log.error("[失败][阶段1][车辆入场] 时间：{} | 原因：停车场不存在 | 参数：parkingLotId={}", new Date(), parkingLotId);
            throw new RuntimeException("停车场不存在");
        }
        if (lot.getAvailableSpaces() <= 0) {
            log.error("[失败][阶段1][车辆入场] 时间：{} | 原因：停车场已满 | 参数：parkingLotId={}", new Date(), parkingLotId);
            throw new RuntimeException("停车场已满，无法入场");
        }

        VehicleRecord record = new VehicleRecord();
        record.setParkingLotId(parkingLotId);
        record.setPlateNumber(plateNumber);
        record.setEntryTime(new Date());
        record.setStatus(0);
        record.setPlateImageUrl(plateImageUrl);
        record.setRecognitionConfidence(confidence);

        Long allocatedSpaceId = parkingSpaceService.atomicAllocateSpace(parkingLotId, 1);
        if (allocatedSpaceId != null) {
            record.setParkingSpaceId(allocatedSpaceId);
            log.info("[成功][阶段2][分配车位] 时间：{} | 参数：spaceId={} | 结果：车位已分配", new Date(), allocatedSpaceId);
        } else {
            log.warn("[失败][阶段2][分配车位] 时间：{} | 原因：无空闲车位 | 参数：parkingLotId={}", new Date(), parkingLotId);
        }

        lot.setAvailableSpaces(lot.getAvailableSpaces() - 1);
        parkingLotService.updateById(lot);

        save(record);
        log.info("[成功][阶段4][车辆入场完成] 时间：{} | 参数：plateNumber={} | 结果：recordId={}", new Date(), plateNumber, record.getId());
        return record;
    }

    @Override
    @Transactional
    public VehicleRecord vehicleExit(Long recordId) {
        log.info("[成功][阶段1][车辆出场] 时间：{} | 参数：recordId={}", new Date(), recordId);

        VehicleRecord record = getById(recordId);
        if (record == null) {
            log.error("[失败][阶段1][车辆出场] 时间：{} | 原因：记录不存在 | 参数：recordId={}", new Date(), recordId);
            throw new RuntimeException("记录不存在");
        }
        if (record.getStatus() == 1) {
            log.error("[失败][阶段1][车辆出场] 时间：{} | 原因：车辆已离场 | 参数：recordId={}", new Date(), recordId);
            throw new RuntimeException("车辆已离场");
        }

        record.setExitTime(new Date());
        record.setStatus(1);

        long durationMs = record.getExitTime().getTime() - record.getEntryTime().getTime();
        long durationMinutes = durationMs / (1000 * 60);
        if (durationMinutes < 60) {
            durationMinutes = 60;
        }

        if (record.getParkingSpaceId() != null) {
            ParkingSpace space = parkingSpaceService.getById(record.getParkingSpaceId());
            if (space != null) {
                space.setStatus(0);
                parkingSpaceService.updateById(space);
                log.info("[成功][阶段2][释放车位] 时间：{} | 参数：spaceId={} | 结果：车位已释放", new Date(), space.getId());
            }
        }

        ParkingLot lot = parkingLotService.getById(record.getParkingLotId());
        if (lot != null) {
            lot.setAvailableSpaces(lot.getAvailableSpaces() + 1);
            parkingLotService.updateById(lot);
        }

        updateById(record);
        log.info("[成功][阶段4][车辆出场完成] 时间：{} | 参数：recordId={} | 结果：停车时长={}分钟", new Date(), recordId, durationMinutes);
        return record;
    }

    @Value("${ocr.service.url:http://127.0.0.1:5000/recognize}")
    private String ocrServiceUrl;

    @Override
    public Map<String, Object> recognizePlate(byte[] imageData) {
        log.info("[成功][阶段2][车牌识别] 时间：{} | 参数：imageDataSize={}bytes", new Date(), imageData != null ? imageData.length : 0);

        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                setConnectTimeout(5000);
                setReadTimeout(30000);
            }});

            File tempFile = File.createTempFile("plate_", ".jpg");
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), imageData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.FileSystemResource(tempFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("[成功][阶段2][调用OCR服务] 时间：{} | 参数：url={}", new Date(), ocrServiceUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, String.class);
            tempFile.delete();

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResult = JSON.parseObject(response.getBody());
                Integer code = jsonResult.getInteger("code");

                if (code != null && code == 200) {
                    JSONObject data = jsonResult.getJSONObject("data");
                    if (data != null) {
                        String plateNumber = data.getString("plateNumber");
                        Double confidence = data.getDouble("confidence");

                        if (plateNumber != null && !plateNumber.isEmpty()) {
                            result.put("plateNumber", plateNumber);
                            result.put("confidence", confidence != null ? confidence : 0.0);
                            log.info("[成功][阶段4][车牌识别完成] 时间：{} | 结果：plateNumber={}, confidence={}%", new Date(), plateNumber, confidence);
                            return result;
                        } else {
                            log.warn("[失败][阶段4][车牌识别] 时间：{} | 原因：OCR未识别到车牌", new Date());
                        }
                    }
                }
            }

            log.warn("[失败][阶段2][车牌识别] 时间：{} | 原因：OCR服务返回异常", new Date());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("[失败][阶段2][车牌识别] 时间：{} | 原因：OCR服务不可用({}) - {}", new Date(), ocrServiceUrl, e.getMessage());
        } catch (Exception e) {
            log.error("[失败][阶段2][车牌识别] 时间：{} | 原因：OCR服务调用失败 - {}", new Date(), e.getMessage());
        }

        result.put("plateNumber", null);
        result.put("confidence", 0.0);
        result.put("ocrFailed", true);
        log.info("[失败][阶段4][车牌识别] 时间：{} | 结果：OCR识别失败，需手动输入", new Date());
        return result;
    }

    private String simulatePlateRecognition() {
        String[] provinces = {"京", "沪", "粤", "苏", "浙", "鲁", "豫", "川", "渝", "湘", "鄂", "闽", "赣", "皖", "冀", "辽", "吉", "黑"};
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        Random random = new Random();
        String province = provinces[random.nextInt(provinces.length)];
        String letter = letters[random.nextInt(letters.length)];
        StringBuilder plateSuffix = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i == 0 || random.nextBoolean()) {
                plateSuffix.append(random.nextInt(10));
            } else {
                plateSuffix.append(letters[random.nextInt(letters.length)]);
            }
        }
        return province + letter + plateSuffix.toString();
    }
}
