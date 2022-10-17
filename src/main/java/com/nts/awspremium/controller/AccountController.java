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
import java.util.Optional;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/gmails")

public class AccountController {
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
            /*
            String datestring=newaccount.getEndtrialstring();
            if(datestring.contains("Free trial ends ")){
                datestring=datestring.replace("Free trial ends ","");
            }
            String MMM = datestring.substring(0,3);
            String date=datestring.replace(MMM,StringUtils.convertMMMtoMM(datestring)+"");
            Long endtrial= StringUtils.getLongTimeFromString(date,"MM dd, yyyy");
            */
            if(count>0){
                if(update==1) {
                    accountRepository.updateAccount(newaccount.getPassword(),newaccount.getRecover(),newaccount.getLive(),"","",newaccount.getEndtrial(),"", newaccount.getUsername());
                    cookieRepository.updateCookieSub(newaccount.getCookie(),newaccount.getUsername());
                    //encodefingerRepository.updateEncodefingerSub(newaccount.getEncodefinger(),newaccount.getUsername());
                    resp.put("status","true");
                    resp.put("message", "Update "+newaccount.getUsername()+" thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    resp.put("message", "Account "+newaccount.getUsername()+" đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }
            }else{
                accountRepository.insertAccount(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(),newaccount.getLive(),"","",newaccount.getEndtrial(),"");
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

    @PostMapping(value = "/updatecookiefinger",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatecookiefinger(@RequestBody Account newaccount,@RequestHeader(defaultValue = "") String Authorization,
                                         @RequestParam(defaultValue = "1") Integer update){
        JSONObject resp = new JSONObject();
        Integer checktoken= adminRepository.FindAdminByToken(Authorization);
        if(checktoken==0){

            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            cookieRepository.updateCookieSub(newaccount.getCookie(),newaccount.getUsername());
            encodefingerRepository.updateEncodefingerSub(newaccount.getEncodefinger(),newaccount.getUsername());
            resp.put("status","true");
            resp.put("message", "Update "+newaccount.getUsername()+" thành công!");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
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


            Long idbyVps=accountRepository.getaccountByVps("%"+vps.trim()+"%");
            if(idbyVps==null){
                Long id=accountRepository.getAccount();
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
                        account.get(0).setVps(vps.trim());
                        account.get(0).setRunning(1);
                        account.get(0).setTimecheck(System.currentTimeMillis());
                        accountRepository.save(account.get(0));

                        resp.put("status","true");
                        resp.put("username",account.get(0).getUsername());
                        resp.put("endtrial",account.get(0).getEndtrial());
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
                    resp.put("endtrial",accountbyVps.get(0).getEndtrial());
                    resp.put("recover",accountbyVps.get(0).getRecover());
                    resp.put("cookie",cookieSub);
                    resp.put("password",accountbyVps.get(0).getPassword());
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
                Long id=accountRepository.getAccountNeedLogin();
                List<Account> account=accountRepository.findAccountById(id);
                if(account.size()==0){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        if(account.get(0).getRunning()==1){
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


    @GetMapping(value = "/checkendtrial",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkendtrial(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization) {
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
            Integer accounts = accountRepository.findUsername(username);
            if (accounts == 0) {
                resp.put("status", "fail");
                resp.put("fail", "trial");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer accountcheck = accountRepository.checkEndTrial(username);
            if (accountcheck == 0) {
                resp.put("status", "fail");
                resp.put("fail", "trial");
                resp.put("message", "Username : " + username + " hết hạn premium!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                resp.put("status", "true");
                resp.put("message", "Username : " + username + " còn hạn premium!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
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


            Integer allgmail = accountRepository.getCountGmails();
                resp.put("counts", allgmail);
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/countgmailsbyendtrial",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> countgmailsbyendtrial(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            Integer checktoken = adminRepository.FindAdminByToken(Authorization);
            if (checktoken == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }


            Integer allgmail = accountRepository.getCountGmailsByEndtrial();
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
            Integer accounts = accountRepository.findUsername(username);
            if (accounts == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer checkEndTrial=accountRepository.checkEndTrial(username);
            if(checkEndTrial==0){
                resp.put("status", "fail");
                resp.put("fail", "trial");
                resp.put("message", "Hết hạn endtrial!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            Integer accountcheck = accountRepository.checkAcountByVps(username,"%"+vps.trim()+"%");
            if (accountcheck == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yều cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                accountRepository.updatetimecheck(System.currentTimeMillis(),username);
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
            Integer accounts=accountRepository.findUsername(username);
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

    @PostMapping(value = "/updatecookieloginlocal", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatecookieloginlocal(@RequestBody Account account,@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization ){
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
            Integer accounts=accountRepository.findUsername(username);
            if(accounts==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                cookieRepository.updateCookieSub(account.getCookie(),username.trim());
                accountRepository.updatecookieloginlocal(username.trim());
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
                    accounts.get(0).setPassword(password);
                }
                if(recover.length()>0){
                    accounts.get(0).setRecover(recover);
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
                    proxy=proxyRepository.getProxyUpdate();
                }else{
                   proxy=proxyRepository.getProxyUpdate(StringUtils.getProxyhost(histories.get(0).getProxy()));
                }

                histories.get(0).setProxy(proxy.get(0).getProxy());
                historyRepository.save(histories.get(0));
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
    @GetMapping(value = "/reset",produces = "application/hal_json;charset=utf8")
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
            List<Account> accounts=accountRepository.findAccountByUsername(username);
            if(accounts.size()==0) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                accounts.get(0).setLive(live);
                accounts.get(0).setRunning(0);
                accounts.get(0).setVps("");
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
            if (listacc.length() ==0  || listacc.isEmpty()) {
                resp.put("status", "fail");
                resp.put("message", "vps không đươc để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            accountRepository.updateListAccount("%"+vps.trim()+"%",listacc);
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


    @GetMapping(value = "/resetAccountByTimecheck",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetAccountByTimecheck() {
        JSONObject resp = new JSONObject();
        try {
            accountRepository.resetAccountByTimecheck();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}
