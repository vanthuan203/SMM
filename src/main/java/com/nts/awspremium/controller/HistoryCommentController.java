package com.nts.awspremium.controller;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historycomment")
public class HistoryCommentController {


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VideoCommentRepository videoCommentRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private HistoryViewRepository historyViewRepository;

    @Autowired
    private HistoryCommentRepository historyCommentRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private HistoryViewSumRepository historyViewSumRepository;

    @Autowired
    private HistoryCommentSumRepository historyCommentSumRepository;

    @Autowired
    private VpsRepository vpsRepository;
    @Autowired
    private ProxyRepository proxyRepository;


    @GetMapping(value = "get", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username, @RequestParam(defaultValue = "") String vps) {
        JSONObject resp = new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if(vpsRepository.checkVpsCmtTrue(vps.trim())==0){
            resp.put("status", "fail");
            resp.put("message", "Vps không chạy cmt!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        String proxies="";
        if(proxyRepository.checkProxyLiveByUsername(username.trim())==0){
            if (accountRepository.getGeoByUsername(username.trim()).equals("cmt-vn") || accountRepository.getGeoByUsername(username.trim()).equals("vn")) {
                proxies=proxyRepository.getProxyRandByGeo("vn");
            } else if (accountRepository.getGeoByUsername(username.trim()).equals("cmt-us") || accountRepository.getGeoByUsername(username.trim()).equals("us")) {
                proxies=proxyRepository.getProxyRandByGeo("us");
            } else if (accountRepository.getGeoByUsername(username.trim()).equals("cmt-kr") || accountRepository.getGeoByUsername(username.trim()).equals("kr")) {
                proxies=proxyRepository.getProxyRandByGeo("kr");
            }
            if(proxies.length()==0){
                resp.put("status", "fail");
                resp.put("message", "Proxy die!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
        }
        /*
        if (historyViewRepository.PROCESSLISTVIEW() >= 40) {
            resp.put("status", "fail");
            resp.put("username", "");
            resp.put("fail", "video");
            resp.put("message", "Không còn video để comment!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
         */
        Random ran = new Random();
        try {
            Thread.sleep(ran.nextInt(1000));
            Long historieId = historyCommentRepository.getId(username);
            List<VideoComment> videos = null;
            if (historieId == null) {
                HistoryComment history = new HistoryComment();
                history.setUsername(username);
                history.setListvideo("");
                history.setRunning(0);
                history.setVps(vps);
                history.setVideoid("");
                history.setOrderid(0L);
                history.setGeo(accountRepository.getGeoByUsername(username.trim()));
                history.setTimeget(System.currentTimeMillis());
                if (history.getGeo().equals("cmt-vn") || history.getGeo().equals("vn")) {
                    videos = videoCommentRepository.getvideoCommentVN("");
                } else if (history.getGeo().equals("cmt-us") || history.getGeo().equals("us")) {
                    videos = videoCommentRepository.getvideoCommentUS("");
                }else if (history.getGeo().equals("cmt-kr") || history.getGeo().equals("kr")) {
                    videos = videoCommentRepository.getvideoCommentKR("");
                }else{
                    resp.put("status", "fail");
                    resp.put("message", "Username không cmt!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videos.size() > 0) {
                    history.setVideoid(videos.get(0).getVideoid());
                    history.setOrderid(videos.get(0).getOrderid());
                    history.setRunning(1);
                    historyCommentRepository.save(history);
                    /*
                    if(dataCommentRepository.checkCommentDone(videos.get(0).getOrderid())>0){
                        resp.put("status", "fail");
                        resp.put("username", history.getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để comment!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }


                    if(historyCommentRepository.CheckGetTaskComment("%orderid="+videos.get(0).getOrderid().toString().trim()+"%")>0){
                        history.setRunning(0);
                        historyCommentRepository.save(history);
                        resp.put("status", "fail");
                        resp.put("username", history.getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để comment!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    */
                    //System.out.println("%(select orderid from videocomment) and orderid="+videos.get(0).getOrderid().toString().trim()+"%");
                    //System.out.println(historyCommentRepository.CheckGetTaskComment("%(select orderid from videocomment) and orderid="+videos.get(0).getOrderid().toString().trim()+"%"));
                    dataCommentRepository.updateRunningComment(System.currentTimeMillis(),username.trim(),vps.trim(),videos.get(0).getOrderid());
                    Thread.sleep(ran.nextInt(1000)+500);
                    String comment=dataCommentRepository.getCommentByOrderIdAndUsername(videos.get(0).getOrderid(),username.trim());
                    if(comment!=null){
                        if(historyCommentSumRepository.checkCommentIdTrue(Long.parseLong(comment.split(",")[0]))>0){
                            dataCommentRepository.updateRunningCommentDone(Long.parseLong(comment.split(",")[0]));
                            history.setRunning(0);
                            historyCommentRepository.save(history);
                            resp.put("status", "fail");
                            resp.put("username", history.getUsername());
                            resp.put("fail", "video");
                            resp.put("message", "Không còn video để comment!");
                            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                        }
                        resp.put("comment_id", comment.split(",")[0]);
                        resp.put("comment", comment.substring(comment.indexOf(",")+1));
                    }else{
                        history.setRunning(0);
                        historyCommentRepository.save(history);
                        resp.put("status", "fail");
                        resp.put("username", history.getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để comment!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    resp.put("channel_id", videos.get(0).getChannelid());
                    resp.put("status", "true");
                    resp.put("video_id", videos.get(0).getVideoid());
                    resp.put("video_title", videos.get(0).getVideotitle());
                    resp.put("username", history.getUsername());
                    resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                    resp.put("proxy",proxies.length()==0?accountRepository.getProxyByUsername(username.trim()):proxies);
                    if (ran.nextInt(10000) > 5000) {
                        resp.put("source", "dtn");
                    } else {
                        resp.put("source", "search");
                    }
                    if (videos.get(0).getDuration() > 360) {
                        resp.put("video_duration", 180 + ran.nextInt(180));
                    } else {
                        resp.put("video_duration", videos.get(0).getDuration());
                    }
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                } else {
                    historyCommentRepository.save(history);
                    resp.put("status", "fail");
                    resp.put("username", history.getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
            } else {
                List<HistoryComment> histories = historyCommentRepository.getHistoriesById(historieId);
                //System.out.println(System.currentTimeMillis()-histories.get(0).getTimeget());
                if (System.currentTimeMillis() - histories.get(0).getTimeget() < (60000L + (long) ran.nextInt(60000))) {
                    //histories.get(0).setTimeget(System.currentTimeMillis());
                    //historyViewRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (histories.get(0).getGeo().equals("vn") || histories.get(0).getGeo().equals("cmt-vn")) {
                    //videos=videoViewRepository.getvideoViewNoCheckMaxThreadVN(histories.get(0).getListvideo());
                    videos = videoCommentRepository.getvideoCommentVN(histories.get(0).getListvideo());
                } else if (histories.get(0).getGeo().equals("us") || histories.get(0).getGeo().equals("cmt-us")) {
                    //videos=videoViewRepository.getvideoViewNoCheckMaxThreadUS(histories.get(0).getListvideo());
                    videos = videoCommentRepository.getvideoCommentUS(histories.get(0).getListvideo());
                }else if (histories.get(0).getGeo().equals("kr") || histories.get(0).getGeo().equals("cmt-kr")) {
                    //videos=videoViewRepository.getvideoViewNoCheckMaxThreadUS(histories.get(0).getListvideo());
                    videos = videoCommentRepository.getvideoCommentKR(histories.get(0).getListvideo());
                }else{
                    resp.put("status", "fail");
                    resp.put("message", "Username không cmt!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                if (videos.size() > 0) {
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVideoid(videos.get(0).getVideoid());
                    histories.get(0).setOrderid(videos.get(0).getOrderid());
                    histories.get(0).setRunning(1);
                    historyCommentRepository.save(histories.get(0));
                } else {
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                /*
                if(dataCommentRepository.checkCommentDone(videos.get(0).getOrderid())>0){
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }


                if(historyCommentRepository.CheckGetTaskComment("%orderid="+videos.get(0).getOrderid().toString().trim()+"%")>0){
                    histories.get(0).setRunning(0);
                    historyCommentRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                 */
                dataCommentRepository.updateRunningComment(System.currentTimeMillis(),username.trim(),vps.trim(),videos.get(0).getOrderid());
                Thread.sleep(ran.nextInt(1000)+500);
                String comment=dataCommentRepository.getCommentByOrderIdAndUsername(videos.get(0).getOrderid(),username.trim());
                if(comment!=null){
                    if(historyCommentSumRepository.checkCommentIdTrue(Long.parseLong(comment.split(",")[0]))>0){
                        dataCommentRepository.updateRunningCommentDone(Long.parseLong(comment.split(",")[0]));
                        histories.get(0).setRunning(0);
                        historyCommentRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username", histories.get(0).getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để comment!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    resp.put("comment_id", comment.split(",")[0]);
                    resp.put("comment", comment.substring(comment.indexOf(",")+1));
                }else{
                    histories.get(0).setRunning(0);
                    historyCommentRepository.save(histories.get(0));
                    resp.put("status", "fail");
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("fail", "video");
                    resp.put("message", "Không còn video để comment!");
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }
                histories.get(0).setTimeget(System.currentTimeMillis());
                histories.get(0).setVps(vps);
                histories.get(0).setRunning(1);
                historyCommentRepository.save(histories.get(0));
                resp.put("channel_id", videos.get(0).getChannelid());
                resp.put("status", "true");
                resp.put("video_id", videos.get(0).getVideoid());
                resp.put("video_title", videos.get(0).getVideotitle());
                resp.put("username", histories.get(0).getUsername());
                resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                resp.put("proxy", proxies.length()==0?accountRepository.getProxyByUsername(username.trim()):proxies);
                if (ran.nextInt(10000) > 5000) {
                    resp.put("source", "dtn");
                } else {
                    resp.put("source", "search");
                }
                if (videos.get(0).getDuration() > 360) {
                    resp.put("video_duration", 180 + ran.nextInt(180));
                } else {
                    resp.put("video_duration", videos.get(0).getDuration());
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
                                         @RequestParam(defaultValue = "") String videoid, @RequestParam(defaultValue = "0") Long comment_id ) {
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
            Long historieId = historyCommentRepository.getId(username);
            if (historieId == null) {
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            } else {
                if(historyCommentSumRepository.checkCommentIdTrue(comment_id)==0){
                    dataCommentRepository.updateRunningCommentDone(comment_id);
                    HistoryCommentSum historySum = new HistoryCommentSum();
                    historySum.setUsername(username);
                    historySum.setTime(System.currentTimeMillis());
                    historySum.setCommentid(comment_id);
                    historySum.setCommnent(dataCommentRepository.getCommentByCommentId(comment_id));
                    historySum.setOrderid(videoCommentRepository.getOrderIdByVideoId(videoid.trim()));
                    try {
                        historyCommentSumRepository.save(historySum);
                    } catch (Exception e) {
                        try {
                            historyCommentSumRepository.save(historySum);
                        } catch (Exception f) {
                        }
                    }
                    if (historyCommentRepository.getListVideoById(historieId).length() > 44) {
                        historyCommentRepository.updateListVideoNew(videoid, historieId);
                    } else {
                        historyCommentRepository.updateListVideo(videoid, historieId);
                    }
                }
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

    @GetMapping(value = "/checkcmttrue", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> checkcmttrue(@RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "0") Long comment_id ) {
        JSONObject resp = new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "Username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        if (comment_id == 0) {
            resp.put("status", "fail");
            resp.put("message", "comment_id không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try {
            if(historyCommentSumRepository.checkCommentIdTrue(comment_id)>0||dataCommentRepository.getCommentByCommentIdAndUsername(comment_id,username.trim())==0){
                resp.put("status", "fail");
                resp.put("message", "Không cmt!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }else{
                resp.put("status", "true");
                resp.put("message", "Sẵn sàng cmt!");
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
            Long historieId = historyCommentRepository.getId(username);
            historyCommentRepository.resetThreadBuffhById(historieId);
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
            dataCommentRepository.resetRunningCommentByRunningHisCron();
            historyCommentRepository.resetThreadThan15mcron();
            dataCommentRepository.resetRunningCommentByCron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "delcommentdone", produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delcommentdone() {
        JSONObject resp = new JSONObject();
        try {
            dataCommentRepository.deleteCommentDoneByCron();
            resp.put("status", "true");
            resp.put("message", "Delete comment thành công!");
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
