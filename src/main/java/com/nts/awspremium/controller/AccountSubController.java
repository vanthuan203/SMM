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
    private CheckProsetListTrue checkProsetListTrue;

    @Autowired CookieRepository cookieRepository;

    @PostMapping(value = "/create",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> createaccount(@RequestBody Account newaccount,
                                                 @RequestParam(defaultValue = "1") Integer update){
        JSONObject resp = new JSONObject();
        try{
            Long id= accountRepository.findIdByUsername(newaccount.getUsername().trim());
            if(id!=null){
                if(update==1) {
                    accountRepository.updateAccountSub(newaccount.getPassword(),newaccount.getRecover(),newaccount.getLive(),"","",id);
                    cookieRepository.updateCookieSub(newaccount.getCookie(),id);
                    resp.put("status","true");
                    resp.put("message", "Update "+newaccount.getUsername()+" thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    resp.put("message", "Account "+newaccount.getUsername()+" đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }
            }else{
                accountRepository.insertAccountSub(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(),newaccount.getLive(),"","", newaccount.getRunning(), newaccount.getVps(), newaccount.getDate());
                cookieRepository.insertCookieSub(newaccount.getUsername(),newaccount.getCookie());
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

    @PostMapping(value = "/updateallinfo",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateallinfo(@RequestBody Account newaccount) {
        JSONObject resp = new JSONObject();

        try {
            Long id = accountRepository.findIdByUsername(newaccount.getUsername().trim());
            if (id != null) {
                accountRepository.updateAllInfoAccSub(newaccount.getPassword(), newaccount.getRecover(), newaccount.getLive(), newaccount.getVps(),id);
                resp.put("status", "true");
                resp.put("message", "Update " + newaccount.getUsername() + " thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                accountRepository.insertAccountSub(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(), newaccount.getLive(), "", "", newaccount.getRunning(), newaccount.getVps(), newaccount.getDate());
                resp.put("status", "true");
                resp.put("message", "Insert " + newaccount.getUsername() + " thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getAccount(@RequestParam(defaultValue = "")  String vps){
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Tên vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if(checkProsetListTrue.getValue()>=20){
            resp.put("status","fail");
            resp.put("message","Get acc không thành công! Thử lại" );
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        /*
        if (vps.indexOf("WS") >= 0 || vps.indexOf("ws") >= 0 || vps.indexOf("Ws") >= 0 ||  vps.indexOf("wS") >= 0) {
            resp.put("status","fail");
            resp.put("message", "Hết tài khoản thỏa mãn!");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }

         */
        try {
            Long idbyVps=accountRepository.getAccountSubByVps(vps);
            if(idbyVps==null){
                Long id=accountRepository.getAccountSub();
                List<Account> account=accountRepository.findAccountById(id);
                if(account.size()==0){
                    resp.put("status", "fail");
                    resp.put("message", "Get account không thành công, thử lại sau ít phút!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }else{
                    try{
                        /*
                        Long idEncodefingerSub= encodefingerRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        String encodefingerSub= encodefingerRepository.findEncodefingerSubById(idEncodefingerSub);
                        int check_getfinger=0;
                        while (check_getfinger<=3){
                            if(encodefingerSub.length()<50){
                                Long idFinger= encodefingerRepository.getRandomIdSub();
                                encodefingerSub= encodefingerRepository.findEncodefingerSubById(idEncodefingerSub);
                                check_getfinger++;
                            }else {
                                break;
                            }
                        }
                        Long idCookieSub= cookieRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        String cookieSub= cookieRepository.findCookieSubById(idCookieSub);
                         */
                        Thread.sleep(300);
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
                        resp.put("cookie",cookieRepository.findCookieSubById(id));
                        //resp.put("encodefinger",encodefingerSub);
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
                    Thread.sleep(300);
                    Integer accountcheck=accountRepository.checkAccountById(idbyVps);
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
                    resp.put("cookie",cookieRepository.findCookieSubById(idbyVps));
                    //resp.put("encodefinger",encodefingerSub);
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

    @GetMapping(value = "/getloginbywhere", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getloginbywhere(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try {
            Long id=accountRepository.getAccountSubByWhere();
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
            Long idUsername=accountRepository.findIdUsername(username);
            if (idUsername == null) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer accountcheck = accountRepository.checkAcountByVps(idUsername,vps.trim());
            if (accountcheck == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yều cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                accountRepository.updatetimecheck(System.currentTimeMillis(),idUsername);
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
    ResponseEntity<String> getinfo(@RequestParam(defaultValue = "")  String username){
            JSONObject resp=new JSONObject();
            try{
                if(username.length()==0){
                    resp.put("status","fail");
                    resp.put("message", "Username không được để trống!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
                }
                Long idUsername=accountRepository.findIdUsername(username);
                if(idUsername==null){
                    resp.put("status","fail");
                    resp.put("message", "Username không tồn tại!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    String account=accountRepository.getInfo(idUsername);
                    String[] accountinfo=account.split(",");
                    resp.put("status","true");
                    //resp.put("username",accounts.get(0).getUsername());
                    resp.put("password",accountinfo[0]);
                    resp.put("recover",accountinfo[1]);
                    resp.put("oldpassword",accountinfo[2]);
                    resp.put("cookie",cookieRepository.findCookieSubById(idUsername));
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
            Long idUsername=accountRepository.findIdUsername(username);
            if(idUsername==null){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                accountRepository.updateTaskSub24h(done.trim(),idUsername);
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


    @PostMapping(value = "/updateinfo", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateinfo(@RequestBody Account account,@RequestParam(defaultValue = "")  String username){
        JSONObject resp=new JSONObject();
        try{
            if(username.length()==0){
                resp.put("status","fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
            Long idUsername=accountRepository.findIdUsername(username.trim());
            if(idUsername==null){
                createaccount(account,1);
                resp.put("status","true");
                resp.put("message", "Insert " +username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                List<Account> accounts=accountRepository.findAccountById(idUsername);
                if(account.getPassword().length()>0){
                    accounts.get(0).setPassword(account.getPassword().trim());
                }
                if(account.getOldpassword().length()>0){
                    accounts.get(0).setOldpassword(account.getOldpassword().trim());
                }
                if(account.getRecover().length()>0){
                    accounts.get(0).setRecover(account.getRecover().trim());
                }
                accounts.get(0).setLive(account.getLive());
                accountRepository.save(accounts.get(0));
                if(account.getCookie().length()>0){
                    cookieRepository.updateCookieByUsername(account.getCookie(),username.trim());
                }
                resp.put("status","true");
                resp.put("message", "Update info "+username+" thành công!");
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
            Long idUsername=accountRepository.findIdUsername(username.trim());
            if(idUsername==null){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
            else{
                List<Account> accounts=accountRepository.findAccountById(idUsername);
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
            Long idUsername=accountRepository.findIdUsername(username.trim());
            if(idUsername==null){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }

            List<Account> acccheckvpsnull=accountRepository.findAccountById(idUsername);
            Integer accountcheck = accountRepository.checkAcountByVps(idUsername,vps.trim());
            if (accountcheck == 0 && acccheckvpsnull.get(0).getVps().length()!=0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yêu cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            if (acccheckvpsnull.get(0).getVps().length()!=0){
                accountRepository.updateTaskSub(running,System.currentTimeMillis(),idUsername);
                resp.put("status","true");
                resp.put("message", "Update "+username+" thành công!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                acccheckvpsnull.get(0).setEndtrial(running);
                acccheckvpsnull.get(0).setEndtrialstring("0");
                acccheckvpsnull.get(0).setRunning(1);
                acccheckvpsnull.get(0).setLive(1);
                acccheckvpsnull.get(0).setVps(vps.trim());
                acccheckvpsnull.get(0).setTimecheck(System.currentTimeMillis());
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

    @GetMapping(value = "/updatethreadsuberror",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> updatethreadsuberror(){
        JSONObject resp=new JSONObject();
        try{
            accountRepository.updateThreadSubError();
            resp.put("status","true");
            resp.put("message", "Update thành công!");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);

        }catch(Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }


    }


    @GetMapping(value = "/updatelive",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> reset(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "0")  Integer live) {
        JSONObject resp = new JSONObject();
        try {

            if (username.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Long idUsername=accountRepository.findIdUsername(username.trim());
            if(idUsername==null) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                List<Account> accounts=accountRepository.findAccountById(idUsername);
                if(live==1){
                    accounts.get(0).setLive(live);
                    accounts.get(0).setRunning(1);
                }else {
                    accounts.get(0).setLive(live);
                    accounts.get(0).setRunning(0);
                    accounts.get(0).setEndtrial(0L);
                    accounts.get(0).setVps("");
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
    ResponseEntity<String> resetaccnotinvps(@RequestBody  String listacc,@RequestParam(defaultValue = "")  String vps) {
        JSONObject resp = new JSONObject();
        try {

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
    ResponseEntity<String> resetAccountByVps(@RequestParam(defaultValue = "")  String vps) {
        JSONObject resp = new JSONObject();
        try {
            if (vps.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Vps không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            accountRepository.resetAccountByVps(vps.trim()+"%");
            resp.put("status", "true");
            resp.put("message", "Update thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/resetaccountbyusername",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetaccountbyusername(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "0")  Integer live,@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            if (username.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Long idUsername=accountRepository.findIdUsername(username.trim());
            accountRepository.resetAccountByUsername(live,idUsername);
            resp.put("status", "true");
            resp.put("message", "Reset Account thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/resetLoginAccountSubByTimecheck",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetLoginAccountSubByTimecheck() {
        JSONObject resp = new JSONObject();
        try {
            accountRepository.resetLoginAccountSubByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/resetAccountSubByTimecheck",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetAccountSubByTimecheck() {
        JSONObject resp = new JSONObject();
        try {
            accountRepository.resetAccountSubByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updateAccSubDieToLiveByTimecheck",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> updateAccSubDieToLiveByTimecheck() {
        JSONObject resp = new JSONObject();
        try {
            accountRepository.updateAccSubDieToLiveByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}
