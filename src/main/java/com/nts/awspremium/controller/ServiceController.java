package com.nts.awspremium.controller;

import com.nts.awspremium.model.Service;
import com.nts.awspremium.model.User;
import com.nts.awspremium.repositories.ServiceRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/service")
public class ServiceController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping(path = "get_List_Service",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Service(@RequestParam(defaultValue = "") String role){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<String > list_Service;
        if(role.equals("ROLE_ADMIN")){
            list_Service=serviceRepository.get_All_Service_Web();
        }else{
            list_Service=serviceRepository.get_All_Service_Enabled_Web();
        }
        String arr_Service="";
        for(int i=0;i<list_Service.size();i++){
            if(i==0){
                arr_Service=list_Service.get(0);
            }else{
                arr_Service=arr_Service+","+list_Service.get(i);
            }

        }
        resp.put("service",arr_Service);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);

    }
    @GetMapping(path = "get_Service_Web",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Service_Web(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        User user = userRepository.find_User_By_Token(Authorization);
        if(user==null){
            resp.put("status",false);
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Service> services;
        if(user.getRole().equals("ROLE_ADMIN")){
            services=serviceRepository.get_All_Service();
        }else{
            services=serviceRepository.get_All_Service_Enabled();
        }
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<services.size();i++){
            JSONObject obj = new JSONObject();
            obj.put("service_id", services.get(i).getService_id());
            obj.put("check_time", services.get(i).getCheck_time());
            obj.put("device_type", services.get(i).getDevice_type());
            obj.put("enabled", services.get(i).getEnabled());
            obj.put("expired", services.get(i).getExpired());
            obj.put("max_order", services.get(i).getMax_order());
            obj.put("max_quantity", services.get(i).getMax_quantity());
            obj.put("min_quantity", services.get(i).getMin_quantity());
            obj.put("max_time", services.get(i).getMax_time());
            obj.put("min_time", services.get(i).getMin_time());
            obj.put("note", services.get(i).getNote());
            obj.put("geo", services.get(i).getGeo());
            obj.put("platform", services.get(i).getPlatform());
            obj.put("refund", services.get(i).getRefund());
            obj.put("refund_time", services.get(i).getRefund_time());
            obj.put("service_category", services.get(i).getService_category());
            obj.put("service_name", services.get(i).getService_name());
            obj.put("service_rate", services.get(i).getService_rate());
            obj.put("service_type", services.get(i).getService_type());
            obj.put("task", services.get(i).getTask());
            obj.put("thread", services.get(i).getThread());
            obj.put("website_click_ads", services.get(i).getWebsite_click_ads());
            obj.put("website_click_web", services.get(i).getWebsite_click_web());
            obj.put("youtube_suggest", services.get(i).getYoutube_suggest());
            obj.put("youtube_direct", services.get(i).getYoutube_direct());
            obj.put("youtube_dtn", services.get(i).getYoutube_dtn());
            obj.put("youtube_embed", services.get(i).getYoutube_embed());
            obj.put("youtube_external", services.get(i).getYoutube_external());
            obj.put("youtube_key_niche", services.get(i).getYoutube_key_niche());
            obj.put("youtube_niche", services.get(i).getYoutube_niche());
            obj.put("youtube_playlists", services.get(i).getYoutube_playlists());
            obj.put("youtube_reply", services.get(i).getYoutube_reply());
            obj.put("youtube_search", services.get(i).getYoutube_search());
            obj.put("bonus", services.get(i).getBonus());
            obj.put("check_done", services.get(i).getCheck_done());
            obj.put("check_count", services.get(i).getCheck_count());
            jsonArray.add(obj);
        }
        resp.put("services",jsonArray);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);

    }
}
