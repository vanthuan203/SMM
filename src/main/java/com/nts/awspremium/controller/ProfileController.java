package com.nts.awspremium.controller;

import com.nts.awspremium.model.Device;
import com.nts.awspremium.model.Profile;
import com.nts.awspremium.model.ProfileShow;
import com.nts.awspremium.repositories.DeviceRepository;
import com.nts.awspremium.repositories.ProfileRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/profile")
public class ProfileController {
    @Autowired
    private ProfileRepository profileRepository;
    @GetMapping(value = "get_List_Profile", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> test(@RequestParam(name = "device_id", required = false, defaultValue = "") String device_id
                                                   ) throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{

                List<ProfileShow> profiles =profileRepository.get_Profile_By_DeviceId(device_id.toString());

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < profiles.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("device_id", profiles.get(i).getDevice_id());
                obj.put("profile_id", profiles.get(i).getProfile_id());
                obj.put("add_time", profiles.get(i).getAdd_time());
                obj.put("update_time", profiles.get(i).getUpdate_time());
                obj.put("get_time", profiles.get(i).getGet_time());
                obj.put("num_account", profiles.get(i).getNum_account());
                obj.put("state", profiles.get(i).getState());
                obj.put("platform", profiles.get(i).getPlatform());
                obj.put("task", profiles.get(i).getTask());
                obj.put("state", profiles.get(i).getState());
                obj.put("running", profiles.get(i).getRunning());
                jsonArray.add(obj);
            }
            resp.put("profiles", jsonArray);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            System.out.println(stackTraceElement.getMethodName());
            System.out.println(stackTraceElement.getLineNumber());
            System.out.println(stackTraceElement.getClassName());
            System.out.println(stackTraceElement.getFileName());
            System.out.println("Error : " + e.getMessage());
            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }
}
