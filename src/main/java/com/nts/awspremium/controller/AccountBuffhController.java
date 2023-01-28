package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.History;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ProxyHistory;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/buffh")

public class AccountBuffhController {
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

    @Autowired
    private VpsRepository vpsRepository;

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
                    accountRepository.updateAccountBuffh(newaccount.getPassword(),newaccount.getRecover(),newaccount.getLive(),"","",newaccount.getUsername());
                    cookieRepository.updateCookieBuffh(newaccount.getCookie(),newaccount.getUsername());
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
                accountRepository.insertAccountBuffh(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(),newaccount.getLive(),"","",0,"",newaccount.getDate(),newaccount.getGeo());
                cookieRepository.insertCookieBuffh(newaccount.getUsername(), newaccount.getCookie());
                //encodefingerRepository.insertEncodefingerSub(newaccount.getUsername(), newaccount.getEncodefinger());

                Long historieId= historyRepository.getId(newaccount.getUsername().trim());
                List<History> histories=historyRepository.getHistoriesById(historieId);
                if(histories.size()==0){
                    History history=new History();
                    history.setId(System.currentTimeMillis());
                    history.setUsername(newaccount.getUsername());
                    history.setListvideo("");
                    history.setChannelid("");
                    history.setProxy("");
                    history.setRunning(0);
                    history.setVideoid("");
                    history.setVps("");
                    history.setTimeget(0L);
                    history.setGeo(newaccount.getGeo());
                    historyRepository.save(history);
                }
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
    ResponseEntity<String> getAccount(@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "0") Integer test){
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
            Integer check_get= vpsRepository.checkGetAccountByThreadVps(vps.trim()+"%");
            if(check_get==0){
                resp.put("status","fail");
                resp.put("message", "Đã đủ acc cho Vps!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }

            Long idbyVps=accountRepository.getaccountByVps(vps.trim()+"%");
            if(idbyVps==null){
                //Thread.sleep((long)(Math.random() * 10000));
                Long id=-0L;
                /*
                Pattern  pattern = Pattern.compile("[a-zA-Z]");
                if(pattern.matcher(vps.trim()).find()){
                    if(test==1){
                        id=accountRepository.getAccountBuffhGmail("vn");
                    }else if(test==2){
                        id=accountRepository.getAccountBuffhGmail("us");
                    }else{
                        id=accountRepository.getAccountBuffhGmail("us");
                    }
                    List<Account> account=accountRepository.findAccountById(id);
                    if(id==0L){
                        if(test==1){
                            id=accountRepository.getAccountBuffhDomain("vn");
                        }else if(test==2){
                            id=accountRepository.getAccountBuffhDomain("us");
                        }else{
                            id=accountRepository.getAccountBuffhDomain("us");
                        }
                    }

                 */

                    if(test==1){
                        id=accountRepository.getAccountBuffhDomain("vn");
                    }else if(test==2){
                        id=accountRepository.getAccountBuffhDomain("us");
                    }else{
                        id=accountRepository.getAccountBuffhDomain("us");
                    }
                List<Account> account=accountRepository.findAccountById(id);
                if(account.size()==0){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        //Long idEncodefingerSub= encodefingerRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        //String encodefingerSub= encodefingerRepository.findEncodefingerSubById(idEncodefingerSub);
                        Long idCookieSub= cookieRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        String cookieSub= cookieRepository.findCookieSubById(idCookieSub);
                        Thread.sleep(2);
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

                        //Save vps to history
                        Long historieId= historyRepository.getId(account.get(0).getUsername().trim());
                        List<History> histories=historyRepository.getHistoriesById(historieId);
                        if(histories.size()==0){
                            History history=new History();
                            history.setId(System.currentTimeMillis());
                            history.setUsername(account.get(0).getUsername().trim());
                            history.setListvideo("");
                            history.setChannelid("");
                            history.setProxy("");
                            history.setRunning(0);
                            history.setVps(vps);
                            history.setTimeget(0L);
                            history.setGeo(account.get(0).getGeo());
                            historyRepository.save(history);
                        }else{
                            histories.get(0).setVps(vps);
                            historyRepository.save(histories.get(0));
                        }



                        resp.put("status","true");
                        resp.put("username",account.get(0).getUsername());
                        //resp.put("endtrial",account.get(0).getEndtrial());
                        resp.put("password",account.get(0).getPassword());
                        resp.put("recover",account.get(0).getRecover());
                        resp.put("cookie",cookieSub);
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
                    Thread.sleep(100);
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

                    //Save vps to history
                    Long historieId= historyRepository.getId(accountbyVps.get(0).getUsername().trim());
                    List<History> histories=historyRepository.getHistoriesById(historieId);
                    if(histories.size()==0){
                        History history=new History();
                        history.setId(System.currentTimeMillis());
                        history.setUsername(accountbyVps.get(0).getUsername().trim());
                        history.setListvideo("");
                        history.setChannelid("");
                        history.setProxy("");
                        history.setRunning(0);
                        history.setVps(vps);
                        history.setTimeget(0L);
                        history.setGeo(accountbyVps.get(0).getGeo());
                        historyRepository.save(history);
                    }else{
                        histories.get(0).setVps(vps);
                        historyRepository.save(histories.get(0));
                    }

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


            Integer allgmail = accountRepository.getCountGmailLiveBuffh();
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


            Integer allgmail = accountRepository.getCountGmailBuffh();
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
            //Thread.sleep((long)(Math.random() * 10000));
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

            Long id_username = accountRepository.findIdByUsername(username);
            if (id_username==null) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

            Integer accountcheck = accountRepository.checkIdByVps(id_username,"%"+vps.trim()+"%");
            if (accountcheck == 0) {
                resp.put("status", "fail");
                resp.put("fail", "nouser");
                resp.put("message", "Yều cầu lấy tài khoản khác");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                accountRepository.updateTimecheckById(System.currentTimeMillis(),id_username);
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
    ResponseEntity<String> getproxy(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization,@RequestParam(defaultValue = "0") Integer test){
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
                if(histories.get(0).getRunning()==1){
                    Integer proxyId= proxyRepository.getIdByProxy(histories.get(0).getProxy().trim());
                    proxyRepository.updaterunning(proxyId);
                }
                List<Proxy> proxy = null;
                if (histories.get(0).getProxy().length() == 0 || histories.get(0).getProxy() == null) {
                    if(test==1 || test==2 || test==3 || test==4){
                        proxy=proxyRepository.getProxyVtBuffTest();
                    }else if(test==5 || test==6 || test==8 || test==0){
                        proxy=proxyRepository.getProxyBuffByUsername(histories.get(0).getUsername().trim());
                    }else if(test==7){
                        proxy=proxyRepository.getProxyHc5pBuffTest();
                    }else{
                        proxy=proxyRepository.getProxyHcPortBuffTest();
                    }
                } else {
                    if(test==1 || test==2 || test==3 || test==4){
                        proxy=proxyRepository.getProxyVtBuffTest(StringUtils.getProxyhost(histories.get(0).getProxy()));
                    }else if(test==5 || test==6 || test==8 || test==0){
                        proxy=proxyRepository.getProxyBuffByIpv4ByUsername(histories.get(0).getUsername().trim(),StringUtils.getProxyhost(histories.get(0).getProxy()));
                    }else if(test==7){
                        proxy=proxyRepository.getProxyHc5pBuffTest(StringUtils.getProxyhost(histories.get(0).getProxy()));
                    }else{
                        proxy=proxyRepository.getProxyHcPortBuffTest(StringUtils.getProxyhost(histories.get(0).getProxy()));
                    }
                }
                if (proxy.size()==0) {
                    //histories.get(0).setProxy("");
                    //historyRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("message", "Không còn proxy để sử dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                Thread.sleep(100);
                if(proxy.get(0).getRunning()==1){
                    resp.put("status", "fail");
                    resp.put("message", "Proxy đã được luồng khác sử dụng!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                histories.get(0).setProxy(proxy.get(0).getProxy());
                historyRepository.save(histories.get(0));
                proxy.get(0).setTimeget(System.currentTimeMillis());
                proxy.get(0).setVps(histories.get(0).getVps());
                proxy.get(0).setRunning(1);
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
                Long  historieId=historyRepository.getId(username.trim());
                historyRepository.resetThreadByUsername(historieId);
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
            historyRepository.resetThreadByVps("%"+vps.trim()+"%");
            resp.put("status", "true");
            resp.put("message", "Update thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "/resetaccountbyvpsthan",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> resetaccountbyvpsthan(@RequestParam(defaultValue = "")  String vps,@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        try {
            List<String> count_vps= accountRepository.getCountByVps();
            for(int i=0;i<count_vps.size();i++){
                System.out.println(count_vps.get(i));
                accountRepository.updatelistaccount(count_vps.get(i).split(",")[0]+"%",Integer.parseInt(count_vps.get(i).split(",")[1])- 2*(vpsRepository.getThreadVPS(count_vps.get(i).split(",")[0]+"%")));
                historyRepository.updateHistoryByAccount();
            }
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
