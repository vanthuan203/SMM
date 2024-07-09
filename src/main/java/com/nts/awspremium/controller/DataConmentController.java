package com.nts.awspremium.controller;

import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.LogError;
import com.nts.awspremium.model.OrderRunning;
import com.nts.awspremium.model.ProfileShow;
import com.nts.awspremium.repositories.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/data_comment")
public class DataConmentController {
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @GetMapping(value = "update_Running_Comment", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> update_Running_Comment() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            List<OrderRunning> orderCommentList=orderRunningRepository.get_Order_Comment_Pending();
            for(int i=0;i<orderCommentList.size();i++){
                String [] comments=orderCommentList.get(i).getComment_list().split("\n");
                for (int j=0;j<comments.length;j++){
                    if(comments[j].trim().length()==0){
                        continue;
                    }
                    DataComment dataComment=new DataComment();
                    dataComment.setComment(comments[j].trim());
                    dataComment.setOrderRunning(orderCommentList.get(i));
                    dataComment.setGet_time(0L);
                    dataComment.setAccount_id("");
                    dataComment.setDevice_id("");
                    dataComment.setRunning(0);
                    dataCommentRepository.save(dataComment);
                }
                orderCommentList.get(i).setStart_time(System.currentTimeMillis());
                orderCommentList.get(i).setThread(orderCommentList.get(i).getService().getThread());
                orderRunningRepository.save(orderCommentList.get(i));
            }
            resp.put("status",true);
            data.put("message", "update thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping(value = "reset_Running_Comment", produces = "application/hal+json;charset=utf8")
    public ResponseEntity<Map<String, Object>> reset_Running_Comment() throws InterruptedException {
        Map<String, Object> resp = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        try{
            dataCommentRepository.reset_Running_Comment();
            resp.put("status",true);
            data.put("message", "reset thành công");
            resp.put("data",data);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }catch (Exception e){
            StackTraceElement stackTraceElement = Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0);
            LogError logError =new LogError();
            logError.setMethod_name(stackTraceElement.getMethodName());
            logError.setLine_number(stackTraceElement.getLineNumber());
            logError.setClass_name(stackTraceElement.getClassName());
            logError.setFile_name(stackTraceElement.getFileName());
            logError.setMessage(e.getMessage());
            logError.setAdd_time(System.currentTimeMillis());
            Date date_time = new Date(System.currentTimeMillis());
            // Tạo SimpleDateFormat với múi giờ GMT+7
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String formattedDate = sdf.format(date_time);
            logError.setDate_time(formattedDate);
            logErrorRepository.save(logError);

            resp.put("status", false);
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

    }
}
