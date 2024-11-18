package com.nts.awspremium.platform.Instagram;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
public class InstagramUpdate {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private InstagramFollower24hRepository instagramFollower24hRepository;
    @Autowired
    private InstagramLike24hRepository instagramLike24hRepository;
    @Autowired
    private InstagramComment24hRepository instagramComment24hRepository;
    @Autowired
    private InstagramView24hRepository instagramView24hRepository;
    @Autowired
    private InstagramFollowerHistoryRepository instagramFollowerHistoryRepository;
    @Autowired
    private InstagramLikeHistoryRepository instagramLikeHistoryRepository;
    @Autowired
    private InstagramCommentHistoryRepository instagramCommentHistoryRepository;
    @Autowired
    private InstagramViewHistoryRepository instagramViewHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    public Boolean instagram_follower(String account_id,String task_key){
        try{
            InstagramFollowerHistory instagramFollowerHistory=instagramFollowerHistoryRepository.get_By_AccountId(account_id.trim());
            if(instagramFollowerHistory!=null){
                instagramFollowerHistory.setList_id(instagramFollowerHistory.getList_id()+task_key.trim()+"|");
                instagramFollowerHistory.setUpdate_time(System.currentTimeMillis());
                instagramFollowerHistoryRepository.save(instagramFollowerHistory);
            }else{
                InstagramFollowerHistory instagramFollowerHistory_new=new InstagramFollowerHistory();
                instagramFollowerHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                instagramFollowerHistory_new.setUpdate_time(System.currentTimeMillis());
                instagramFollowerHistory_new.setList_id(task_key.trim()+"|");
                instagramFollowerHistoryRepository.save(instagramFollowerHistory_new);
            }
            InstagramFollower24h instagramFollower24h =new InstagramFollower24h();
            instagramFollower24h.setId(account_id.trim()+task_key.trim());
            instagramFollower24h.setUpdate_time(System.currentTimeMillis());
            instagramFollower24hRepository.save(instagramFollower24h);
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
    public Boolean instagram_like(String account_id,String task_key){
        try{
            InstagramLikeHistory instagramLikeHistory=instagramLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(instagramLikeHistory!=null){
                instagramLikeHistory.setList_id(instagramLikeHistory.getList_id()+task_key.trim()+"|");
                instagramLikeHistory.setUpdate_time(System.currentTimeMillis());
                instagramLikeHistoryRepository.save(instagramLikeHistory);
            }else{
                InstagramLikeHistory instagramLikeHistory_new=new InstagramLikeHistory();
                instagramLikeHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                instagramLikeHistory_new.setUpdate_time(System.currentTimeMillis());
                instagramLikeHistory_new.setList_id(task_key.trim()+"|");
                instagramLikeHistoryRepository.save(instagramLikeHistory_new);
            }
            InstagramLike24h instagramLike24h =new InstagramLike24h();
            instagramLike24h.setId(account_id.trim()+task_key.trim());
            instagramLike24h.setUpdate_time(System.currentTimeMillis());
            instagramLike24hRepository.save(instagramLike24h);
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
    public Boolean instagram_view(String account_id,String task_key){
        try{
            InstagramViewHistory instagramViewHistory=instagramViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(instagramViewHistory!=null){
                instagramViewHistory.setList_id(instagramViewHistory.getList_id()+task_key.trim()+"|");
                instagramViewHistory.setUpdate_time(System.currentTimeMillis());
                instagramViewHistoryRepository.save(instagramViewHistory);
            }else{
                InstagramViewHistory instagramViewHistory_new=new InstagramViewHistory();
                instagramViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                instagramViewHistory_new.setUpdate_time(System.currentTimeMillis());
                instagramViewHistory_new.setList_id(task_key.trim()+"|");
                instagramViewHistoryRepository.save(instagramViewHistory_new);
            }
            InstagramView24h instagramView24h =new InstagramView24h();
            instagramView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
            instagramView24h.setUpdate_time(System.currentTimeMillis());
            instagramView24hRepository.save(instagramView24h);
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

    public Boolean instagram_comment(String account_id,String task_key,Boolean status){
        try{
            if(status==true){
                InstagramCommentHistory instagramCommentHistory=instagramCommentHistoryRepository.get_By_AccountId(account_id.trim());
                if(instagramCommentHistory!=null){
                    instagramCommentHistory.setList_id(instagramCommentHistory.getList_id()+task_key.trim()+"|");
                    instagramCommentHistory.setUpdate_time(System.currentTimeMillis());
                    instagramCommentHistoryRepository.save(instagramCommentHistory);
                }else{
                    InstagramCommentHistory instagramCommentHistory_new=new InstagramCommentHistory();
                    instagramCommentHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    instagramCommentHistory_new.setUpdate_time(System.currentTimeMillis());
                    instagramCommentHistory_new.setList_id(task_key.trim()+"|");
                    instagramCommentHistoryRepository.save(instagramCommentHistory_new);
                }
                dataCommentRepository.update_Task_Comment_Done(account_id.trim());

                InstagramComment24h instagramComment24h =new InstagramComment24h();
                instagramComment24h.setId(account_id.trim()+task_key.trim());
                instagramComment24h.setUpdate_time(System.currentTimeMillis());
                instagramComment24hRepository.save(instagramComment24h);
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

    public Boolean instagram_delete_task_24h(){
        try{
            instagramComment24hRepository.deleteAllByThan24h();
            instagramView24hRepository.deleteAllByThan24h();
            instagramLike24hRepository.deleteAllByThan24h();
            instagramFollower24hRepository.deleteAllByThan24h();
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
