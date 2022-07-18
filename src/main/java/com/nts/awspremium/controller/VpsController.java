package com.nts.awspremium.controller;

import com.nts.awspremium.model.Admin;
import com.nts.awspremium.model.Vps;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.VpsRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping(path ="/vps")
public class VpsController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private VpsRepository vpsRepository;
    @GetMapping(value = "list",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getlist(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vps=vpsRepository.getListVPS();
            if(vps.size()==0){
                resp.put("status","fail");
                resp.put("message", "Vps trống");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }else{
                JSONArray jsonArray= new JSONArray();

                //JSONObject jsonObject=new JSONObject().put("")
                //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
                //jsonArray.add(orderRunnings);

                for(int i=0;i<vps.size();i++){
                    JSONObject obj = new JSONObject();
                    obj.put("id", vps.get(i).getId());
                    obj.put("vps", vps.get(i).getVps());
                    obj.put("vpsoption",  vps.get(i).getVpsoption());
                    obj.put("state",  vps.get(i).getState());
                    jsonArray.add(obj);
                }
                resp.put("accounts",jsonArray);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "addvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> addvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vps1 =vpsRepository.findVPS(vps);
            if(vps1.size()>0){
                resp.put("status", "fail");
                resp.put("message", "Vps đã tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(1);
                vpsnew.setRunning(0);
                vpsnew.setVpsoption("");
                vpsnew.setUrlapi("");
                vpsnew.setToken("");
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("message", "Vps thêm thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "checkvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkvps(@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            List<Vps> vps1 =vpsRepository.findVPS(vps);
            if(vps1.size()>0){
                vps1.get(0).setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vps1.get(0));
                resp.put("status", "true");
                resp.put("message", "Cập nhật vps thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                Vps vpsnew=new Vps();
                vpsnew.setVps(vps);
                vpsnew.setState(1);
                vpsnew.setRunning(0);
                vpsnew.setVpsoption("");
                vpsnew.setUrlapi("");
                vpsnew.setToken("");
                vpsnew.setTimecheck(System.currentTimeMillis());
                vpsRepository.save(vpsnew);
                resp.put("status", "true");
                resp.put("message", "Vps thêm thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}
