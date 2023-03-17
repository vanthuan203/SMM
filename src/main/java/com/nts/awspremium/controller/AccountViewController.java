package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.History;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path ="/accview")

public class AccountViewController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;

    @Autowired
    private CookieRepository cookieRepository;

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
            Long idUsername= accountRepository.findIdUsername(newaccount.getUsername().trim());
            if(idUsername!=null){
                if(update==1) {
                    accountRepository.updateAccountView(newaccount.getPassword(),newaccount.getRecover(),newaccount.getLive(),"","",idUsername);
                    //cookieRepository.updateCookieBuffh(newaccount.getCookie(),newaccount.getUsername());
                    resp.put("status","true");
                    resp.put("message", "Update "+newaccount.getUsername()+" thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    resp.put("message", "Account "+newaccount.getUsername()+" đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }
            }else{
                accountRepository.insertAccountView(newaccount.getUsername(), newaccount.getPassword(), newaccount.getRecover(),newaccount.getLive(),"","",0,"",newaccount.getDate(),newaccount.getGeo());
                //cookieRepository.insertCookieView(newaccount.getUsername(), newaccount.getCookie());
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
    ResponseEntity<String> getAccount(@RequestParam(defaultValue = "")  String vps,@RequestParam(defaultValue = "vn") String geo){
        JSONObject resp = new JSONObject();
        Random ran=new Random();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Tên vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if(historyViewRepository.PROCESSLISTVIEW()>=30){
            resp.put("status", "fail");
            resp.put("message", "Get account không thành công, thử lại sau ítp phút!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        try {
            Integer check_get=0;
            check_get= vpsRepository.checkGetAccount3ByThreadVps(vps.trim()+"%");
            if(check_get==0){
                resp.put("status","fail");
                resp.put("message", "Đã đủ acc cho Vps!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }
            Thread.sleep(ran.nextInt(500));
            Long idbyVps=accountRepository.getaccountByVps(vps.trim());
            if(idbyVps==null){
                Thread.sleep(ran.nextInt(500));
                Long id=accountRepository.getAccountView(geo.trim());
                if(id==null){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        List<Account> account=accountRepository.findAccountById(id);
                        //Long idCookieSub= cookieRepository.findIdSubByUsername(account.get(0).getUsername().trim());
                        //String cookieSub= cookieRepository.findCookieSubById(idCookieSub);
                        Thread.sleep(100+ran.nextInt(200));
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
                        resp.put("password",account.get(0).getPassword());
                        resp.put("recover",account.get(0).getRecover());
                        resp.put("cookie","");
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
                    accountbyVps.get(0).setVps(vps.trim());
                    accountbyVps.get(0).setRunning(1);
                    accountbyVps.get(0).setTimecheck(System.currentTimeMillis());
                    accountRepository.save(accountbyVps.get(0));

                    resp.put("status","true");
                    resp.put("username",accountbyVps.get(0).getUsername());
                    resp.put("endtrial",accountbyVps.get(0).getEndtrial());
                    resp.put("recover",accountbyVps.get(0).getRecover());
                    resp.put("cookie","");
                    resp.put("password",accountbyVps.get(0).getPassword());
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
    ResponseEntity<String> getlogin(){
        JSONObject resp = new JSONObject();
        try {
                Long id=accountRepository.getAccountNeedLogin();
                if(id==null){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        List<Account> account=accountRepository.findAccountById(id);
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
                        //resp.put("encodefinger",encodefingerRepository.findEncodefingerSub(account.get(0).getUsername().trim()));
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

            Integer allgmail = accountRepository.getCountGmailLiveView();
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
            Long idUsername=accountRepository.findIdUsername(username);
            if(idUsername==null){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{

                cookieRepository.updateCookieSub(account.getCookie(),idUsername);
                accountRepository.updateAccSubWhileCookieUpdate(idUsername);
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
            Long idUsername=accountRepository.findIdUsername(username);
            if(idUsername==null){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                cookieRepository.updateCookieSub(account.getCookie(),idUsername);
                accountRepository.updatecookieloginlocal(idUsername);
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
    ResponseEntity<String> update(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "")  String password,@RequestParam(defaultValue = "")  String recover ){
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
            }
            else{
                List<Account> accounts=accountRepository.findAccountById(idUsername);
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

    @GetMapping(value = "/reset",produces = "application/hal_json;charset=utf8")
    ResponseEntity<String> reset(@RequestParam(defaultValue = "")  String username,@RequestParam(defaultValue = "0")  Integer live) {
        JSONObject resp = new JSONObject();
        try {
            if (username.length() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Username không được để trống!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Long idUsername=accountRepository.findIdUsername(username);
            if(idUsername==null) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                List<Account> accounts=accountRepository.findAccountById(idUsername);
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
            accountRepository.resetAccountByVps(vps.trim());
            historyRepository.resetThreadByVps(vps.trim());
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
                accountRepository.updatelistaccount(count_vps.get(i).split(",")[0],Integer.parseInt(count_vps.get(i).split(",")[1])- 2*(vpsRepository.getThreadVPS(count_vps.get(i).split(",")[0])));
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
