package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.VehicleRecord;
import com.parking.system.service.VehicleRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/vehicle-records")
@Api(tags = "车辆进出管理")
public class VehicleRecordController {

    @Resource
    private VehicleRecordService vehicleRecordService;

    private static final String UPLOAD_DIR = "uploads/plates/";

    @GetMapping
    @ApiOperation("获取所有车辆记录")
    public Response<List<VehicleRecord>> getAllRecords() {
        log.info("[成功][阶段2][获取所有车辆记录] 时间：{} | 参数：无", new Date());
        List<VehicleRecord> records = vehicleRecordService.getAllWithDetails();
        return Response.success("获取成功", records);
    }

    @GetMapping("/status/{status}")
    @ApiOperation("按状态查询车辆记录")
    public Response<List<VehicleRecord>> getByStatus(@PathVariable Integer status) {
        log.info("[成功][阶段2][按状态查询车辆记录] 时间：{} | 参数：status={}", new Date(), status);
        List<VehicleRecord> records = vehicleRecordService.getByStatus(status);
        return Response.success("获取成功", records);
    }

    @PostMapping("/recognize")
    @ApiOperation("上传车牌图片识别车牌号")
    public Response<Map<String, Object>> recognizePlate(@RequestParam("file") MultipartFile file) {
        log.info("[成功][阶段1][车牌识别请求] 时间：{} | 参数：fileName={}, fileSize={}", new Date(), file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            log.error("[失败][阶段1][车牌识别] 时间：{} | 原因：文件为空", new Date());
            return Response.error("请上传车牌图片");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            String imageUrl = "/api/vehicle-records/plates/" + fileName;

            byte[] imageData = file.getBytes();
            Map<String, Object> ocrResult = vehicleRecordService.recognizePlate(imageData);

            String plateNumber = (String) ocrResult.get("plateNumber");
            BigDecimal confidence = new BigDecimal(String.valueOf(ocrResult.getOrDefault("confidence", 0.0)));

            Map<String, Object> result = new HashMap<>();
            result.put("plateNumber", plateNumber);
            result.put("confidence", confidence);
            result.put("imageUrl", imageUrl);

            log.info("[成功][阶段4][车牌识别完成] 时间：{} | 结果：plateNumber={}, confidence={}", new Date(), plateNumber, confidence);
            return Response.success("识别成功", result);
        } catch (IOException e) {
            log.error("[失败][阶段2][车牌识别] 时间：{} | 原因：{} | 参数：fileName={}", new Date(), e.getMessage(), file.getOriginalFilename());
            return Response.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/entry")
    @ApiOperation("车辆入场")
    public Response<VehicleRecord> vehicleEntry(
            @RequestParam Long parkingLotId,
            @RequestParam String plateNumber,
            @RequestParam(required = false) String plateImageUrl,
            @RequestParam(required = false) BigDecimal confidence) {
        log.info("[成功][阶段1][车辆入场请求] 时间：{} | 参数：parkingLotId={}, plateNumber={}", new Date(), parkingLotId, plateNumber);
        try {
            VehicleRecord record = vehicleRecordService.vehicleEntry(parkingLotId, plateNumber, plateImageUrl, confidence);
            log.info("[成功][阶段4][车辆入场完成] 时间：{} | 结果：recordId={}", new Date(), record.getId());
            return Response.success("入场成功", record);
        } catch (RuntimeException e) {
            log.error("[失败][阶段2][车辆入场] 时间：{} | 原因：{} | 参数：parkingLotId={}, plateNumber={}", new Date(), e.getMessage(), parkingLotId, plateNumber);
            return Response.error(e.getMessage());
        }
    }

    @PostMapping("/entry-with-photo")
    @ApiOperation("上传车牌图片并入场")
    public Response<VehicleRecord> vehicleEntryWithPhoto(
            @RequestParam Long parkingLotId,
            @RequestParam("file") MultipartFile file) {
        log.info("[成功][阶段1][拍照入场请求] 时间：{} | 参数：parkingLotId={}, fileName={}", new Date(), parkingLotId, file.getOriginalFilename());

        if (file.isEmpty()) {
            log.error("[失败][阶段1][拍照入场] 时间：{} | 原因：文件为空", new Date());
            return Response.error("请上传车牌图片");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            String imageUrl = "/api/vehicle-records/plates/" + fileName;

            byte[] imageData = file.getBytes();
            Map<String, Object> ocrResult = vehicleRecordService.recognizePlate(imageData);

            String plateNumber = (String) ocrResult.get("plateNumber");
            BigDecimal confidence = new BigDecimal(String.valueOf(ocrResult.getOrDefault("confidence", 0.0)));

            VehicleRecord record = vehicleRecordService.vehicleEntry(parkingLotId, plateNumber, imageUrl, confidence);
            log.info("[成功][阶段4][拍照入场完成] 时间：{} | 结果：recordId={}, plateNumber={}", new Date(), record.getId(), plateNumber);
            return Response.success("入场成功", record);
        } catch (IOException e) {
            log.error("[失败][阶段2][拍照入场] 时间：{} | 原因：{} | 参数：parkingLotId={}", new Date(), e.getMessage(), parkingLotId);
            return Response.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/exit")
    @ApiOperation("车辆出场")
    public Response<VehicleRecord> vehicleExit(@PathVariable Long id) {
        log.info("[成功][阶段1][车辆出场请求] 时间：{} | 参数：recordId={}", new Date(), id);
        try {
            VehicleRecord record = vehicleRecordService.vehicleExit(id);
            log.info("[成功][阶段4][车辆出场完成] 时间：{} | 结果：recordId={}", new Date(), id);
            return Response.success("出场成功", record);
        } catch (RuntimeException e) {
            log.error("[失败][阶段2][车辆出场] 时间：{} | 原因：{} | 参数：recordId={}", new Date(), e.getMessage(), id);
            return Response.error(e.getMessage());
        }
    }

    @GetMapping("/plates/{fileName}")
    @ApiOperation("获取车牌图片")
    public org.springframework.http.ResponseEntity<byte[]> getPlateImage(@PathVariable String fileName) {
        log.info("[成功][阶段2][获取车牌图片] 时间：{} | 参数：fileName={}", new Date(), fileName);
        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.error("[失败][阶段2][获取车牌图片] 时间：{} | 原因：非法文件名 | 参数：fileName={}", new Date(), fileName);
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            if (!fileName.matches("[a-zA-Z0-9._-]+")) {
                log.error("[失败][阶段2][获取车牌图片] 时间：{} | 原因：文件名含非法字符 | 参数：fileName={}", new Date(), fileName);
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath)) {
                log.error("[失败][阶段2][获取车牌图片] 时间：{} | 原因：路径越界 | 参数：fileName={}", new Date(), fileName);
                return org.springframework.http.ResponseEntity.badRequest().build();
            }
            if (Files.exists(filePath)) {
                byte[] imageBytes = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "image/jpeg";
                }
                return org.springframework.http.ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(imageBytes);
            }
            log.error("[失败][阶段2][获取车牌图片] 时间：{} | 原因：文件不存在 | 参数：fileName={}", new Date(), fileName);
            return org.springframework.http.ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("[失败][阶段2][获取车牌图片] 时间：{} | 原因：{} | 参数：fileName={}", new Date(), e.getMessage(), fileName);
            return org.springframework.http.ResponseEntity.notFound().build();
        }
    }
}
