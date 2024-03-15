package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.ProxyAPI;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api")
public class ApiTrafficController {
    @Autowired
    private VideoViewRepository videoViewRepository;
    @Autowired
    private WebTrafficRepository webTrafficRepository;
    @Autowired
    private WebTrafficHistoryRepository webTrafficHistoryRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private VideoViewHistoryRepository videoViewHistoryRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataOrderRepository dataOrderRepository;

    @Autowired
    private LimitServiceRepository limitServiceRepository;

    @Autowired
    private VpsRepository vpsRepository;


    @PostMapping(value = "/traffic", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> view(DataRequest data) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try {
            List<Admin> admins = adminRepository.FindByToken(data.getKey().trim());
            if (data.getKey().length() == 0 || admins.size() == 0) {
                resp.put("error", "Key not found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            //Danh sách dịch vụ view cmc
            if (data.getAction().equals("services")) {
                List<Service> services = serviceRepository.getAllServiceTraffic();
                JSONArray arr = new JSONArray();
                float rate;
                for (int i = 0; i < services.size(); i++) {
                    rate = services.get(i).getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                    JSONObject serviceBuffH = new JSONObject();
                    serviceBuffH.put("service", services.get(i).getService());
                    serviceBuffH.put("name", services.get(i).getName());
                    serviceBuffH.put("type", services.get(i).getType());
                    serviceBuffH.put("category", services.get(i).getCategory());
                    serviceBuffH.put("rate", rate);
                    serviceBuffH.put("min", services.get(i).getMin());
                    serviceBuffH.put("max", services.get(i).getMax());
                    arr.add(serviceBuffH);
                }
                return new ResponseEntity<String>(arr.toJSONString(), HttpStatus.OK);
            }
            //truy vấn số dư tài khoản
            if (data.getAction().equals("balance")) {
                JSONObject serviceBuffH = new JSONObject();
                serviceBuffH.put("balance", admins.get(0).getBalance());
                serviceBuffH.put("currency", "USD");
                return new ResponseEntity<String>(serviceBuffH.toJSONString(), HttpStatus.OK);
            }
            //Get trạng thái đơns
            if (data.getAction().equals("status")) {
                if (data.getOrders().length() == 0) {
                    WebTraffic traffic = webTrafficRepository.getWebTrafficById(data.getOrder());
                    WebTrafficHistory trafficHistory = webTrafficHistoryRepository.getWebTrafficHisById(data.getOrder());
                    if (traffic != null) {
                        resp.put("start_count",0);
                        resp.put("current_count", traffic.getTraffictotal());
                        resp.put("charge", traffic.getPrice());
                        if (traffic.getMaxthreads() <= 0) {
                            resp.put("status", "Pending");
                        } else {
                            resp.put("status", "In progress");
                        }
                        resp.put("remains", traffic.getTrafficorder() - traffic.getTraffictotal());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    } else {
                        if (trafficHistory == null) {
                            resp.put("error", "Incorrect order ID");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        } else {
                            resp.put("start_count", 0);
                            resp.put("current_count",trafficHistory.getTraffictotal());
                            resp.put("charge", trafficHistory.getPrice());
                            if (trafficHistory.getCancel() == 1) {
                                resp.put("status", "Canceled");
                            } else if (trafficHistory.getCancel() == 2) {
                                resp.put("status", "Partial");
                            } else {
                                resp.put("status", "Completed");
                            }
                            resp.put("remains", trafficHistory.getTrafficorder() - trafficHistory.getTraffictotal());
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    List<String> ordersArrInput = new ArrayList<>();
                    ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                    String listId = String.join(",", ordersArrInput);
                    List<WebTraffic> webTraffics= webTrafficRepository.getWebTrafficByListId(ordersArrInput);
                    JSONObject trafficObject = new JSONObject();
                    for (WebTraffic v : webTraffics) {
                        JSONObject traffic = new JSONObject();
                        traffic.put("start_count", 0);
                        traffic.put("current_count", v.getTraffictotal());
                        traffic.put("charge", v.getPrice());
                        if (v.getMaxthreads() <=0) {
                            traffic.put("status", "Pending");
                        } else {
                            traffic.put("status", "In progress");
                        }
                        //videoview.put("status", "In progress");
                        traffic.put("remains", v.getTrafficorder() - v.getTraffictotal());
                        trafficObject.put("" + v.getOrderid(), traffic);
                        ordersArrInput.remove("" + v.getOrderid());
                    }
                    String listIdHis = String.join(",", ordersArrInput);
                    List<WebTrafficHistory> webTrafficHistories = webTrafficHistoryRepository.getWebTrafficHisByListId(ordersArrInput);
                    for (WebTrafficHistory vh : webTrafficHistories) {
                        JSONObject traffhis = new JSONObject();
                        if (webTrafficHistories != null) {
                            traffhis.put("start_count", 0);
                            traffhis.put("current_count", vh.getTraffictotal() );
                            traffhis.put("charge", vh.getPrice());
                            if (vh.getCancel() == 1) {
                                traffhis.put("status", "Canceled");
                            } else if (vh.getCancel() == 2) {
                                traffhis.put("status", "Partial");
                            } else {
                                traffhis.put("status", "Completed");
                            }
                            traffhis.put("remains", vh.getTrafficorder() - vh.getTraffictotal());
                            trafficObject.put("" + vh.getOrderid(), traffhis);
                            ordersArrInput.remove("" + vh.getOrderid());
                        }
                    }
                    for (String orderId : ordersArrInput) {
                        JSONObject orderIdError = new JSONObject();
                        orderIdError.put("error", "Incorrect order ID");
                        trafficObject.put(orderId, orderIdError);
                    }
                    return new ResponseEntity<String>(trafficObject.toJSONString(), HttpStatus.OK);
                }
            }
            if (data.getAction().equals("add")) {

                Service service = serviceRepository.getService(data.getService());
                if (service == null) {
                    resp.put("error", "Invalid service");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);

                }
                if (data.getLink().trim().length() == 0) {
                    resp.put("error", "Link is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (webTrafficRepository.getCountLink(data.getLink().trim()) > 0) {
                    resp.put("error", "This link in process");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if(!ProxyAPI.checkResponseCode(data.getLink().trim())){
                    resp.put("error", "The link is not accessible!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                Setting setting = settingRepository.getReferenceById(1L);
                if (videoViewRepository.getCountOrderByUser(admins.get(0).getUsername().trim()) >= admins.get(0).getMaxorder()) {
                    resp.put("error", "System busy try again");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (service.getType().equals("Special") && data.getList().length() == 0) {
                    resp.put("error", "Keyword is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if(data.getList().indexOf(",")>4){
                    resp.put("error", "Enter a maximum of 5 keywords");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (data.getQuantity() > service.getMax() || data.getQuantity() < service.getMin()) {
                    resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                float priceorder = 0;
                priceorder = (data.getQuantity() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                if (priceorder > (float) admins.get(0).getBalance()) {
                    resp.put("error", "Your balance not enough");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                WebTraffic webTrafficNew = new WebTraffic();
                String stringrand="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefhijkprstuvwx0123456789";
                String token="";
                Random ran=new Random();
                for(int i=0;i<30;i++){
                    Integer ranver=ran.nextInt(stringrand.length());
                    token=token+stringrand.charAt(ranver);
                }
                webTrafficNew.setToken(token);
                webTrafficNew.setInsertdate(System.currentTimeMillis());
                webTrafficNew.setTraffic24h(0);
                webTrafficNew.setMaxtraffic24h((int)(data.getQuantity()/(service.getExpired()==1?0.9:(service.getExpired()-1))));
                webTrafficNew.setTrafficorder(data.getQuantity());
                webTrafficNew.setTraffictotal(0);
                webTrafficNew.setLink(data.getLink().trim());
                webTrafficNew.setUser(admins.get(0).getUsername());
                webTrafficNew.setKeywords(data.getList());
                webTrafficNew.setTimeupdate(0L);
                webTrafficNew.setEnddate(0L);
                webTrafficNew.setTimestart(System.currentTimeMillis());
                webTrafficNew.setMaxthreads(((int)(webTrafficNew.getMaxtraffic24h()/(((service.getClick_web()/100F)*service.getSearch()+service.getSuggest()+service.getDirect()+service.getExternal())/100F))/24/(60/((int)(service.getMaxtime()*1.4))))<1?1:((int)(webTrafficNew.getMaxtraffic24h()/(((service.getClick_web()/100F)*service.getSearch()+service.getSuggest()+service.getDirect()+service.getExternal())/100F))/24/(60/((int)(service.getMaxtime()*1.4)))));
                webTrafficNew.setNote("");
                webTrafficNew.setPrice(priceorder);
                webTrafficNew.setService(data.getService());
                webTrafficNew.setValid(1);
                webTrafficNew.setLastcompleted(0L);
                webTrafficRepository.save(webTrafficNew);

                Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                Balance balance = new Balance();
                balance.setUser(admins.get(0).getUsername().trim());
                balance.setTime(System.currentTimeMillis());
                balance.setTotalblance(balance_update);
                balance.setBalance(-priceorder);
                balance.setService(data.getService());
                balance.setNote("Order " + data.getQuantity() + " traffic cho orderid " + webTrafficNew.getOrderid());
                balanceRepository.save(balance);
                resp.put("order", webTrafficNew.getOrderid());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("error", "api system error");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        resp.put("error", "api system error");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
}
