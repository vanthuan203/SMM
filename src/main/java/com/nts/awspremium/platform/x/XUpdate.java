package com.nts.awspremium.platform.x;

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
public class XUpdate {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private XLike24hRepository xLike24hRepository;
    @Autowired
    private XView24hRepository xView24hRepository;
    @Autowired
    private XComment24hRepository xComment24hRepository;
    @Autowired
    private XRepost24hRepository xRepost24hRepository;
    @Autowired
    private XFollowerHistoryRepository xFollowerHistoryRepository;

    @Autowired
    private XLikeHistoryRepository xLikeHistoryRepository;

    @Autowired
    private XCommentHistoryRepository xCommentHistoryRepository;

    @Autowired
    private XViewHistoryRepository xViewHistoryRepository;

    @Autowired
    private XRepostHistoryRepository xRepostHistoryRepository;

    @Autowired
    private XFollower24hRepository xFollower24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    public Boolean x_follower(String account_id,String task_key){
        try{
            XFollowerHistory xFollowerHistory=xFollowerHistoryRepository.get_By_AccountId(account_id.trim());
            if(xFollowerHistory!=null){
                xFollowerHistory.setList_id(xFollowerHistory.getList_id()+task_key.trim()+"|");
                xFollowerHistory.setUpdate_time(System.currentTimeMillis());
                xFollowerHistoryRepository.save(xFollowerHistory);
            }else{
                XFollowerHistory xFollowerHistory_new=new XFollowerHistory();
                xFollowerHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                xFollowerHistory_new.setUpdate_time(System.currentTimeMillis());
                xFollowerHistory_new.setList_id(task_key.trim()+"|");
                xFollowerHistoryRepository.save(xFollowerHistory_new);
            }
            XFollower24h xFollower24h =new XFollower24h();
            xFollower24h.setId(account_id.trim()+task_key.trim());
            xFollower24h.setUpdate_time(System.currentTimeMillis());
            xFollower24hRepository.save(xFollower24h);
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
    public Boolean x_like(String account_id,String task_key){
        try{
            XLikeHistory xLikeHistory=xLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(xLikeHistory!=null){
                xLikeHistory.setList_id(xLikeHistory.getList_id()+task_key.trim()+"|");
                xLikeHistory.setUpdate_time(System.currentTimeMillis());
                xLikeHistoryRepository.save(xLikeHistory);
            }else{
                XLikeHistory xLikeHistory_new=new XLikeHistory();
                xLikeHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                xLikeHistory_new.setUpdate_time(System.currentTimeMillis());
                xLikeHistory_new.setList_id(task_key.trim()+"|");
                xLikeHistoryRepository.save(xLikeHistory_new);
            }
            XLike24h xLike24h =new XLike24h();
            xLike24h.setId(account_id.trim()+task_key.trim());
            xLike24h.setUpdate_time(System.currentTimeMillis());
            xLike24hRepository.save(xLike24h);
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
    public Boolean x_view(String account_id,String task_key){
        try{
            XViewHistory xViewHistory=xViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(xViewHistory!=null){
                xViewHistory.setList_id(xViewHistory.getList_id()+task_key.trim()+"|");
                xViewHistory.setUpdate_time(System.currentTimeMillis());
                xViewHistoryRepository.save(xViewHistory);
            }else{
                XViewHistory xViewHistory_new=new XViewHistory();
                xViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                xViewHistory_new.setUpdate_time(System.currentTimeMillis());
                xViewHistory_new.setList_id(task_key.trim()+"|");
                xViewHistoryRepository.save(xViewHistory_new);
            }
            XView24h xView24h =new XView24h();
            xView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
            xView24h.setUpdate_time(System.currentTimeMillis());
            xView24hRepository.save(xView24h);
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

    public Boolean x_repost(String account_id,String task_key){
        try{
            XRepostHistory xRepostHistory=xRepostHistoryRepository.get_By_AccountId(account_id.trim());
            if(xRepostHistory!=null){
                xRepostHistory.setList_id(xRepostHistory.getList_id()+task_key.trim()+"|");
                xRepostHistory.setUpdate_time(System.currentTimeMillis());
                xRepostHistoryRepository.save(xRepostHistory);
            }else{
                XRepostHistory xRepostHistory_new=new XRepostHistory();
                xRepostHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                xRepostHistory_new.setUpdate_time(System.currentTimeMillis());
                xRepostHistory_new.setList_id(task_key.trim()+"|");
                xRepostHistoryRepository.save(xRepostHistory_new);
            }
            XRepost24h xRepost24h =new XRepost24h();
            xRepost24h.setId(account_id.trim()+task_key.trim());
            xRepost24h.setUpdate_time(System.currentTimeMillis());
            xRepost24hRepository.save(xRepost24h);
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
    public Boolean x_comment(String account_id,String task_key,Boolean status){
        try{
            if(status==true){
                XCommentHistory xCommentHistory=xCommentHistoryRepository.get_By_AccountId(account_id.trim());
                if(xCommentHistory!=null){
                    xCommentHistory.setList_id(xCommentHistory.getList_id()+task_key.trim()+"|");
                    xCommentHistory.setUpdate_time(System.currentTimeMillis());
                    xCommentHistoryRepository.save(xCommentHistory);
                }else{
                    XCommentHistory xCommentHistory_new=new XCommentHistory();
                    xCommentHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    xCommentHistory_new.setUpdate_time(System.currentTimeMillis());
                    xCommentHistory_new.setList_id(task_key.trim()+"|");
                    xCommentHistoryRepository.save(xCommentHistory_new);
                }
                dataCommentRepository.update_Task_Comment_Done(account_id.trim());

                XComment24h xComment24h =new XComment24h();
                xComment24h.setId(account_id.trim()+task_key.trim());
                xComment24h.setUpdate_time(System.currentTimeMillis());
                xComment24hRepository.save(xComment24h);
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

    public Boolean x_delete_task_24h(){
        try{
            xComment24hRepository.deleteAllByThan24h();
            xView24hRepository.deleteAllByThan24h();
            xLike24hRepository.deleteAllByThan24h();
            xFollower24hRepository.deleteAllByThan24h();
            xRepost24hRepository.deleteAllByThan24h();
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
