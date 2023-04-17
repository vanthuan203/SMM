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

import java.util.List;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/historyview")
public class HistoryViewController {
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private ProxyRepository proxyRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private VideoRepository videoRepository;

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
    private HistorySumRepository historySumRepository;
    @Autowired
    private DataOrderRepository dataOrderRepository;
    @Autowired
    private HistoryViewSumRepository historyViewSumRepository;
    @Autowired
    private ProxyHistoryRepository proxyHistoryRepository;
    @Autowired
    private IpV4Repository ipV4Repository;
    @GetMapping(value = "get",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> get(@RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "") String vps,@RequestParam(defaultValue = "0") Integer buffh ){
        JSONObject resp=new JSONObject();
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
        if(historyViewRepository.PROCESSLISTVIEW()>=50){
            resp.put("status", "fail");
            resp.put("username","");
            resp.put("fail", "video");
            resp.put("message", "Không còn video để view!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }
        Random ran=new Random();
            try{
                //Thread.sleep(ran.nextInt(2000));
                Long  historieId=historyViewRepository.getId(username);
                List<VideoView> videos=null;
                if(historieId==null){
                    HistoryView history=new HistoryView();
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
                    if(buffh==1){
                        videos=videoViewRepository.getvideoViewNoCheckMaxThreadViewBuff("");
                    }else{
                        if(history.getGeo().equals("vn")){
                            //videos=videoViewRepository.getvideoViewNoCheckMaxThreadVN("");
                            videos=videoViewRepository.getvideoViewVer2VN("");
                        }else if(history.getGeo().equals("us")){
                            //videos=videoViewRepository.getvideoViewNoCheckMaxThreadUS("");
                            videos=videoViewRepository.getvideoViewVer2US("");
                        }else if(history.getGeo().equals("vn-test")){
                            videos=videoViewRepository.getvideoViewNoCheckMaxThreadVNTEST("");
                        }else{
                            videos=videoViewRepository.getvideoViewNoCheckMaxThreadUSTEST("");
                        }
                    }
                    if(videos.size()>0){
                        history.setVideoid(videos.get(0).getVideoid());
                        history.setOrderid(videos.get(0).getOrderid());
                        history.setChannelid(videos.get(0).getChannelid());
                        history.setRunning(1);
                        historyViewRepository.save(history);

                        resp.put("channel_id", videos.get(0).getChannelid());
                        resp.put("status", "true");
                        resp.put("video_id", videos.get(0).getVideoid());
                        resp.put("video_title", videos.get(0).getVideotitle());
                        resp.put("username", history.getUsername());
                        resp.put("geo",accountRepository.getGeoByUsername(username.trim()));
                        if(videos.get(0).getService()==701 || videos.get(0).getService()==702){
                            int randLike =ran.nextInt(10000);
                            if(randLike<700){
                                resp.put("like","true");
                            }else{
                                resp.put("like","fail");
                            }
                            int randSub =ran.nextInt(10000);
                            if(randSub<100){
                                resp.put("sub","true");
                            }else{
                                resp.put("sub","fail");
                            }
                        }else if(videos.get(0).getService()<600 || history.getUsername().indexOf("@gmail")<0){
                            resp.put("like","fail");
                            resp.put("sub","fail");
                        }else{
                            int randLike =ran.nextInt(10000);
                            if(randLike<300){
                                resp.put("like","true");
                            }else{
                                resp.put("like","fail");
                            }
                            int randSub =ran.nextInt(10000);
                            if(randSub<100){
                                resp.put("sub","true");
                            }else{
                                resp.put("sub","fail");
                            }
                        }
                        String list_key= dataOrderRepository.getListKeyByOrderid(videos.get(0).getOrderid());
                        String key="";
                        if(list_key!=null && list_key.length()!=0){
                            String[] keyArr=list_key.split(",");
                            key=keyArr[ran.nextInt(keyArr.length)];
                        }
                        resp.put("suggest_type","fail");
                        resp.put("suggest_key",key.length()==0?videos.get(0).getVideotitle():key);
                        resp.put("suggest_video","");
                        if(videos.get(0).getService()==666 || videos.get(0).getService()==111 || videos.get(0).getService()==801 || videos.get(0).getService()==600 || videos.get(0).getService()==599||
                                videos.get(0).getService()==201 ){
                            if(ran.nextInt(10000)>5000){
                                resp.put("source", "dtn");
                            }else{
                                resp.put("source", "search");
                            }
                        }else  if(videos.get(0).getService()==668 || videos.get(0).getService()==112 || videos.get(0).getService()==802 || videos.get(0).getService()==202 || videos.get(0).getService()==203 || videos.get(0).getService()==667){
                            resp.put("source", "suggest");
                        } else if(videos.get(0).getService()==669 || videos.get(0).getService()==688||videos.get(0).getService()==689||videos.get(0).getService()==999 || videos.get(0).getService()==998 ||
                                videos.get(0).getService()==113 || videos.get(0).getService()==122||videos.get(0).getService()==123||
                                videos.get(0).getService()==811 || videos.get(0).getService()==812||videos.get(0).getService()==813 ||
                                videos.get(0).getService()==211 || videos.get(0).getService()==212||videos.get(0).getService()==213){
                            int rand =ran.nextInt(10000);
                            if(rand<5000){
                                resp.put("source", "suggest");
                                if(videos.get(0).getService()!=999 && videos.get(0).getService()!=998){
                                    resp.put("suggest_type","true");
                                }
                            }else if(rand<7500){
                                resp.put("source", "search");
                                if(videos.get(0).getService()!=999 && videos.get(0).getService()!=998){
                                    resp.put("video_title", key.length()==0?videos.get(0).getVideotitle():key);
                                }
                            }else{
                                resp.put("source", "dtn");
                            }
                        }else if(videos.get(0).getService()==701 || videos.get(0).getService()==702){
                            resp.put("source", "dtn");
                        }

                        if(videos.get(0).getService()==666 || videos.get(0).getService()==668 || videos.get(0).getService()==669 || videos.get(0).getService()==600 || videos.get(0).getService()==599 ||
                                videos.get(0).getService()==111 || videos.get(0).getService()==112 || videos.get(0).getService()==113||
                                videos.get(0).getService()==801 || videos.get(0).getService()==802 || videos.get(0).getService()==811 ||
                                videos.get(0).getService()==201 || videos.get(0).getService()==202||videos.get(0).getService()==211){
                            if(videos.get(0).getDuration()>240){
                                resp.put("video_duration", 180+ran.nextInt(60));
                            }else{
                                resp.put("video_duration", videos.get(0).getDuration());
                            }
                        }else if(videos.get(0).getService()==688 || videos.get(0).getService()==122 || videos.get(0).getService()==812 || videos.get(0).getService()==212){
                            if(videos.get(0).getDuration()>540){
                                resp.put("video_duration", 300+ran.nextInt(240));
                            }else{
                                resp.put("video_duration", videos.get(0).getDuration());
                            }
                        }else if(videos.get(0).getService()==689 || videos.get(0).getService()==123 || videos.get(0).getService()==813 || videos.get(0).getService()==213 || videos.get(0).getService()==203 || videos.get(0).getService()==667){
                            if(videos.get(0).getDuration()>1200){
                                resp.put("video_duration", 900+ran.nextInt(300));
                            }else{
                                resp.put("video_duration", videos.get(0).getDuration());
                            }
                        }else if(videos.get(0).getService()==998){
                            if(videos.get(0).getDuration()>1920){
                                resp.put("video_duration", 1800+ran.nextInt(120));
                            }else{
                                resp.put("video_duration", videos.get(0).getDuration());
                            }
                        }else if(videos.get(0).getService()==999){
                            if(videos.get(0).getDuration()>3720){
                                resp.put("video_duration", 3600+ran.nextInt(120));
                            }else{
                                resp.put("video_duration", videos.get(0).getDuration());
                            }
                        }else if(videos.get(0).getService()==701){
                            int ran_time=(int)(1800*(Math.random()*(1-0.6+1)+0.6));
                            if(ran_time>videos.get(0).getDuration()){
                                resp.put("video_duration",videos.get(0).getDuration() );
                            }else{
                                resp.put("video_duration",ran_time );
                            }
                        }else if(videos.get(0).getService()==702){
                            int ran_time=(int)(3600*(Math.random()*(1-0.6+1)+0.6));
                            if(ran_time>videos.get(0).getDuration()){
                                resp.put("video_duration",videos.get(0).getDuration() );
                            }else{
                                resp.put("video_duration",ran_time );
                            }
                        }

                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }else{
                        historyViewRepository.save(history);
                        resp.put("status", "fail");
                        resp.put("username",history.getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                }else{
                    List<HistoryView> histories=historyViewRepository.getHistoriesById(historieId);
                    //System.out.println(System.currentTimeMillis()-histories.get(0).getTimeget());
                    if(System.currentTimeMillis()-histories.get(0).getTimeget()<(60000L+ (long) ran.nextInt(60000))){
                        //histories.get(0).setTimeget(System.currentTimeMillis());
                        //historyViewRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username",histories.get(0).getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    if(buffh==1){
                        if(histories.get(0).getListvideo().length()>600){
                            videos=videoViewRepository.getvideoViewLoopNoCheckMaxThreadViewBuff(histories.get(0).getListvideo());
                        }else{
                            videos=videoViewRepository.getvideoViewNoCheckMaxThreadViewBuff(histories.get(0).getListvideo());
                        }
                    }else{
                        if(histories.get(0).getListvideo().length()>600){
                            if(histories.get(0).getGeo().equals("vn")){
                                //videos=videoViewRepository.getvideoViewLoopNoCheckMaxThreadVN(histories.get(0).getListvideo());
                                videos=videoViewRepository.getvideoViewLoopVer2VN(histories.get(0).getListvideo());
                            }else if(histories.get(0).getGeo().equals("us")){
                                //videos=videoViewRepository.getvideoViewLoopNoCheckMaxThreadUS(histories.get(0).getListvideo());
                                videos=videoViewRepository.getvideoViewLoopVer2US(histories.get(0).getListvideo());
                            }else if(histories.get(0).getGeo().equals("vn-test")){
                                videos=videoViewRepository.getvideoViewLoopNoCheckMaxThreadVNTEST(histories.get(0).getListvideo());
                            }else{
                                videos=videoViewRepository.getvideoViewLoopNoCheckMaxThreadUSTEST(histories.get(0).getListvideo());
                            }
                        }else{
                            if(histories.get(0).getGeo().equals("vn")){
                                //videos=videoViewRepository.getvideoViewNoCheckMaxThreadVN(histories.get(0).getListvideo());
                                videos=videoViewRepository.getvideoViewVer2VN(histories.get(0).getListvideo());
                            }else if(histories.get(0).getGeo().equals("us")){
                                //videos=videoViewRepository.getvideoViewNoCheckMaxThreadUS(histories.get(0).getListvideo());
                                videos=videoViewRepository.getvideoViewVer2US(histories.get(0).getListvideo());
                            }else if(histories.get(0).getGeo().equals("vn-test")){
                                videos=videoViewRepository.getvideoViewNoCheckMaxThreadVNTEST(histories.get(0).getListvideo());
                            }else{
                                videos=videoViewRepository.getvideoViewNoCheckMaxThreadUSTEST(histories.get(0).getListvideo());
                            }
                        }
                    }
                    if(videos.size()>0){
                        histories.get(0).setTimeget(System.currentTimeMillis());
                        histories.get(0).setVideoid(videos.get(0).getVideoid());
                        histories.get(0).setOrderid(videos.get(0).getOrderid());
                        histories.get(0).setChannelid(videos.get(0).getChannelid());
                    }else{
                        historyViewRepository.save(histories.get(0));
                        resp.put("status", "fail");
                        resp.put("username",histories.get(0).getUsername());
                        resp.put("fail", "video");
                        resp.put("message", "Không còn video để view!");
                        return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                    }
                    histories.get(0).setTimeget(System.currentTimeMillis());
                    histories.get(0).setVps(vps);
                    histories.get(0).setRunning(1);
                    historyViewRepository.save(histories.get(0));
                    resp.put("channel_id", videos.get(0).getChannelid());
                    resp.put("status", "true");
                    resp.put("video_id", videos.get(0).getVideoid());
                    resp.put("video_title", videos.get(0).getVideotitle());
                    resp.put("username", histories.get(0).getUsername());
                    resp.put("geo", accountRepository.getGeoByUsername(username.trim()));
                    if(videos.get(0).getService()==701 || videos.get(0).getService()==702){
                        int randLike =ran.nextInt(10000);
                        if(randLike<700){
                            resp.put("like","true");
                        }else{
                            resp.put("like","fail");
                        }
                        int randSub =ran.nextInt(10000);
                        if(randSub<100){
                            resp.put("sub","true");
                        }else{
                            resp.put("sub","fail");
                        }
                    }else if(videos.get(0).getService()<600 || histories.get(0).getUsername().indexOf("@gmail")<0){
                        resp.put("like","fail");
                        resp.put("sub","fail");
                    }else{
                        int randLike =ran.nextInt(10000);
                        if(randLike<300){
                            resp.put("like","true");
                        }else{
                            resp.put("like","fail");
                        }
                        int randSub =ran.nextInt(10000);
                        if(randSub<100){
                            resp.put("sub","true");
                        }else{
                            resp.put("sub","fail");
                        }
                    }

                    String list_key= dataOrderRepository.getListKeyByOrderid(videos.get(0).getOrderid());
                    String key="";
                    if(list_key!=null && list_key.length()!=0){
                        String[] keyArr=list_key.split(",");
                        key=keyArr[ran.nextInt(keyArr.length)];
                    }
                    resp.put("suggest_type","fail");
                    resp.put("suggest_key",key.length()==0?videos.get(0).getVideotitle():key);
                    resp.put("suggest_video","");
                    if(videos.get(0).getService()==666 || videos.get(0).getService()==111 || videos.get(0).getService()==801 || videos.get(0).getService()==600 || videos.get(0).getService()==599||
                            videos.get(0).getService()==201 ){
                        if(ran.nextInt(10000)>5000){
                            resp.put("source", "dtn");
                        }else{
                            resp.put("source", "search");
                        }
                    }else  if(videos.get(0).getService()==668 || videos.get(0).getService()==112 || videos.get(0).getService()==802 || videos.get(0).getService()==202 || videos.get(0).getService()==203 || videos.get(0).getService()==667){
                        resp.put("source", "suggest");
                    } else if(videos.get(0).getService()==669 || videos.get(0).getService()==688||videos.get(0).getService()==689||videos.get(0).getService()==999 || videos.get(0).getService()==998 ||
                            videos.get(0).getService()==113 || videos.get(0).getService()==122||videos.get(0).getService()==123||
                            videos.get(0).getService()==811 || videos.get(0).getService()==812||videos.get(0).getService()==813 ||
                            videos.get(0).getService()==211 || videos.get(0).getService()==212||videos.get(0).getService()==213){
                        int rand =ran.nextInt(10000);
                        if(rand<5000){
                            resp.put("source", "suggest");
                            if(videos.get(0).getService()!=999 && videos.get(0).getService()!=998){
                                resp.put("suggest_type","true");
                            }
                        }else if(rand<7500){
                            resp.put("source", "search");
                            if(videos.get(0).getService()!=999 && videos.get(0).getService()!=998){
                                resp.put("video_title", key.length()==0?videos.get(0).getVideotitle():key);
                            }
                        }else{
                            resp.put("source", "dtn");
                        }
                    }else if(videos.get(0).getService()==701 || videos.get(0).getService()==702){
                        resp.put("source", "dtn");
                    }

                    if(videos.get(0).getService()==666 || videos.get(0).getService()==668 || videos.get(0).getService()==669 || videos.get(0).getService()==600 || videos.get(0).getService()==599 ||
                            videos.get(0).getService()==111 || videos.get(0).getService()==112 || videos.get(0).getService()==113||
                            videos.get(0).getService()==801 || videos.get(0).getService()==802 || videos.get(0).getService()==811 ||
                            videos.get(0).getService()==201 || videos.get(0).getService()==202||videos.get(0).getService()==211){
                        if(videos.get(0).getDuration()>240){
                            resp.put("video_duration", 180+ran.nextInt(60));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getService()==688 || videos.get(0).getService()==122 || videos.get(0).getService()==812 || videos.get(0).getService()==212){
                        if(videos.get(0).getDuration()>540){
                            resp.put("video_duration", 300+ran.nextInt(240));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getService()==689 || videos.get(0).getService()==123 || videos.get(0).getService()==813 || videos.get(0).getService()==213 || videos.get(0).getService()==203 || videos.get(0).getService()==667){
                        if(videos.get(0).getDuration()>1200){
                            resp.put("video_duration", 900+ran.nextInt(300));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getService()==998){
                        if(videos.get(0).getDuration()>1920){
                            resp.put("video_duration", 1800+ran.nextInt(120));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getService()==999){
                        if(videos.get(0).getDuration()>3720){
                            resp.put("video_duration", 3600+ran.nextInt(120));
                        }else{
                            resp.put("video_duration", videos.get(0).getDuration());
                        }
                    }else if(videos.get(0).getService()==701){
                        int ran_time=(int)(1800*(Math.random()*(1-0.6+1)+0.6));
                        if(ran_time>videos.get(0).getDuration()){
                            resp.put("video_duration",videos.get(0).getDuration() );
                        }else{
                            resp.put("video_duration",ran_time );
                        }
                    }else if(videos.get(0).getService()==702){
                        int ran_time=(int)(3600*(Math.random()*(1-0.6+1)+0.6));
                        if(ran_time>videos.get(0).getDuration()){
                            resp.put("video_duration",videos.get(0).getDuration() );
                        }else{
                            resp.put("video_duration",ran_time );
                        }
                    }
                    return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
                }

            }catch (Exception e){
                resp.put("status","fail");
                resp.put("fail","sum");
                resp.put("message",e.getMessage());
                return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
            }
    }
    @GetMapping(value = "/updatevideoid",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> updatevideoid( @RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String channelid,@RequestParam(defaultValue = "0") Integer duration){
        JSONObject resp=new JSONObject();
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
        try{
            Long  historieId=historyViewRepository.getId(username);
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                if(historyViewRepository.getListVideoById(historieId).length()>200){
                    historyViewRepository.updateListVideoNew(videoid,historieId);
                }else{
                    historyViewRepository.updateListVideo(videoid,historieId);
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
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/update",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> update( @RequestParam(defaultValue = "") String username,
                                  @RequestParam(defaultValue = "") String videoid,@RequestParam(defaultValue = "") String channelid,@RequestParam(defaultValue = "0") Integer duration){
        JSONObject resp=new JSONObject();

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

        try{

            Long  historieId=historyViewRepository.getId(username);
            if(historieId==null){
                resp.put("status", "fail");
                resp.put("message", "Không tìm thấy username!");
                return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
            }
            else{
                //histories.get(0).setProxy(proxy);
                //histories.get(0).setRunning(0);
                //histories.get(0).setVideoid("");
                //histories.get(0).setVps("");
                //historyRepository.save(histories.get(0));
                Integer check_duration= historyViewRepository.checkDurationViewByTimecheck(historieId,(long)(duration));
                if(check_duration>0){
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
        }catch (Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "delthreadbyusername",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadbyusername(@RequestParam(defaultValue = "") String username,@RequestParam(defaultValue = "") String videoid){
        JSONObject resp=new JSONObject();
        if (username.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "username không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            Long  historieId=historyViewRepository.getId(username.trim());
            historyViewRepository.resetThreadBuffhById(historieId);
            //historyCommentRepository.resetThreadBuffhByUsername(username.trim());
            //dataCommentRepository.resetRunningComment(username.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(value = "getviewbuff7day",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> gettimebuff7day(@RequestParam(defaultValue = "") String user){
        JSONObject resp=new JSONObject();
        try{
            List<String> time7day;
            if(user.length()==0){
                time7day=historyViewSumRepository.Gettimebuff7day();
            }else{
                time7day=historyViewSumRepository.Gettimebuff7day(user.trim());
            }

            JSONArray jsonArray=new JSONArray();
            Integer maxview=0;

            for(int i=0;i<time7day.size();i++){
                //System.out.println(time7day.get(i).split(",")[1]);
                if(maxview<Integer.parseInt(time7day.get(i).split(",")[1])){
                    maxview=Integer.parseInt(time7day.get(i).split(",")[1]);
                }
            }
            for(int i=0;i<time7day.size();i++){
                JSONObject obj=new JSONObject();
                obj.put("date", time7day.get(i).split(",")[0]);
                obj.put("view", time7day.get(i).split(",")[1]);
                obj.put("maxview",maxview.toString());

                jsonArray.add(obj);
            }
            resp.put("view7day", jsonArray);

            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delthreadcron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delthreadcron(){
        JSONObject resp=new JSONObject();
        try{
            historyViewRepository.resetThreadThan90mcron();
            historyViewRepository.resetThreadcron();
            resp.put("status", "true");
            resp.put("message", "Reset thread error thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "delhistorysumcron",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delhistorysumcron(){
        JSONObject resp=new JSONObject();
        try{
            historyViewSumRepository.DelHistorySum();
            resp.put("status", "true");
            resp.put("message", "Delete history thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping(value = "delnamebyvps",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> delnamebyvps(@RequestParam(defaultValue = "") String vps){
        JSONObject resp=new JSONObject();
        if (vps.length() == 0) {
            resp.put("status", "fail");
            resp.put("message", "vps không để trống");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
        try{
            historyViewRepository.resetThreadViewByVps(vps.trim());
            historyCommentRepository.resetThreadViewByVps(vps.trim());
            dataCommentRepository.resetRunningCommentByVPS(vps.trim());
            resp.put("status", "true");
            resp.put("message", "Update running thành công!");
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.OK);
        }catch(Exception e){
            resp.put("status", "fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "vpsrunning",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> vpsrunning(@RequestHeader(defaultValue = "") String Authorization){
        JSONObject resp = new JSONObject();
        //Integer checktoken= adminRepository.FindAdminByToken(Authorization.split(",")[0]);
        List<Admin> admins=adminRepository.FindByToken(Authorization.trim());
        if(Authorization.length()==0|| admins.size()==0){
            resp.put("status","fail");
            resp.put("message", "Token expired");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.BAD_REQUEST);
        }
        try{
            List<VpsRunning> vpsRunnings=historyViewRepository.getvpsrunning();

            //String a=orderRunnings.toString();
            JSONArray jsonArray= new JSONArray();

            //JSONObject jsonObject=new JSONObject().put("")
            //JSONObject jsonObject= (JSONObject) new JSONObject().put("Channelid",orderRunnings.get(0).toString());
            //jsonArray.add(orderRunnings);
            Integer sum_total=0;
            for(int i=0;i<vpsRunnings.size();i++){
                sum_total=sum_total+vpsRunnings.get(i).getTotal();
                JSONObject obj = new JSONObject();
                obj.put("vps", vpsRunnings.get(i).getVps());
                obj.put("total", vpsRunnings.get(i).getTotal());
                obj.put("time",  vpsRunnings.get(i).getTime());
                jsonArray.add(obj);
            }
            //JSONArray lineItems = jsonObject.getJSONArray("lineItems");


            resp.put("computers",jsonArray);
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(path = "deleteviewthan24h",produces = "application/hal+json;charset=utf8")
    ResponseEntity<String> deleteAllViewThan24h(){
        JSONObject resp = new JSONObject();
        try{
            historyViewRepository.deleteAllViewThan24h();
            resp.put("status","true");
            return new ResponseEntity<String>(resp.toJSONString(),HttpStatus.OK);
        }catch (Exception e){
            resp.put("status","fail");
            resp.put("message", e.getMessage());
            return new ResponseEntity<String>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }
}
