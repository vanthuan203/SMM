package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/sub")

public class AccountSubController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private CookieRepository cookieRepository;
    @Autowired
    private EncodefingerRepository encodefingerRepository;
    @PostMapping(value = "/create",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> createaccount(@RequestBody Account newaccount,@RequestHeader(defaultValue = "") String Authorization,
                                                 @RequestParam(defaultValue = "1") Integer update){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){

            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            Integer count= accountRepository.findUsername(newaccount.getUsername().trim());
            if(count>0){
                if(update==1) {
                    accountRepository.updateAccountSub(newaccount.getPassword(),newaccount.getRecover(),newaccount.getLive(),"","",newaccount.getUsername());
                    List<Account> accountcheck= accountRepository.findAccountByUsername(newaccount.getUsername().trim());
                    if(accountcheck.get(0).getLive()!=1){
                        cookieRepository.updateCookieSub(newaccount.getCookie(),newaccount.getUsername());
                    }
                    //encodefingerRepository.updateEncodefingerSub(newaccount.getEncodefinger(), newaccount.getUsername());
                    resp.put("status","true");
                    resp.put("message", "Update "+newaccount.getUsername()+" thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    resp.put("message", "Account "+newaccount.getUsername()+" đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }
            }else{
                accountRepository.insertAccountSub(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(),newaccount.getLive(),"","", newaccount.getRunning(), newaccount.getVps());
                cookieRepository.insertCookieSub(newaccount.getUsername(), newaccount.getCookie());
                encodefingerRepository.insertEncodefingerSub(newaccount.getUsername(), newaccount.getEncodefinger());
                resp.put("status","true");
                resp.put("message", "Insert "+newaccount.getUsername()+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getAccount(@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Tên vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long idbyVps=accountRepository.getAccountSubByVps(vps);
            if(idbyVps==null){
                Long id=accountRepository.getAccountSub();
                List<Account> account=accountRepository.findAccountById(id);
                if(account.size()==0){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        Long idEncodefingerSub= encodefingerRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        String encodefingerSub= encodefingerRepository.findEncodefingerSubById(idEncodefingerSub);
                        Long idCookieSub= cookieRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        String cookieSub= cookieRepository.findCookieSubById(idCookieSub);
                        Thread.sleep(3);
                        Integer accountcheck=accountRepository.checkAccountById(id);
                        if(accountcheck==0){
                            resp.put("status", "fail");
                            resp.put("message", "Get account không thành công, thử lại sau ítp phút!");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        //accountRepository.updateAccountGetByVPS(1,vps.);
                        account.get(0).setVps(vps.trim());
                        account.get(0).setRunning(1);
                        account.get(0).setTimecheck(System.currentTimeMillis());
                        accountRepository.save(account.get(0));
                        resp.put("status","true");
                        resp.put("username",account.get(0).getUsername());
                        //resp.put("endtrial",account.get(0).getEndtrial());
                        resp.put("password",account.get(0).getPassword());
                        resp.put("recover",account.get(0).getRecover());
                        resp.put("cookie",cookieSub);
                        resp.put("encodefinger",encodefingerSub);
                        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                    }catch(Exception e){
                        resp.put("status", "fail");
                        resp.put("message", e.getMessage());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
                    }
                }
            }else{
                try{
                    List<Account> accountbyVps=accountRepository.findAccountById(idbyVps);
                    Thread.sleep(3);
                    Integer accountcheck=accountRepository.checkAccountById(idbyVps);

                    Long idEncodefingerSub= encodefingerRepository.findIdSubByUsername(accountbyVps.get(0).getUsername().trim());
                    String encodefingerSub= encodefingerRepository.findEncodefingerSubById(idEncodefingerSub);

                    Long idCookieSub= cookieRepository.findIdSubByUsername(accountbyVps.get(0).getUsername().trim());
                    String cookieSub= cookieRepository.findCookieSubById(idCookieSub);
                    if(accountcheck==0){
                        resp.put("status", "fail");
                        resp.put("message", "Get account không thành công, thử lại sau ítp phút!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    accountbyVps.get(0).setVps(vps.trim());
                    accountbyVps.get(0).setRunning(1);
                    accountbyVps.get(0).setTimecheck(System.currentTimeMillis());
                    accountRepository.save(accountbyVps.get(0));
                    resp.put("status","true");
                    resp.put("username",accountbyVps.get(0).getUsername());
                    resp.put("password",accountbyVps.get(0).getPassword());
                    resp.put("recover",accountbyVps.get(0).getRecover());
                    resp.put("cookie",cookieSub);
                    resp.put("encodefinger",encodefingerSub);
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }catch (Exception e){
                    resp.put("status", "fail");
                    resp.put("message", e.getMessage());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
                }
            }
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping(value = "/getlogin", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getlogin(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
                Long id=accountRepository.getAccountSubNeedLogin();
                List<Account> account=accountRepository.findAccountById(id);
                if(account.size()==0){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        Thread.sleep(2);
                        Integer accountcheck=accountRepository.checkAccountById(id);
                        if(accountcheck==0){
                            resp.put("status", "fail");
                            resp.put("message", "Get account không thành công, thử lại sau ítp phút!");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        //account.get(0).setVps("");
                        account.get(0).setRunning(1);
                        accountRepository.save(account.get(0));

                        resp.put("status","true");
                        resp.put("username",account.get(0).getUsername());
                        resp.put("password",account.get(0).getPassword());
                        resp.put("recover",account.get(0).getRecover());
                        //resp.put("cookie",account.get(0).getCookie());
                        resp.put("encodefinger",encodefingerRepository.findEncodefingerSub(account.get(0).getUsername().trim()));
                        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                    }catch(Exception e){
                        resp.put("status", "fail");
                        resp.put("message", e.getMessage());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
                    }
                }
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping(value = "/countgmails",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> countgmails(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }


            Integer allgmail = accountRepository.getCountGmailsSub();
                resp.put("counts", allgmail);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/countgmailslive",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> countgmailslive(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }


            Integer allgmail = accountRepository.getCountGmailsSubLive();
            resp.put("counts", allgmail);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/countgmailsfullsub24h",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> countgmailsfullsub24h(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }


            Integer allgmail = accountRepository.getCountGmailsFullSub24h();
            resp.put("counts", allgmail);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/checkaccount",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkaccount(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (username.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (vps.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Vps không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Integer accounts = accountRepository.findUsername(username.trim());
            if (accounts == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

            Integer accountcheck = accountRepository.checkAcountByVps(username,vps.trim());
            if (accountcheck == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yều cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                accountRepository.updatetimecheck(System.currentTimeMillis(),username.trim());
                resp.put("status", "true");
                resp.put("message", "Check time user thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/getinfo",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getinfo(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization){
            JSONObject resp=new JSONObject();
            try{
                Integer checktoken= adminRepository.FindAdminByToken(Authorization);
                if(checktoken==0){

                    resp.put("status","fail");
                    resp.put("message", "Token het han!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
                }
                if(username.length()==0){
                    resp.put("status","fail");
                    resp.put("message", "Username không được để trống!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
                }
                Integer accounts=accountRepository.findUsername(username);
                if(accounts==0){
                    resp.put("status","fail");
                    resp.put("message", "Username không tồn tại!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    String account=accountRepository.getInfo(username);
                    String[] accountinfo=account.split(",");
                    resp.put("status","true");
                    //resp.put("username",accounts.get(0).getUsername());
                    resp.put("password",accountinfo[0]);
                    resp.put("recover",accountinfo[1]);
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }catch (Exception e){
                resp.put("status","fail");
                resp.put("message",e.getMessage());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
    }
    @GetMapping(value = "/updatetasksub24h",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatetasksub(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "")  String done,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){

                resp.put("status","fail");
                resp.put("message", "Token het han!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Integer accounts=accountRepository.findUsername(username);
            if(accounts==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                accountRepository.updateTaskSub24h(done.trim(),username.trim());
                resp.put("status","true");
                //resp.put("username",accounts.get(0).getUsername());
                resp.put("message","update thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message",e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/getcookie",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getcookie(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){

                resp.put("status","fail");
                resp.put("message", "Token het han!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Integer accounts=accountRepository.findUsername(username.trim());
            if(accounts==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                Integer cookie=accountRepository.getCookieAccSub(username.trim());
                if(cookie>0){
                    resp.put("status","true");
                    //resp.put("username",accounts.get(0).getUsername());
                    resp.put("cookie",cookieRepository.findCookieSub(username.trim()));
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    //resp.put("username",accounts.get(0).getUsername());
                    resp.put("message","Tài khoản live=0");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

            }
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message",e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping(value = "/updatecookie", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatecookie(@RequestBody Account account,@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization ){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Integer accounts=accountRepository.findUsername(username.trim());
            if(accounts==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{

                cookieRepository.updateCookieSub(account.getCookie(),username.trim());
                accountRepository.updateAccSubWhileCookieUpdate(username);
                resp.put("status","true");
                resp.put("message", "Update cookie "+username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);

            }
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping(value = "/update",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> update(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "")  String password,@RequestParam(defaultValue = "")  String recover ,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            List<Account> accounts=accountRepository.findAccountByUsername(username);
            if(accounts.size()==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
            else{
                if(password.length()>0){
                    accounts.get(0).setPassword(password.trim());
                }
                if(recover.length()>0){
                    accounts.get(0).setRecover(recover.trim());
                }
                accountRepository.save(accounts.get(0));
                resp.put("status","true");
                resp.put("message", "Update "+username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }


    }

    @GetMapping(value = "/updatetasksub",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> updatetasksub(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "")  String vps,@RequestParam(defaultValue = "0")  Long running,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Integer accounts=accountRepository.findUsername(username.trim());
            if(accounts==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
            List<Account> acccheckvpsnull=accountRepository.findAccountByUsername(username);
            Integer accountcheck = accountRepository.checkAcountByVps(username,vps.trim());
            if (accountcheck == 0 && acccheckvpsnull.get(0).getVps().length()!=0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yêu cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (acccheckvpsnull.get(0).getVps().length()!=0){
                accountRepository.updateTaskSub(running,username.trim());
                resp.put("status","true");
                resp.put("message", "Update "+username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                acccheckvpsnull.get(0).setEndtrial(running);
                acccheckvpsnull.get(0).setEndtrialstring("0");
                acccheckvpsnull.get(0).setRunning(1);
                acccheckvpsnull.get(0).setLive(1);
                acccheckvpsnull.get(0).setVps(vps.trim());
                accountRepository.save(acccheckvpsnull.get(0));
                resp.put("status","true");
                resp.put("message", "Update "+username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }


    }

    @GetMapping(value = "/deltasksuberror",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> deltasksuberror(@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(vps.length()==0){
                resp.put("status","fail");
                resp.put("message", "vps không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
                accountRepository.delTaskSubError(vps.trim());
                resp.put("status","true");
                resp.put("message", "Update "+vps+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }


    }
    @GetMapping(value = "/getproxy",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> getproxy(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp=new JSONObject();
        try{
            Integer checktoken= adminRepository.FindAdminByToken(Authorization);
            if(checktoken==0){
                resp.put("status","fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            List<History> histories=historyRepository.get(username);
            if(histories.size()==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                List<Proxy> proxy;
                if(histories.get(0).getProxy().length()==0 || histories.get(0).getProxy()==null){
                    //proxy=proxyRepository.getProxyTimeGetNull();
                    //if(proxy.size()==0){
                        proxy=proxyRepository.getProxy();
                    //}
                }else{
                    //proxy=proxyRepository.getProxyTimeGetNull(StringUtils.getProxyhost(histories.get(0).getProxy()));
                    //if(proxy.size()==0){
                        proxy=proxyRepository.getProxy(StringUtils.getProxyhost(histories.get(0).getProxy()));
                    //}
                }
                List<Proxy> proxys=proxyRepository.findProxy(histories.get(0).getProxy());
                if(proxys.get(0).getRunning()>=1){
                    proxys.get(0).setRunning(proxys.get(0).getRunning()-1);
                    proxyRepository.save(proxys.get(0));
                }

                ProxyHistory proxyHistory =new ProxyHistory();
                proxyHistory.setId(System.currentTimeMillis());
                proxyHistory.setProxy(histories.get(0).getProxy());
                proxyHistory.setIpv4(StringUtils.getProxyhost(histories.get(0).getProxy()));
                proxyHistory.setState(0);
                proxyHistoryRepository.save(proxyHistory);

                ProxyHistory proxyHistoryNew =new ProxyHistory();
                proxyHistoryNew.setId(System.currentTimeMillis());
                proxyHistoryNew.setProxy(proxy.get(0).getProxy());
                proxyHistoryNew.setIpv4(proxy.get(0).getIpv4());
                proxyHistoryNew.setState(1);
                proxyHistoryRepository.save(proxyHistoryNew);

                IpV4 ipV4=new IpV4();
                ipV4.setId(System.currentTimeMillis());
                ipV4.setIpv4(proxy.get(0).getIpv4());
                ipV4.setState(1);
                ipV4Repository.save(ipV4);

                histories.get(0).setProxy(proxy.get(0).getProxy());
                historyRepository.save(histories.get(0));
                proxy.get(0).setRunning(proxy.get(0).getRunning()+1);
                proxy.get(0).setTimeget(System.currentTimeMillis());
                proxyRepository.save(proxy.get(0));
                resp.put("status","true");
                resp.put("proxy",proxy.get(0).getProxy());
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/updatelive",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> reset(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "0")  Integer live,@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (username.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            List<Account> accounts=accountRepository.findAccountByUsername(username.trim());
            if(accounts.size()==0) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                if(live==1){
                    accounts.get(0).setLive(live);
                    accounts.get(0).setRunning(1);
                }else {
                    accounts.get(0).setLive(live);
                    accounts.get(0).setRunning(0);
                    accounts.get(0).setEndtrial(0L);
                }
                accountRepository.save(accounts.get(0));
                resp.put("status", "true");
                resp.put("message", "Reset account thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "/resetaccnotinvps",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetaccnotinvps(@RequestBody  String listacc,@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "")  String vps) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (vps.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "vps không đươc để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            accountRepository.updateListAccount(vps.trim(),listacc);
            resp.put("status", "true");
            resp.put("message", listacc);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/resetaccountbyvps",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetAccountByVps(@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            if (vps.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Vps không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            accountRepository.resetAccountByVps("%"+vps.trim()+"%");
            resp.put("status", "true");
            resp.put("message", "Update thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}
