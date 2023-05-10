package com.nts.awspremium.controller;

import com.nts.awspremium.StringUtils;
import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historyview")
public class HistoryViewController {


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;

    @Autowired
    private HistoryCommentRepository historyCommentRepository;
    @Autowired
    private DataOrderRepository dataOrderRepository;
    @Autowired
    private HistoryViewSumRepository historyViewSumRepository;
    @Autowired
    private OrderTrue orderTrue;
    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps, @RequestParam(defaultValue = "0") Integer buffh) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            Long historieId = historyViewRepository.getId(username);
            List<VideoView> videos = null;
            if (historieId == null) {
                HistoryView history = new HistoryView();
                history.setId(System.currentTimeMillis());
                history.setUsername(username);
                history.setListvideo("");
                history.setProxy("");
                history.setRunning(0);
                history.setVps(vps);
                history.setVideoid("");
                history.setOrderid(0L);
                history.setChannelid("");
                //System.out.println(accountRepository.getGeoByUsername(username.trim()));
                history.setGeo(accountRepository.getGeoByUsername(username.trim()));
                history.setTimeget(System.currentTimeMillis());
                if (buffh == 1) {
                    if (history.getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoBuffHVer2VNTEST("", orderTrue.getValue());
                    } else if (history.getGeo().equals("us")) {
                        videos = videoViewRepository.getvideoBuffHVer2USTEST("", orderTrue.getValue());
                    }
                }else if(buffh == 2){
                    if (history.getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoLiveVer2VNTEST("", orderTrue.getValue());
                    } else {
                        videos = videoViewRepository.getvideoLiveVer2USTEST("", orderTrue.getValue());
                    }
                } else {
                    if (history.getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoViewVer2VNTEST("", orderTrue.getValue());
                    } else {
                        videos = videoViewRepository.getvideoViewVer2USTEST("", orderTrue.getValue());
                    }
                }
                if (videos.size() > 0) {
                    history.setVideoid(videos.get(0).getVideoid());
                    history.setOrderid(videos.get(0).getOrderid());
                    history.setChannelid(videos.get(0).getChannelid());
                    history.setRunning(1);
                    historyViewRepository.save(history);

                    Service service = serviceRepository.getInfoService(videos.get(0).getService());
                    resp.put("live",service.getLive()==1?"true":"fail");
                    resp.put("channel_id", videos.get(0).getChannelid());
                    resp.put("status", "true");
                    resp.put("video_id", videos.get(0).getVideoid());
                    resp.put("video_title", videos.get(0).getVideotitle());
                    resp.put("username", history.getUsername());
                    resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                    resp.put("like", "fail");
                    resp.put("sub", "fail");

                    String list_key = dataOrderRepository.getListKeyByOrderid(videos.get(0).getOrderid());
                    String key = "";
                    if (list_key != null && list_key.length() != 0) {
                        String[] keyArr = list_key.split(",");
                        key = keyArr[ran.nextInt(keyArr.length)];
                    }
                    resp.put("suggest_type", "fail");
                    resp.put("suggest_key", key.length() == 0 ? videos.get(0).getVideotitle() : key);
                    resp.put("suggest_video", "");

                    List<String> arrSource = new ArrayList<>();
                    for (int i = 0; i < service.getSuggest(); i++) {
                        arrSource.add("suggest");
                    }
                    for (int i = 0; i < service.getSearch(); i++) {
                        arrSource.add("search");
                    }
                    for (int i = 0; i < service.getDtn(); i++) {
                        arrSource.add("dtn");
                    }
                    resp.put("source", arrSource.get(ran.nextInt(arrSource.size())));

                    if (service.getMintime() != service.getMaxtime()&&service.getLive()==0) {
                        if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                            resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                        } else {
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(service.getLive()==1){
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                    } else {
                        if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                            resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                        } else {
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    historyViewRepository.save(history);
                    resp.put("status", "fail");
                    resp.put("username", history.getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            } else {
                List<HistoryView> histories = historyViewRepository.getHistoriesById(historieId);

                if (buffh == 1) {
                    if (histories.get(0).getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoBuffHVer2VNTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    } else if (histories.get(0).getGeo().equals("us")) {
                        videos = videoViewRepository.getvideoBuffHVer2USTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    }
                }else if(buffh == 2){
                    if (histories.get(0).getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoLiveVer2VNTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    } else {
                        videos = videoViewRepository.getvideoLiveVer2USTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    }
                } else {

                    if (histories.get(0).getGeo().equals("vn")) {
                        videos = videoViewRepository.getvideoViewVer2VNTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    } else {
                        videos = videoViewRepository.getvideoViewVer2USTEST(histories.get(0).getListvideo(), orderTrue.getValue());
                    }
                }
                if (videos.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVideoid(videos.get(0).getVideoid());
                    histories.get(0).setOrderid(videos.get(0).getOrderid());
                    histories.get(0).setChannelid(videos.get(0).getChannelid());
                } else {
                    historyViewRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để view!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

                Service service = serviceRepository.getInfoService(videos.get(0).getService());

                histories.get(0).setTimeget(System.currentTimeMillis());
                histories.get(0).setVps(vps);
                histories.get(0).setRunning(1);
                historyViewRepository.save(histories.get(0));
                resp.put("live",service.getLive()==1?"true":"fail");
                resp.put("channel_id", videos.get(0).getChannelid());
                resp.put("status", "true");
                resp.put("video_id", videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getVideotitle());
                resp.put("username", histories.get(0).getUsername());
                resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                resp.put("like", "fail");
                resp.put("sub", "fail");

                String list_key = dataOrderRepository.getListKeyByOrderid(videos.get(0).getOrderid());
                String key = "";
                if (list_key != null && list_key.length() != 0) {
                    String[] keyArr = list_key.split(",");
                    key = keyArr[ran.nextInt(keyArr.length)];
                }
                resp.put("suggest_type", "fail");
                resp.put("suggest_key", key.length() == 0 ? videos.get(0).getVideotitle() : key);
                resp.put("suggest_video", "");
                List<String> arrSource = new ArrayList<>();
                for (int i = 0; i < service.getSuggest(); i++) {
                    arrSource.add("suggest");
                }
                for (int i = 0; i < service.getSearch(); i++) {
                    arrSource.add("search");
                }
                for (int i = 0; i < service.getDtn(); i++) {
                    arrSource.add("dtn");
                }
                resp.put("source", arrSource.get(ran.nextInt(arrSource.size())));

                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                if (service.getMintime() != service.getMaxtime()&&service.getLive()==0) {
                    if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                    } else {
                        resp.put("video_duration", videos.get(0).getDuration());
                    }
                }else if(service.getLive()==1){
                    resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                } else {
                    if (videos.get(0).getDuration() > service.getMaxtime() * 60) {
                        resp.put("video_duration", service.getMintime() * 60 + (service.getMintime()<service.getMaxtime()?(ran.nextInt((service.getMaxtime() - service.getMintime()) * 60 + service.getMaxtime() >= 15 ? 120 : 0)):0));
                    } else {
                        resp.put("video_duration", videos.get(0).getDuration());
                    }
                }
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }

        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("fail", "sum");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/updatevideoid", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatevideoid(@RequestParam(defaultValue = "") String username,
                                         @RequestParam(defaultValue = "") String videoid, @RequestParam(defaultValue = "") String channelid, @RequestParam(defaultValue = "0") Integer duration) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "videoid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyViewRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                if (historyViewRepository.getListVideoById(historieId).length() > 200) {
                    historyViewRepository.updateListVideoNew(videoid, historieId);
                } else {
                    historyViewRepository.updateListVideo(videoid, historieId);
                }
                /*
                List<HistoryView> histories =historyViewRepository.getHistoriesById(historieId);
                if(histories.get(0).getListvideo().length()==0){
                    histories.get(0).setListvideo(videoid);
                }else{
                    histories.get(0).setListvideo(histories.get(0).getListvideo()+","+videoid);
                }
                //histories.get(0).setRunning(1);
                historyViewRepository.save(histories.get(0));

                 */
                resp.put("status", "true");
                resp.put("message", "Update videoid vào history thành công!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/update", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update(@RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid, @RequestParam(defaultValue = "") String channelid, @RequestParam(defaultValue = "0") Integer duration) {
        JSONObject resp = new JSONObject();

        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (videoid.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Videoid không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }

        try {

            Long historieId = historyViewRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                //histories.get(0).setProxy(proxy);
                //histories.get(0).setRunning(0);
                //histories.get(0).setVideoid("");
                //histories.get(0).setVps("");
                //historyRepository.save(histories.get(0));
                Integer check_duration = historyViewRepository.checkDurationViewByTimecheck(historieId, (long) (duration));
                if (check_duration > 0) {
                    HistoryViewSum historySum = new HistoryViewSum();
                    historySum.setVideoid(videoid.trim());
                    historySum.setUsername(username);
                    historySum.setTime(System.currentTimeMillis());
                    historySum.setChannelid(channelid);
                    historySum.setDuration(duration);
                    try {
                        historyViewSumRepository.save(historySum);
                    } catch (Exception e) {
                        try {
                            historyViewSumRepository.save(historySum);
                        } catch (Exception f) {
                        }
                    }
                    resp.put("status", "true");
                    resp.put("message", "Update view thành công!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                //historyViewRepository.updateduration(duration,username,videoid);
                resp.put("status", "fail");
                resp.put("message", "Không update duration !");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadbyusername", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String videoid) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            Long historieId = historyViewRepository.getId(username.trim());
            historyViewRepository.resetThreadBuffhById(historieId);
            Long historieIdC = historyCommentRepository.getId(username);
            historyCommentRepository.resetThreadBuffhById(historieIdC);
            //dataCommentRepository.resetRunningComment(username.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "getviewbuff7day", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> gettimebuff7day(@RequestParam(defaultValue = "") String user) {
        JSONObject resp = new JSONObject();
        try {
            List<String> time7day;
            if (user.length() == 0) {
                time7day = historyViewSumRepository.Gettimebuff7day();
            } else {
                time7day = historyViewSumRepository.Gettimebuff7day(user.trim());
            }

            JSONArray jsonArray = new JSONArray();
            Integer maxview = 0;

            for (int i = 0; i < time7day.size(); i++) {
                //System.out.println(time7day.get(i).split(",")[1]);
                if (maxview < Integer.parseInt(time7day.get(i).split(",")[1])) {
                    maxview = Integer.parseInt(time7day.get(i).split(",")[1]);
                }
            }
            for (int i = 0; i < time7day.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("date", time7day.get(i).split(",")[0]);
                obj.put("view", time7day.get(i).split(",")[1]);
                obj.put("maxview", maxview.toString());

                jsonArray.add(obj);
            }
            resp.put("view7day", jsonArray);

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
            historyViewRepository.resetThreadThan90mcron();
            historyViewRepository.resetThreadcron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delhistorysumcron", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delhistorysumcron() {
        JSONObject resp = new JSONObject();
        try {
            historyViewSumRepository.DelHistorySum();
            resp.put("status", "true");
            resp.put("message", "Delete history thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "delnamebyvps", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delnamebyvps(@RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            historyViewRepository.resetThreadViewByVps(vps.trim());
            historyCommentRepository.resetThreadViewByVps(vps.trim());
            dataCommentRepository.resetRunningCommentByVPS(vps.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "vpsrunning", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> vpsrunning(@RequestHeader(defaultValue = "") String Authorization) {
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins = adminRepository.FindByToken(Authorization.trim());
        if (Authorization.length() == 0 || admins.size() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            List<VpsRunning> vpsRunnings = historyViewRepository.getvpsrunning();

            //String a=orderRunnings.toString();
            JSONArray jsonArray = new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);
            Integer sum_total = 0;
            for (int i = 0; i < vpsRunnings.size(); i++) {
                sum_total = sum_total + vpsRunnings.get(i).getTotal();
                JSONObject obj = new JSONObject();
                obj.put("vps", vpsRunnings.get(i).getVps());
                obj.put("total", vpsRunnings.get(i).getTotal());
                obj.put("time", vpsRunnings.get(i).getTime());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");


            resp.put("computers", jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "deleteviewthan24h", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> deleteAllViewThan24h() {
        JSONObject resp = new JSONObject();
        try {
            historyViewRepository.deleteAllViewThan24h();
            resp.put("status", "true");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}
