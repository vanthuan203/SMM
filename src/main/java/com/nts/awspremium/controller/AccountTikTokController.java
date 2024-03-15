package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
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
@RequestMapping(path = "/acc_tiktok")

public class AccountTikTokController {
    @Autowired
    private AccountTikTokRepository accountRepository;
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
                    accountTiktok.setNick_name(account.getNick_name().trim());
                    accountTiktok.setRunning(account.getRunning());
                    accountTiktok.setTime_check(System.currentTimeMillis());
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
                accountTiktokNew.setTime_check(System.currentTimeMillis());
                accountTiktokNew.setProxy("");
                accountTiktokNew.setRunning(account.getRunning());
                accountTiktokNew.setGeo("vn");
                accountTiktokNew.setVps(account.getVps().trim());
                accountRepository.save(accountTiktokNew);
                //add history tiktok
                if(historyTiktokRepository.checkUsername(account.getUsername().trim())==0){
                    HistoryTikTok historyTikTok =new HistoryTikTok();
                    historyTikTok.setUsername(account.getUsername().trim());
                    historyTikTok.setOption_running(account.getRunning());
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


}
