package com.nts.awspremium.platform.tiktok;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.platform.youtube.YoutubeTask;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
public class TiktokUpdate {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;
    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;
    @Autowired
    private TikTokCommentHistoryRepository tikTokCommentHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    public Boolean tiktok_follower(String account_id,String task_key){
        try{
            TikTokFollowerHistory tikTokAccountHistory=tikTokAccountHistoryRepository.get_By_AccountId(account_id.trim());
            if(tikTokAccountHistory!=null){
                tikTokAccountHistory.setList_id(tikTokAccountHistory.getList_id()+task_key.trim()+"|");
                tikTokAccountHistory.setUpdate_time(System.currentTimeMillis());
                tikTokAccountHistoryRepository.save(tikTokAccountHistory);
            }else{
                TikTokFollowerHistory tikTokAccountHistory_New=new TikTokFollowerHistory();
                tikTokAccountHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                tikTokAccountHistory_New.setUpdate_time(System.currentTimeMillis());
                tikTokAccountHistory_New.setList_id(task_key.trim()+"|");
                tikTokAccountHistoryRepository.save(tikTokAccountHistory_New);
            }
            TiktokFollower24h tiktokFollower24h =new TiktokFollower24h();
            tiktokFollower24h.setId(account_id.trim()+task_key.trim());
            tiktokFollower24h.setUpdate_time(System.currentTimeMillis());
            tikTokFollower24hRepository.save(tiktokFollower24h);
            return true;
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
            return false;
        }
    }

    public Boolean tiktok_like(String account_id,String task_key){
        try{
            TikTokLikeHistory tikTokLikeHistory=tikTokLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(tikTokLikeHistory!=null){
                tikTokLikeHistory.setList_id(tikTokLikeHistory.getList_id()+task_key.trim()+"|");
                tikTokLikeHistory.setUpdate_time(System.currentTimeMillis());
                tikTokLikeHistoryRepository.save(tikTokLikeHistory);
            }else{
                TikTokLikeHistory tikTokLikeHistory_New=new TikTokLikeHistory();
                tikTokLikeHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                tikTokLikeHistory_New.setUpdate_time(System.currentTimeMillis());
                tikTokLikeHistory_New.setList_id(task_key.trim()+"|");
                tikTokLikeHistoryRepository.save(tikTokLikeHistory_New);
            }
            TiktokLike24h tiktokLike24h =new TiktokLike24h();
            tiktokLike24h.setId(account_id.trim()+task_key.trim());
            tiktokLike24h.setUpdate_time(System.currentTimeMillis());
            tikTokLike24hRepository.save(tiktokLike24h);
            return true;
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
            return false;
        }
    }
    public Boolean tiktok_comment(String account_id,String task_key,Boolean status){
        try{
            if(status==true){
                TikTokCommentHistory tikTokCommentHistory=tikTokCommentHistoryRepository.get_By_AccountId(account_id.trim());
                if(tikTokCommentHistory!=null){
                    tikTokCommentHistory.setList_id(tikTokCommentHistory.getList_id()+task_key.trim()+"|");
                    tikTokCommentHistory.setUpdate_time(System.currentTimeMillis());
                    tikTokCommentHistoryRepository.save(tikTokCommentHistory);
                }else{
                    TikTokCommentHistory tikTokCommentHistory_New=new TikTokCommentHistory();
                    tikTokCommentHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    tikTokCommentHistory_New.setUpdate_time(System.currentTimeMillis());
                    tikTokCommentHistory_New.setList_id(task_key.trim()+"|");
                    tikTokCommentHistoryRepository.save(tikTokCommentHistory_New);
                }
                dataCommentRepository.update_Task_Comment_Done(account_id.trim());
            }else {
                dataCommentRepository.update_Task_Comment_Fail(account_id.trim());
            }
            return true;
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
            return false;
        }
    }
}
