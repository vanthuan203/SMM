package com.nts.awspremium.controller;

import com.nts.awspremium.ProxyAPI;
import com.nts.awspremium.TikTokApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api")
public class ApiTikTokController {
    @Autowired
    private ChannelTikTokRepository channelTikTokRepository;
    @Autowired
    private ChannelTikTokHistoryRepository channelTikTokHistoryRepository;
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private ProxyRepository proxyRepository;
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


    @PostMapping(value = "/tiktok", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> tiktok(DataRequest data) throws IOException, ParseException {
        JSONObject resp = new JSONObject();
        try {
            List<Admin> admins = adminRepository.FindByToken(data.getKey().trim());
            if (data.getKey().length() == 0 || admins.size() == 0) {
                resp.put("error", "Key not found");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            //Danh sách dịch vụ view cmc
            if (data.getAction().equals("services")) {
                List<Service> services = serviceRepository.getAllServiceTiktok();
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
                    ChannelTiktok tiktok = channelTikTokRepository.getChannelTiktokById(data.getOrder());
                    ChannelTikTokHistory tikTokHistory = channelTikTokHistoryRepository.getChannelTikTokHistoriesById(data.getOrder());
                    if (tiktok != null) {
                        resp.put("start_count",0);
                        resp.put("current_count", tiktok.getFollower_total());
                        resp.put("charge", tiktok.getPrice());
                        if (tiktok.getMax_threads() <= 0) {
                            resp.put("status", "Pending");
                        } else {
                            resp.put("status", "In progress");
                        }
                        resp.put("remains", tiktok.getFollower_order() - tiktok.getFollower_total());
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    } else {
                        if (tikTokHistory == null) {
                            resp.put("error", "Incorrect order ID");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        } else {
                            resp.put("start_count", 0);
                            resp.put("current_count",tikTokHistory.getFollower_total());
                            resp.put("charge", tikTokHistory.getPrice());
                            if (tikTokHistory.getCancel() == 1) {
                                resp.put("status", "Canceled");
                            } else if (tikTokHistory.getCancel() == 2) {
                                resp.put("status", "Partial");
                            } else {
                                resp.put("status", "Completed");
                            }
                            resp.put("remains", tikTokHistory.getFollower_order() - tikTokHistory.getFollower_total());
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                    }

                } else {
                    List<String> ordersArrInput = new ArrayList<>();
                    ordersArrInput.addAll(Arrays.asList(data.getOrders().split(",")));
                    List<ChannelTiktok> channelTiktokList= channelTikTokRepository.getChannelTiktokByListId(ordersArrInput);
                    JSONObject tiktokObject = new JSONObject();
                    for (ChannelTiktok v : channelTiktokList) {
                        JSONObject tiktok = new JSONObject();
                        tiktok.put("start_count", 0);
                        tiktok.put("current_count", v.getFollower_total());
                        tiktok.put("charge", v.getPrice());
                        if (v.getMax_threads() <=0) {
                            tiktok.put("status", "Pending");
                        } else {
                            tiktok.put("status", "In progress");
                        }
                        //videoview.put("status", "In progress");
                        tiktok.put("remains", v.getFollower_order() - v.getFollower_total());
                        tiktokObject.put("" + v.getOrderid(), tiktok);
                        ordersArrInput.remove("" + v.getOrderid());
                    }
                    String listIdHis = String.join(",", ordersArrInput);
                    List<ChannelTikTokHistory> channelTikTokHistoryList = channelTikTokHistoryRepository.getChannelTikTokHistoriesListById(ordersArrInput);
                    for (ChannelTikTokHistory vh : channelTikTokHistoryList) {
                        JSONObject tiktok_list = new JSONObject();
                        if (channelTikTokHistoryList != null) {
                            tiktok_list.put("start_count", 0);
                            tiktok_list.put("current_count", vh.getFollower_total() );
                            tiktok_list.put("charge", vh.getPrice());
                            if (vh.getCancel() == 1) {
                                tiktok_list.put("status", "Canceled");
                            } else if (vh.getCancel() == 2) {
                                tiktok_list.put("status", "Partial");
                            } else {
                                tiktok_list.put("status", "Completed");
                            }
                            tiktok_list.put("remains", vh.getFollower_order() - vh.getFollower_total());
                            tiktokObject.put("" + vh.getOrderid(), tiktok_list);
                            ordersArrInput.remove("" + vh.getOrderid());
                        }
                    }
                    for (String orderId : ordersArrInput) {
                        JSONObject orderIdError = new JSONObject();
                        orderIdError.put("error", "Incorrect order ID");
                        tiktokObject.put(orderId, orderIdError);
                    }
                    return new ResponseEntity<String>(tiktokObject.toJSONString(), HttpStatus.OK);
                }
            }
            if (data.getAction().equals("add")) {

                Service service = serviceRepository.getService(data.getService());
                if (service == null) {
                    resp.put("error", "Invalid service");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if(service.getMaxorder() <= channelTikTokRepository.getCountOrderByService(data.getService())){
                    resp.put("error", "System busy try again");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (data.getLink().trim().length() == 0) {
                    resp.put("error", "Link is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                Setting setting = settingRepository.getReferenceById(1L);
                if (service.getType().equals("Special") && data.getList().length() == 0) {
                    resp.put("error", "Keyword is null");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (data.getQuantity() > service.getMax() || data.getQuantity() < service.getMin()) {
                    resp.put("error", "Min/Max order is: " + service.getMin() + "/" + service.getMax());
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                String tiktok_id= TikTokApi.getTiktokId(data.getLink().trim());
                if (tiktok_id == null) {
                    resp.put("error", "Cant filter tiktok_id from link");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (channelTikTokRepository.getCountTiktokId(tiktok_id.trim()) > 0) {
                    resp.put("error", "This tiktok_id in process");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                String proxycheck=proxyRepository.getProxyRandTrafficForCheckAPI();
                Integer follower_count=TikTokApi.getFollowerCount("https://www.tiktok.com/"+tiktok_id.trim(),proxycheck);
                float priceorder = 0;
                priceorder = (data.getQuantity() / 1000F) * service.getRate() * ((float) (admins.get(0).getRate()) / 100) * ((float) (100 - admins.get(0).getDiscount()) / 100);
                if (priceorder > (float) admins.get(0).getBalance()) {
                    resp.put("error", "Your balance not enough");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

                ChannelTiktok channelTiktok = new ChannelTiktok();
                channelTiktok.setInsert_date(System.currentTimeMillis());
                channelTiktok.setFollower_order(data.getQuantity());
                channelTiktok.setFollower_start(follower_count!=null?follower_count:0);
                channelTiktok.setFollower_total(0);
                channelTiktok.setTiktok_id(tiktok_id.trim());
                channelTiktok.setUser(admins.get(0).getUsername());
                channelTiktok.setTime_update(0L);
                channelTiktok.setTime_start(System.currentTimeMillis());
                channelTiktok.setMax_threads(5);
                channelTiktok.setNote("");
                channelTiktok.setPrice(priceorder);
                channelTiktok.setService(data.getService());
                channelTiktok.setValid(1);
                channelTikTokRepository.save(channelTiktok);

                Float balance_update=adminRepository.updateBalanceFine(-priceorder,admins.get(0).getUsername().trim());
                Balance balance = new Balance();
                balance.setUser(admins.get(0).getUsername().trim());
                balance.setTime(System.currentTimeMillis());
                balance.setTotalblance(balance_update);
                balance.setBalance(-priceorder);
                balance.setService(data.getService());
                balance.setNote("Order " + data.getQuantity() + " follower cho tiktok_id " + tiktok_id.trim());
                balanceRepository.save(balance);
                resp.put("order", channelTiktok.getOrderid());
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("error", "api system error");
            resp.put("error",e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        resp.put("error", "api system error");
        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
    }
}
