package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/acc_tiktok")

public class AccountTikTokController {
    @Autowired
    private AccountTikTokRepository accountRepository;
    @Autowired
    private AccountRegTikTokRepository accountRegTikTokRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private HistoryTiktokRepository historyTiktokRepository;

    @Autowired
    private HistoryTrafficRepository historyTrafficRepository;

    @Autowired
    private HistoryCommentRepository historyCommentRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private RecoverRepository recoverRepository;

    @Autowired
    private CheckProsetListTrue checkProsetListTrue;

    @PostMapping(value = "/create", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> create(@RequestBody AccountTiktok account, @RequestHeader(defaultValue = "") String Authorization,
                                       @RequestParam(defaultValue = "1") Integer update) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            AccountRegTiktok accountRegTiktok = accountRegTikTokRepository.checkUsername(account.getUsername().trim());
            if (accountRegTiktok != null) {
                if (update == 1) {
                    accountRegTiktok.setLive(1);
                    accountRegTiktok.setPassword(account.getPassword().trim());
                    accountRegTiktok.setRecover(account.getRecover().trim());
                    accountRegTikTokRepository.save(accountRegTiktok);
                    resp.put("status", "true");
                    resp.put("message", "Update " + account.getUsername() + " thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    resp.put("status", "fail");
                    resp.put("message", "Account " + account.getUsername() + " đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            } else {
                AccountRegTiktok accountTiktokNew =new AccountRegTiktok();
                accountTiktokNew.setUsername(account.getUsername().trim());
                accountTiktokNew.setLive(1);
                accountTiktokNew.setPassword(account.getPassword().trim());
                accountTiktokNew.setRecover(account.getRecover().trim());
                accountTiktokNew.setTime_add(System.currentTimeMillis());
                accountTiktokNew.setTime_check(0L);
                accountTiktokNew.setDevice_id("");
                accountTiktokNew.setProxy("");
                accountTiktokNew.setRunning(account.getRunning());
                accountTiktokNew.setVps("");
                accountRegTikTokRepository.save(accountTiktokNew);
                //add history tiktok
                resp.put("status", "true");
                resp.put("message", "Insert " + account.getUsername() + " thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/add_account", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> add_account(@RequestBody AccountTiktok account, @RequestHeader(defaultValue = "") String Authorization,
                                         @RequestParam(defaultValue = "1") Integer update) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            AccountTiktok accountTiktok = accountRepository.checkUsername(account.getUsername().trim());
            if (accountTiktok != null) {
                if (update == 1) {
                    accountTiktok.setLive(account.getLive());
                    accountTiktok.setPassword(account.getPassword().trim());
                    accountTiktok.setRecover(account.getRecover().trim());
                    accountRepository.save(accountTiktok);
                    resp.put("status", "true");
                    resp.put("message", "Update " + account.getUsername() + " thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    resp.put("status", "fail");
                    resp.put("message", "Account " + account.getUsername() + " đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            } else {
                AccountTiktok accountTiktokNew =new AccountTiktok();
                accountTiktokNew.setUsername(account.getUsername().trim());
                accountTiktokNew.setNick_name(account.getNick_name().trim());
                accountTiktokNew.setLive(account.getLive());
                accountTiktokNew.setPassword(account.getPassword().trim());
                accountTiktokNew.setRecover(account.getRecover().trim());
                accountTiktokNew.setTime_add(System.currentTimeMillis());
                accountTiktokNew.setTime_check(0L);
                accountTiktokNew.setDevice_id(account.getDevice_id().trim());
                accountTiktokNew.setProxy("");
                accountTiktokNew.setRunning(account.getRunning());
                accountTiktokNew.setVps(account.getVps().trim());
                accountRepository.save(accountTiktokNew);
                //add history tiktok
                if(historyTiktokRepository.checkUsername(account.getUsername().trim())==0){
                    HistoryTikTok historyTikTok =new HistoryTikTok();
                    historyTikTok.setUsername(account.getUsername().trim());
                    historyTikTok.setOption_running(account.getRunning());
                    historyTikTok.setDevice_id(account.getDevice_id());
                    historyTikTok.setRunning(0);
                    historyTikTok.setVps(account.getVps().trim());
                    historyTikTok.setOrderid(0L);
                    historyTikTok.setTimeget(0L);
                    historyTiktokRepository.save(historyTikTok);
                }
                resp.put("status", "true");
                resp.put("message", "Insert " + account.getUsername() + " thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/check_account", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> check_account(@RequestParam(defaultValue = "") String username) {
        JSONObject resp = new JSONObject();
        if (username.trim().length() == 0) {
            resp.put("status", "fail");
            resp.put("message","username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            if(accountRepository.findIdByUsername(username.trim())>0){
                resp.put("status", "true");
                resp.put("message",username.trim() + " đã tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status", "fail");
                resp.put("message",username.trim() + " không tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getCountByVps", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getCountByVps() {
        JSONObject resp = new JSONObject();
        try {
            List<String> vps=accountRepository.countbyVPS();
            for(int i=0;i<vps.size();i++){
                resp.put(vps.get(i).split(",")[0], vps.get(i).split(",")[1]);
            }
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping(value = "/update_live_tiktok", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_live_tiktok(@RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "-1") Integer live) {
        JSONObject resp = new JSONObject();
        if (username.trim().length() == 0) {
            resp.put("status", "fail");
            resp.put("message","username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (live == -1) {
            resp.put("status", "fail");
            resp.put("message","live không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            AccountTiktok accountTiktok=accountRepository.findAccountByUsername(username.trim());
            if(accountTiktok!=null){
                accountTiktok.setLive(live);
                accountRepository.save(accountTiktok);
                resp.put("status", "true");
                resp.put("message","update live thành công");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status", "fail");
                resp.put("message",username.trim() + " không tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/update_live_reg", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update_live_reg(@RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "-1") Integer live) {
        JSONObject resp = new JSONObject();
        if (username.trim().length() == 0) {
            resp.put("status", "fail");
            resp.put("message","username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (live == -1) {
            resp.put("status", "fail");
            resp.put("message","live không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            AccountRegTiktok accountRegTiktok=accountRegTikTokRepository.checkUsername(username.trim());
            if(accountRegTiktok!=null){
                accountRegTiktok.setLive(live);
                accountRegTiktok.setVps("");
                accountRegTiktok.setDevice_id("");
                accountRegTiktok.setRunning(0);
                accountRegTikTokRepository.save(accountRegTiktok);
                resp.put("status", "true");
                resp.put("message","update live thành công");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status", "fail");
                resp.put("message",username.trim() + " không tồn tại");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/reg_acc", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> reg_acc(@RequestParam(defaultValue = "") String device_id,@RequestParam(defaultValue = "") String vps, @RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        Integer checktoken = adminRepository.FindAdminByToken(Authorization);
        List<Vps> vps_check =vpsRepository.findVPS(vps.trim());
        if(vps_check.size()==0){
            Vps vpsnew=new Vps();
            vpsnew.setVps(vps.trim());
            vpsnew.setState(1);
            vpsnew.setRunning(0);
            vpsnew.setVpsoption("tiktok");
            vpsnew.setUrlapi("");
            vpsnew.setToken("");
            vpsnew.setTimecheck(System.currentTimeMillis());
            vpsRepository.save(vpsnew);
        }
        if (checktoken == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (device_id.trim().length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "device_id không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (vps.trim().length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            if(accountRepository.CheckRegByDeviceId(device_id.trim())>0||accountRepository.getCountByDeviceId(device_id.trim())==0){
                String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                String code="";
                Random ran=new Random();
                for(int i=0;i<50;i++){
                    Integer ranver=ran.nextInt(stringrand.length());
                    code=code+stringrand.charAt(ranver);
                }
                AccountRegTiktok accountRegTiktok=accountRegTikTokRepository.getAccountRegTiktok(vps.trim(),device_id.trim(),System.currentTimeMillis(),code);
                if(accountRegTiktok!=null){
                    resp.put("status", "true");
                    resp.put("acc", accountRegTiktok.getUsername()+"|"+accountRegTiktok.getPassword()+"|"+accountRegTiktok.getRecover()+"|"+(accountRegTiktok.getAuthy()==null?"":accountRegTiktok.getAuthy().trim()));
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    resp.put("status", "fail");
                    resp.put("message","Hết acc để reg reg_tiktok");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

            }else{
                resp.put("status", "fail");
                resp.put("message","Đã reg đủ acc");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }



}
