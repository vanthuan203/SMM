package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/device")

public class DeviceController {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private AccountRegTikTokRepository accountRegTikTokRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private HistoryTiktokRepository historyTiktokRepository;


    @PostMapping(value = "/add_device", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> add_device(@RequestBody Device device, @RequestHeader(defaultValue = "") String Authorization,
                                      @RequestParam(defaultValue = "1") Integer update) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            if (deviceRepository.checkDeviceId(device.getDevice_id()) >0) {
                    resp.put("status", "fail");
                    resp.put("message", "Device_id đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                Device device_new =new Device();
                device_new.setName(device.getName());
                device_new.setDevice_id(device.getDevice_id());
                device_new.setBox_name(device.getBox_name());
                device_new.setState(1);
                device_new.setTime_update(System.currentTimeMillis());
                deviceRepository.save(device_new);
                resp.put("status", "true");
                resp.put("message", "Insert device_id thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }



}
