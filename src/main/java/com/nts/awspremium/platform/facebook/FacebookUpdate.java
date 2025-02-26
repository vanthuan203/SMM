package com.nts.awspremium.platform.facebook;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.platform.tiktok.TiktokTask;
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
public class FacebookUpdate {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FacebookLike24hRepository facebookLike24hRepository;
    @Autowired
    private FacebookFollowerHistoryRepository facebookFollowerHistoryRepository;

    @Autowired
    private FacebookLikeHistoryRepository facebookLikeHistoryRepository;

    @Autowired
    private FacebookCommentHistoryRepository facebookCommentHistoryRepository;

    @Autowired
    private FacebookViewHistoryRepository facebookViewHistoryRepository;

    @Autowired
    private FacebookMemberHistoryRepository facebookMemberHistoryRepository;

    @Autowired
    private FacebookFollower24hRepository facebookFollower24hRepository;
    @Autowired
    private FacebookComment24hRepository facebookComment24hRepository;
    @Autowired
    private FacebookView24hRepository facebookView24hRepository;
    @Autowired
    private FacebookMember24hRepository facebookMember24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    public Boolean facebook_follower(String account_id,String task_key){
        try{
            FacebookFollowerHistory facebookFollowerHistory=facebookFollowerHistoryRepository.get_By_AccountId(account_id.trim());
            if(facebookFollowerHistory!=null){
                facebookFollowerHistory.setList_id(facebookFollowerHistory.getList_id()+task_key.trim()+"|");
                facebookFollowerHistory.setUpdate_time(System.currentTimeMillis());
                facebookFollowerHistoryRepository.save(facebookFollowerHistory);
            }else{
                FacebookFollowerHistory facebookFollowerHistory_New=new FacebookFollowerHistory();
                facebookFollowerHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                facebookFollowerHistory_New.setUpdate_time(System.currentTimeMillis());
                facebookFollowerHistory_New.setList_id(task_key.trim()+"|");
                facebookFollowerHistoryRepository.save(facebookFollowerHistory_New);
            }
            FacebookFollower24h facebookFollower24h =new FacebookFollower24h();
            facebookFollower24h.setId(account_id.trim()+task_key.trim());
            facebookFollower24h.setUpdate_time(System.currentTimeMillis());
            facebookFollower24hRepository.save(facebookFollower24h);
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
    public Boolean facebook_like(String account_id,String task_key){
        try{
            FacebookLikeHistory facebookLikeHistory=facebookLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(facebookLikeHistory!=null){
                facebookLikeHistory.setList_id(facebookLikeHistory.getList_id()+task_key.trim()+"|");
                facebookLikeHistory.setUpdate_time(System.currentTimeMillis());
                facebookLikeHistoryRepository.save(facebookLikeHistory);
            }else{
                FacebookLikeHistory facebookLikeHistory_new=new FacebookLikeHistory();
                facebookLikeHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                facebookLikeHistory_new.setUpdate_time(System.currentTimeMillis());
                facebookLikeHistory_new.setList_id(task_key.trim()+"|");
                facebookLikeHistoryRepository.save(facebookLikeHistory_new);
            }
            FacebookLike24h facebookLike24h =new FacebookLike24h();
            facebookLike24h.setId(account_id.trim()+task_key.trim());
            facebookLike24h.setUpdate_time(System.currentTimeMillis());
            facebookLike24hRepository.save(facebookLike24h);
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
    public Boolean facebook_view(String account_id,String task_key){
        try{
            FacebookViewHistory facebookViewHistory=facebookViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(facebookViewHistory!=null){
                facebookViewHistory.setList_id(facebookViewHistory.getList_id()+task_key.trim()+"|");
                facebookViewHistory.setUpdate_time(System.currentTimeMillis());
                facebookViewHistoryRepository.save(facebookViewHistory);
            }else{
                FacebookViewHistory facebookViewHistory_new=new FacebookViewHistory();
                facebookViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                facebookViewHistory_new.setUpdate_time(System.currentTimeMillis());
                facebookViewHistory_new.setList_id(task_key.trim()+"|");
                facebookViewHistoryRepository.save(facebookViewHistory_new);
            }
            FacebookView24h facebookView24h =new FacebookView24h();
            facebookView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
            facebookView24h.setUpdate_time(System.currentTimeMillis());
            facebookView24hRepository.save(facebookView24h);
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

    public Boolean facebook_member(String account_id,String task_key){
        try{
            FacebookMemberHistory facebookMemberHistory=facebookMemberHistoryRepository.get_By_AccountId(account_id.trim());
            if(facebookMemberHistory!=null){
                facebookMemberHistory.setList_id(facebookMemberHistory.getList_id()+task_key.trim()+"|");
                facebookMemberHistory.setUpdate_time(System.currentTimeMillis());
                facebookMemberHistoryRepository.save(facebookMemberHistory);
            }else{
                FacebookMemberHistory facebookMemberHistory_new=new FacebookMemberHistory();
                facebookMemberHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                facebookMemberHistory_new.setUpdate_time(System.currentTimeMillis());
                facebookMemberHistory_new.setList_id(task_key.trim()+"|");
                facebookMemberHistoryRepository.save(facebookMemberHistory_new);
            }
            FacebookMember24h facebookMember24h =new FacebookMember24h();
            facebookMember24h.setId(account_id.trim()+task_key.trim());
            facebookMember24h.setUpdate_time(System.currentTimeMillis());
            facebookMember24hRepository.save(facebookMember24h);
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
    public Boolean facebook_comment(String account_id,String task_key,Boolean status){
        try{
            if(status==true){
                FacebookCommentHistory facebookCommentHistory=facebookCommentHistoryRepository.get_By_AccountId(account_id.trim());
                if(facebookCommentHistory!=null){
                    facebookCommentHistory.setList_id(facebookCommentHistory.getList_id()+task_key.trim()+"|");
                    facebookCommentHistory.setUpdate_time(System.currentTimeMillis());
                    facebookCommentHistoryRepository.save(facebookCommentHistory);
                }else{
                    FacebookCommentHistory facebookCommentHistory_new=new FacebookCommentHistory();
                    facebookCommentHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    facebookCommentHistory_new.setUpdate_time(System.currentTimeMillis());
                    facebookCommentHistory_new.setList_id(task_key.trim()+"|");
                    facebookCommentHistoryRepository.save(facebookCommentHistory_new);
                }
                dataCommentRepository.update_Task_Comment_Done(account_id.trim());

                FacebookComment24h facebookComment24h =new FacebookComment24h();
                facebookComment24h.setId(account_id.trim()+task_key.trim());
                facebookComment24h.setUpdate_time(System.currentTimeMillis());
                facebookComment24hRepository.save(facebookComment24h);

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
    public Boolean facebook_delete_task_24h(){
        try{
            facebookComment24hRepository.deleteAllByThan24h();
            facebookView24hRepository.deleteAllByThan24h();
            facebookLike24hRepository.deleteAllByThan24h();
            facebookFollower24hRepository.deleteAllByThan24h();
            facebookMember24hRepository.deleteAllByThan24h();
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
