package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historytraffic")
public class HistoryTrafficController {
    @Autowired
    private WebTrafficRepository webTrafficRepository;
    @Autowired
    private HistoryTrafficRepository historyTrafficRepository;
    @Autowired
    private HistoryTrafficSumRepository historyTrafficSumRepository;
    @Autowired
    private OrderTrafficTrue orderTrue;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(3000));
            Long historieId = historyTrafficRepository.getId(username);
            List<WebTraffic> webTraffics = null;
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Username không tồn tại!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            } else {
                List<HistoryTraffic> histories = historyTrafficRepository.getHistoriesById(historieId);
                webTraffics = webTrafficRepository.getWebTrafficByGeo(histories.get(0).getGeo().trim(), histories.get(0).getListorderid(), orderTrue.getValue());
                if (webTraffics.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setOrderid(webTraffics.get(0).getOrderid());
                    histories.get(0).setRunning(1);
                    historyTrafficRepository.save(histories.get(0));
                } else {
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        historyTrafficRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username", histories.get(0).getUsername());
                        resp.put("fail", "traffic");
                        resp.put("message", "Không còn nhiêm vụ traffic!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                String[] proxy = accountRepository.getProxyByUsername(username.trim()).split(":");
                resp.put("status", "true");
                resp.put("click_ads", "true");
                resp.put("proxy",proxy[0] + ":" + proxy[1] + ":1:1");
                resp.put("orderid", webTraffics.get(0).getOrderid());
                resp.put("device", histories.get(0).getDevice());
                resp.put("link", webTraffics.get(0).getLink());
                resp.put("username", histories.get(0).getUsername());

                String[] keyArr = webTraffics.get(0).getKeywords().split(",");
                String key = keyArr[ran.nextInt(keyArr.length)];
                resp.put("keyword", key);

                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updateOrderid", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateOrderid(@RequestParam(defaultValue = "") String username,
                                         @RequestParam(defaultValue = "0") Long orderid) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (orderid == 0) {
            resp.put("status", "fail");
            resp.put("message", "orderid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyTrafficRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                if (historyTrafficRepository.getListOrderIdById(historieId).length() > 44) {
                    historyTrafficRepository.updateListOrderidNew(orderid.toString(), historieId);
                } else {
                    historyTrafficRepository.updateListOrderid(orderid.toString(), historieId);
                }
                resp.put("status", "true");
                resp.put("message", "Update orderid vào history thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping(value = "/updateDoneTraffic", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateDoneTask(@RequestBody String keyword,@RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "0") Long orderid,@RequestParam(defaultValue = "0") Integer duration) {
        JSONObject resp = new JSONObject();

        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (orderid == 0) {
            resp.put("status", "fail");
            resp.put("message", "Orderid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

        try {

            Long historieId = historyTrafficRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                    List<HistoryTraffic> histories = historyTrafficRepository.getHistoriesById(historieId);
                    HistoryTraficSum historySum = new HistoryTraficSum();
                    historySum.setOrderid(orderid);
                    historySum.setUsername(username);
                    historySum.setTime(System.currentTimeMillis());
                    historySum.setKeyword(keyword!=null?keyword:"");
                    historySum.setDuration(duration);
                    historySum.setDevice(histories.get(0).getDevice());
                    try {
                        historyTrafficSumRepository.save(historySum);
                    } catch (Exception e) {
                        try {
                            historyTrafficSumRepository.save(historySum);
                        } catch (Exception f) {
                        }
                    }
                    resp.put("status", "true");
                    resp.put("message", "Update traffic thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            }catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "delthreadbyusername", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestParam(defaultValue = "") String username) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyTrafficRepository.getId(username.trim());
            historyTrafficRepository.resetThreadById(historieId);
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadcron() {
        JSONObject resp = new JSONObject();
        try {
            historyTrafficRepository.resetThreadThan30mcron();
            historyTrafficRepository.resetThreadcron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delnamebyvps", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delnamebyvps(@RequestParam(defaultValue = "") String vps) throws InterruptedException {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (historyTrafficRepository.PROCESSLISTVIEW() >= 30) {
            Random ran = new Random();
            Thread.sleep(1000 + ran.nextInt(2000));
            resp.put("status", "fail");
            resp.put("message", "Đợi reset threads...");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        try {
            historyTrafficRepository.resetThreadByVps(vps.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

}
