package com.nts.awspremium.controller;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.model.Service;
import com.nts.awspremium.model.User;
import com.nts.awspremium.repositories.LogErrorRepository;
import com.nts.awspremium.repositories.ServiceRepository;
import com.nts.awspremium.repositories.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/service")
public class ServiceController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @GetMapping(path = "get_List_Service",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_List_Service(@RequestParam(defaultValue = "") String role,@RequestParam(defaultValue = "") String platform){
        JSONObject resp = new JSONObject();
        try {
            List<String > list_Service;
            if(role.equals("ROLE_ADMIN")){
                list_Service=serviceRepository.get_All_Service_Web(platform);
            }else{
                list_Service=serviceRepository.get_All_Service_Enabled_Web(platform);
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
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "get_Service_Web",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get_Service_Web(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        try{
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
                obj.put("mode", services.get(i).getMode());
                jsonArray.add(obj);
            }
            resp.put("services",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(path = "get_Option_Service",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getOptionService(){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<String > task=serviceRepository.get_All_Task();
        List<String > type=serviceRepository.get_All_Type();
        List<String > platform=serviceRepository.get_All_Platform();
        String list_Task="";
        String list_Type="";
        String list_Platform="";
        for(int i=0;i<task.size();i++){
            if(i==0){
                list_Task=task.get(0);
            }else{
                list_Task=list_Task+","+task.get(i);
            }

        }
        resp.put("list_Task",list_Task);

        for(int i=0;i<type.size();i++){
            if(i==0){
                list_Type=type.get(0);
            }else{
                list_Type=list_Type+","+type.get(i);
            }

        }
        resp.put("list_Type",list_Type);
        for(int i=0;i<platform.size();i++){
            if(i==0){
                list_Platform=platform.get(0);
            }else{
                list_Platform=list_Platform+","+platform.get(i);
            }

        }
        resp.put("list_Platform",list_Platform);
        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
    }

    @PostMapping(path = "update_Service",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_Service(@RequestHeader(defaultValue = "") String Authorization,@RequestBody Service service_body){
        JSONObject resp = new JSONObject();
        try{
            Integer checktoken = userRepository.check_User_By_Token(Authorization);
            if(checktoken ==0){
                resp.put("status",false);
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Service service=serviceRepository.get_Service_Web(service_body.getService_id());
            service.setService_name(service_body.getService_name());
            service.setMin_quantity(service_body.getMin_quantity());
            service.setMax_quantity(service_body.getMax_quantity());
            service.setMax_order(service_body.getMax_order());
            service.setThread(service_body.getThread());
            service.setMin_time(service_body.getMin_time());
            service.setMax_time(service_body.getMax_time());
            service.setBonus(service_body.getBonus());
            service.setRefund(service_body.getRefund());
            service.setRefund_time(service_body.getRefund_time());
            service.setCheck_time(service_body.getCheck_time());
            service.setService_rate(service_body.getService_rate());
            service.setEnabled(service_body.getEnabled());

            if(service_body.getPlatform().trim().equals("youtube")){
                service.setYoutube_embed(service_body.getYoutube_embed());
                service.setYoutube_direct(service_body.getYoutube_direct());
                service.setYoutube_external(service_body.getYoutube_external());
                service.setYoutube_dtn(service_body.getYoutube_dtn());
                service.setYoutube_search(service_body.getYoutube_search());
                service.setYoutube_suggest(service_body.getYoutube_suggest());
            }
            serviceRepository.save(service);
            JSONObject obj = new JSONObject();
            obj.put("service_id", service.getService_id());
            obj.put("check_time", service.getCheck_time());
            obj.put("device_type", service.getDevice_type());
            obj.put("enabled", service.getEnabled());
            obj.put("expired", service.getExpired());
            obj.put("max_order", service.getMax_order());
            obj.put("max_quantity", service.getMax_quantity());
            obj.put("min_quantity", service.getMin_quantity());
            obj.put("max_time", service.getMax_time());
            obj.put("min_time", service.getMin_time());
            obj.put("note", service.getNote());
            obj.put("geo", service.getGeo());
            obj.put("platform", service.getPlatform());
            obj.put("refund", service.getRefund());
            obj.put("refund_time", service.getRefund_time());
            obj.put("service_category", service.getService_category());
            obj.put("service_name", service.getService_name());
            obj.put("service_rate", service.getService_rate());
            obj.put("service_type", service.getService_type());
            obj.put("task", service.getTask());
            obj.put("thread", service.getThread());
            obj.put("website_click_ads", service.getWebsite_click_ads());
            obj.put("website_click_web", service.getWebsite_click_web());
            obj.put("youtube_suggest", service.getYoutube_suggest());
            obj.put("youtube_direct", service.getYoutube_direct());
            obj.put("youtube_dtn", service.getYoutube_dtn());
            obj.put("youtube_embed", service.getYoutube_embed());
            obj.put("youtube_external", service.getYoutube_external());
            obj.put("youtube_key_niche", service.getYoutube_key_niche());
            obj.put("youtube_niche", service.getYoutube_niche());
            obj.put("youtube_playlists", service.getYoutube_playlists());
            obj.put("youtube_reply", service.getYoutube_reply());
            obj.put("youtube_search", service.getYoutube_search());
            obj.put("bonus", service.getBonus());
            obj.put("check_done", service.getCheck_done());
            obj.put("check_count", service.getCheck_count());
            obj.put("mode", service.getMode());
            resp.put("service",obj);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);
            resp.put("status",false);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}
