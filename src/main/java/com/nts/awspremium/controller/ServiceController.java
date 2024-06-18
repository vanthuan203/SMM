package com.nts.awspremium.controller;

import com.nts.awspremium.model.User;
import com.nts.awspremium.repositories.ServiceRepository;
import com.nts.awspremium.repositories.UserRepository;
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
}
