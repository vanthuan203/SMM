package com.nts.awspremium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.Proxy;
import com.nts.awspremium.model.ResponseObject;
import com.nts.awspremium.repositories.AccountRepository;
import com.nts.awspremium.repositories.AdminRepository;
import com.nts.awspremium.repositories.ProxyRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path ="/gmails")

public class AccountController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProxyRepository proxyRepository;

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
            List<Account> foundAccounts=accountRepository.findAccountByUsername(newaccount.getUsername().trim());
            String datestring=newaccount.getEndtrialstring();
            String MMM = datestring.substring(0,3);
            String date=datestring.replace(MMM,StringUtils.convertMMMtoMM(datestring)+"");
            Long endtrial= StringUtils.getLongTimeFromString(date,"MM dd, yyyy");

            if(foundAccounts.size()>0){
                if(update==1) {
                    foundAccounts.get(0).setPassword(newaccount.getPassword());
                    foundAccounts.get(0).setRecover(newaccount.getRecover());
                    foundAccounts.get(0).setLive(newaccount.getLive());
                    foundAccounts.get(0).setEncodefinger(newaccount.getEncodefinger());
                    foundAccounts.get(0).setCookie(newaccount.getCookie());
                    foundAccounts.get(0).setEndtrial(endtrial);
                    foundAccounts.get(0).setEndtrialstring(newaccount.getEndtrialstring());
                    foundAccounts.get(0).setVps(newaccount.getVps());
                    accountRepository.save(foundAccounts.get(0));
                    resp.put("status","true");
                    resp.put("message", "Update "+foundAccounts.get(0).getUsername()+" thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    resp.put("status","fail");
                    resp.put("message", "Account "+foundAccounts.get(0).getUsername()+" đã tồn tại");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }

            }else{
                newaccount.setRunning(0);
                newaccount.setProxy("");
                newaccount.setEndtrial(endtrial);
                accountRepository.save(newaccount);
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
            List<Account> accountbyVps=accountRepository.getaccountByVps(vps);
            if(accountbyVps.size()==0){
                List<Account> accounts=accountRepository.getAccount();
                if(accounts.size()==0){
                    resp.put("status","fail");
                    resp.put("message", "Hết tài khoản thỏa mãn!");
                    return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                }else{
                    try{
                        List<Proxy> proxy;
                        if(accounts.get(0).getProxy().length()==0 || accounts.get(0).getProxy()==null){
                            proxy=proxyRepository.getProxyTimeGetNull();
                            if(proxy.size()==0){
                                proxy=proxyRepository.getProxy();
                            }
                        }else{
                            proxy=proxyRepository.getProxyTimeGetNull(StringUtils.getProxyhost(accounts.get(0).getProxy()));
                            if(proxy.size()==0){
                                proxy=proxyRepository.getProxy(StringUtils.getProxyhost(accounts.get(0).getProxy()));
                            }
                        }
                        accounts.get(0).setProxy(proxy.get(0).getProxy());
                        accounts.get(0).setVps(vps);
                        accounts.get(0).setRunning(1);
                        accountRepository.save(accounts.get(0));
                        proxy.get(0).setTimeget(System.currentTimeMillis());
                        proxyRepository.save(proxy.get(0));

                        resp.put("status","true");
                        resp.put("username",accounts.get(0).getUsername());
                        resp.put("password",accounts.get(0).getPassword());
                        resp.put("recover",accounts.get(0).getRecover());
                        resp.put("cookie",accounts.get(0).getCookie());
                        resp.put("proxy",accounts.get(0).getProxy());
                        return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
                    }catch(Exception e){
                        resp.put("status", "fail");
                        resp.put("message", e.getMessage());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
                    }
                }
            }else{
                try{
                    List<Proxy> proxy;
                    if(accountbyVps.get(0).getProxy().length()==0 || accountbyVps.get(0).getProxy()==null){
                        proxy=proxyRepository.getProxyTimeGetNull();
                        if(proxy.size()==0){
                            proxy=proxyRepository.getProxy();
                        }
                    }else{
                        proxy=proxyRepository.getProxyTimeGetNull(StringUtils.getProxyhost(accountbyVps.get(0).getProxy()));
                        if(proxy.size()==0){
                            proxy=proxyRepository.getProxy(StringUtils.getProxyhost(accountbyVps.get(0).getProxy()));
                        }
                    }
                    accountbyVps.get(0).setProxy(proxy.get(0).getProxy());
                    accountbyVps.get(0).setVps(vps);
                    accountbyVps.get(0).setRunning(1);
                    accountRepository.save(accountbyVps.get(0));
                    proxy.get(0).setTimeget(System.currentTimeMillis());
                    proxyRepository.save(proxy.get(0));

                    resp.put("status","true");
                    resp.put("username",accountbyVps.get(0).getUsername());
                    resp.put("password",accountbyVps.get(0).getPassword());
                    resp.put("recover",accountbyVps.get(0).getRecover());
                    resp.put("cookie",accountbyVps.get(0).getCookie());
                    resp.put("proxy",accountbyVps.get(0).getProxy());
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
    @GetMapping(value = "/checkendtrial",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkendtrial(@RequestParam(defaultValue = "")  String username,@RequestHeader(defaultValue = "") String Authorization){
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
            List<Account> accountcheck=accountRepository.checkEndTrial(username);
            if(accountcheck.size()==0){
                resp.put("status","fail");
                resp.put("message", "Username : "+username+" hết hạn premium!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status","true");
                resp.put("message", "Username : "+username+" còn hạn premium!");
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
            List<Account> accounts=accountRepository.findAccountByUsername(username);
            if(accounts.size()==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                accounts.get(0).setCookie(account.getCookie());
                accountRepository.save(accounts.get(0));
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
            List<Account> accounts=accountRepository.findAccountByUsername(username);
            if(accounts.size()==0){
                resp.put("status","fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
            }else{
                List<Proxy> proxy;
                if(accounts.get(0).getProxy().length()==0 || accounts.get(0).getProxy()==null){
                    proxy=proxyRepository.getProxyTimeGetNull();
                    if(proxy.size()==0){
                        proxy=proxyRepository.getProxy();
                    }
                }else{
                    proxy=proxyRepository.getProxyTimeGetNull(StringUtils.getProxyhost(accounts.get(0).getProxy()));
                    if(proxy.size()==0){
                        proxy=proxyRepository.getProxy(StringUtils.getProxyhost(accounts.get(0).getProxy()));
                    }
                }
                accounts.get(0).setProxy(proxy.get(0).getProxy());
                accountRepository.save(accounts.get(0));
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
}
