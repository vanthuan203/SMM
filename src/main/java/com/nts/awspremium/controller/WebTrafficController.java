package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/webtraffic")
public class WebTrafficController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private WebTrafficHistoryRepository webTrafficHistoryRepository;
    @Autowired
    private WebTrafficRepository webTrafficRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataOrderRepository dataOrderRepository;

    @Autowired
    private AutoRefillRepository autoRefillRepository;

    @Autowired
    private GoogleAPIKeyRepository googleAPIKeyRepository;

    @Autowired
    private LimitServiceRepository limitServiceRepository;

    @PostMapping(value = "/ordertraffic", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> ordertraffic(@RequestBody WebTraffic webTraffic, @RequestHeader(defaultValue = "") String Authorization) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        //System.out.println(videoView.getService());
        try {
            List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
            if (Authorization.length() == 0 || admins.size() == 0) {
                resp.put("status", "fail");
                resp.put("message", "Token expired");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
            }
            Service service = serviceRepository.getServiceNoCheckEnabled(webTraffic.getService());
            float priceorder = 0;
            int time = 0;
            priceorder = (webTraffic.getTrafficorder() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
            if (priceorder > (float) admins.get(0).getBalance()) {
                resp.put("webtraffic", "Your balance not enough");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            WebTraffic webTrafficNew = new WebTraffic();
            webTrafficNew.setInsertdate(System.currentTimeMillis());
            webTrafficNew.setTraffic24h(webTraffic.getTrafficorder());
            webTrafficNew.setTrafficorder(webTraffic.getTrafficorder());
            webTrafficNew.setTraffictotal(0);
            webTrafficNew.setLink(webTraffic.getLink());
            webTrafficNew.setUser(admins.get(0).getUsername());
            webTrafficNew.setKeywords(webTraffic.getKeywords());
            webTrafficNew.setTimeupdate(0L);
            webTrafficNew.setEnddate(0L);
            webTrafficNew.setTimestart(System.currentTimeMillis());
            webTrafficNew.setMaxthreads(service.getThread());
            webTrafficNew.setNote(webTraffic.getNote());
            webTrafficNew.setPrice(priceorder);
            webTrafficNew.setService(webTraffic.getService());
            webTrafficRepository.save(webTrafficNew);

            Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(admins.get(0).getUsername().trim());
            balance.setTime(System.currentTimeMillis());
            balance.setTotalblance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(webTraffic.getService());
            balance.setNote("Order " + webTraffic.getTrafficorder() + " traffic cho orderid " + webTrafficNew.getOrderid());
            balanceRepository.save(balance);

            resp.put("webtraffic", "true");
            resp.put("balance", admins.get(0).getBalance());
            resp.put("price", priceorder);
            resp.put("time", time);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            //new Video(video.get("videoId").toString(), "channel_id", Duration.parse(video.get("duration").toString()).getSeconds(), video.get("title").toString());
        } catch (Exception e) {
            resp.put("webtraffic", "Error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
    }
    @GetMapping(path = "getordertraffic", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getordertraffic(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<OrderTrafficRunning> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = webTrafficRepository.getOrder();

            } else {
                orderRunnings = webTrafficRepository.getOrder(user.trim());
            }

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderId());
                obj.put("link", orderRunnings.get(i).getLink());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertDate());
                obj.put("timestart", orderRunnings.get(i).getTimeStart());
                obj.put("total", orderRunnings.get(i).getTotal());
                obj.put("trafficorder", orderRunnings.get(i).getTrafficOrder());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("service", orderRunnings.get(i).getService());
                obj.put("user", orderRunnings.get(i).getUser());

                obj.put("traffic24h", orderRunnings.get(i).getTraffic24h());
                obj.put("traffictotal", orderRunnings.get(i).getTrafficTotal());
                obj.put("price", orderRunnings.get(i).getPrice());
                jsonArray.add(obj);
            }

            resp.put("total", orderRunnings.size());
            resp.put("webtraffic", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "delete", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delete(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String orderid, @RequestParam(defaultValue = "1") Integer cancel) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (orderid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "orderid không được để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            String[] orderidArr = orderid.split(",");
            for (int i = 0; i < orderidArr.length; i++) {

                Long enddate = System.currentTimeMillis();
                List<WebTraffic> videoBuffh = webTrafficRepository.getWebTrafficByOrderId(Long.parseLong(orderidArr[i].trim()));
                WebTrafficHistory videoBuffhnew = new WebTrafficHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(0).getOrderid());
                videoBuffhnew.setInsertdate(videoBuffh.get(0).getInsertdate());
                videoBuffhnew.setService(videoBuffh.get(0).getService());
                videoBuffhnew.setKeywords(videoBuffh.get(0).getKeywords());
                videoBuffhnew.setLink(videoBuffh.get(0).getLink());
                videoBuffhnew.setMaxthreads(videoBuffh.get(0).getMaxthreads());
                videoBuffhnew.setTrafficorder(videoBuffh.get(0).getTrafficorder());
                videoBuffhnew.setMaxthreads(videoBuffh.get(0).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(0).getNote());
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(0).getTimestart());
                //videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                if (cancel == 1) {
                    Service service = serviceRepository.getInfoService(videoBuffh.get(0).getService());
                    List<Admin> user = adminRepository.getAdminByUser(videoBuffh.get(0).getUser());
                    //Hoàn tiền những view chưa buff
                    int viewbuff = videoBuffh.get(0).getTraffictotal();
                    int viewthan = videoBuffh.get(0).getTrafficorder() - (videoBuffh.get(0).getTraffictotal() > videoBuffh.get(0).getTrafficorder() ? videoBuffh.get(0).getTrafficorder() : videoBuffh.get(0).getTraffictotal());
                    //System.out.println(videoBuffh.get(0).getViewtotal() > videoBuffh.get(0).getVieworder() ? videoBuffh.get(0).getVieworder() : videoBuffh.get(0).getViewtotal());
                    float price_refund = (viewthan / (float) videoBuffh.get(0).getTrafficorder()) * videoBuffh.get(0).getPrice();
                    //float pricebuffed=(videoBuffh.get(0).getViewtotal()/1000F)*service.getRate()*((float)(100-admins.get(0).getDiscount())/100);
                    float pricebuffed = (videoBuffh.get(0).getPrice() - price_refund);
                    videoBuffhnew.setPrice(pricebuffed);
                    if (viewbuff == 0) {
                        videoBuffhnew.setCancel(1);
                    } else if (viewbuff >= videoBuffh.get(0).getTrafficorder()) {
                        videoBuffhnew.setCancel(0);
                    } else {
                        videoBuffhnew.setCancel(2);
                    }
                    //hoàn tiền & add thong báo số dư
                    if (viewthan > 0) {
                        Float balance_update=adminRepository.updateBalanceFine(price_refund,videoBuffh.get(0).getUser().trim());
                        Balance balance = new Balance();
                        balance.setUser(user.get(0).getUsername().trim());
                        balance.setTime(System.currentTimeMillis());
                        balance.setTotalblance(balance_update);
                        balance.setBalance(price_refund);
                        balance.setService(videoBuffh.get(0).getService());
                        balance.setNote("Refund " + (viewthan) + " traffic cho orderid " + videoBuffh.get(0).getOrderid());
                        balanceRepository.save(balance);
                    }
                } else {
                    videoBuffhnew.setPrice(videoBuffh.get(0).getPrice());
                    videoBuffhnew.setCancel(0);
                }
                videoBuffhnew.setUser(videoBuffh.get(0).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setTraffictotal(videoBuffh.get(0).getTraffictotal());
                webTrafficHistoryRepository.save(videoBuffhnew);
                webTrafficRepository.deletevideoByOrderId(Long.parseLong(orderidArr[i].trim()));
            }
            resp.put("webtraffic", "");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "updateTrafficDoneCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updateTrafficDoneCron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            //historyRepository.updateHistoryByAccount();
            List<WebTraffic> videoBuffh = webTrafficRepository.getOrderFullTraffic();
            for (int i = 0; i < videoBuffh.size(); i++) {
                Long enddate = System.currentTimeMillis();

                WebTrafficHistory videoBuffhnew = new WebTrafficHistory();
                videoBuffhnew.setOrderid(videoBuffh.get(i).getOrderid());
                videoBuffhnew.setInsertdate(videoBuffh.get(i).getInsertdate());
                videoBuffhnew.setKeywords(videoBuffh.get(i).getKeywords());
                videoBuffhnew.setLink(videoBuffh.get(i).getLink());
                videoBuffhnew.setMaxthreads(videoBuffh.get(i).getMaxthreads());
                videoBuffhnew.setNote(videoBuffh.get(i).getNote());
                videoBuffhnew.setCancel(0);
                videoBuffhnew.setNumbh(0);
                videoBuffhnew.setTimecheck(0L);
                videoBuffhnew.setTimestart(videoBuffh.get(i).getTimestart());
                videoBuffhnew.setUser(videoBuffh.get(i).getUser());
                videoBuffhnew.setEnddate(enddate);
                videoBuffhnew.setService(videoBuffh.get(i).getService());
                videoBuffhnew.setTraffictotal(videoBuffh.get(i).getTraffictotal());
                videoBuffhnew.setTrafficorder(videoBuffh.get(i).getTrafficorder());
                videoBuffhnew.setPrice(videoBuffh.get(i).getPrice());
                try {
                    webTrafficHistoryRepository.save(videoBuffhnew);
                    webTrafficRepository.deletevideoByOrderId(videoBuffh.get(i).getOrderid());
                } catch (Exception e) {

                }
            }
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "updatTrafficedCron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatTrafficDoneCron() {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        try {
            Setting setting = settingRepository.getReferenceById(1L);
            List<String> trafficBuff;
            List<String> traffic24h;
            List<WebTraffic> webTraffics = webTrafficRepository.getAllOrderTraffic();
            trafficBuff = webTrafficRepository.getTotalTrafficBuff();

            for (int i = 0; i < webTraffics.size(); i++) {
                int traffictotal = 0;
                int traffictotal24h = 0;
                for (int j = 0; j < trafficBuff.size(); j++) {
                    if (webTraffics.get(i).getOrderid()==Integer.parseInt(trafficBuff.get(j).split(",")[0])) {
                        traffictotal = Integer.parseInt(trafficBuff.get(j).split(",")[1]);
                    }
                }
                try {
                    webTrafficRepository.updateTrafficOrderByOrderId(traffictotal, System.currentTimeMillis(), webTraffics.get(i).getOrderid());
                } catch (Exception e) {

                }
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", webTraffics.size());
            resp.put("webtraffic", true);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "getorderviewhhistory", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> getorderviewhhistory(@RequestHeader(defaultValue = "") String Authorization, @RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<WebTrafficHistory> orderRunnings;
            if (user.length() == 0) {
                orderRunnings = webTrafficHistoryRepository.getWebTrafficHistories();
            } else {
                orderRunnings = webTrafficHistoryRepository.getWebTrafficHistories(user.trim());
            }
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < orderRunnings.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("orderid", orderRunnings.get(i).getOrderid());
                obj.put("link", orderRunnings.get(i).getLink());
                obj.put("keywords", orderRunnings.get(i).getKeywords());
                obj.put("maxthreads", orderRunnings.get(i).getMaxthreads());
                obj.put("insertdate", orderRunnings.get(i).getInsertdate());
                obj.put("user", orderRunnings.get(i).getUser());
                obj.put("note", orderRunnings.get(i).getNote());
                obj.put("enddate", orderRunnings.get(i).getEnddate());
                obj.put("cancel", orderRunnings.get(i).getCancel());
                obj.put("timestart", orderRunnings.get(i).getTimestart());
                obj.put("timecheckbh", orderRunnings.get(i).getTimecheckbh());
                obj.put("traffictotal", orderRunnings.get(i).getTraffictotal());
                obj.put("trafficorder", orderRunnings.get(i).getTrafficorder());
                obj.put("price", orderRunnings.get(i).getPrice());
                obj.put("service", orderRunnings.get(i).getService());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");

            resp.put("total", orderRunnings.size());
            resp.put("webtraffic", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}
