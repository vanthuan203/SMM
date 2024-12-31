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
    private AccountProfileRepository accountProfileRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TikTokView24hRepository tikTokView24hRepository;
    @Autowired
    private TikTokComment24hRepository tikTokComment24hRepository;
    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;
    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;
    @Autowired
    private TikTokCommentHistoryRepository tikTokCommentHistoryRepository;
    @Autowired
    private TikTokViewHistoryRepository tikTokViewHistoryRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;

    public Boolean tiktok_follower(String account_id,String task_key,Boolean success){
        try{
            if(success==null?true:success){
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
            }
            TiktokFollower24h tiktokFollower24h =new TiktokFollower24h();
            tiktokFollower24h.setId(account_id.trim()+task_key.trim());
            tiktokFollower24h.setUpdate_time(System.currentTimeMillis());
            tikTokFollower24hRepository.save(tiktokFollower24h);

            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                if(success==null?true:success){
                    accountTask_New.setFollower_time(System.currentTimeMillis());
                }else{
                    accountTask_New.setFollower_time(System.currentTimeMillis()+60 * 60 * 1000);
                }
                accountTaskRepository.save(accountTask_New);
            }else{
                if(success==null?true:success){
                    accountTask.setFollower_time(System.currentTimeMillis());
                }else{
                    accountTask.setFollower_time(System.currentTimeMillis()+60 * 60 * 1000);
                }
                accountTaskRepository.save(accountTask);
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


            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                accountTask_New.setLike_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask_New);
            }else{
                accountTask.setLike_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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

                TiktokComment24h tiktokComment24h =new TiktokComment24h();
                tiktokComment24h.setId(account_id.trim()+task_key.trim());
                tiktokComment24h.setUpdate_time(System.currentTimeMillis());
                tikTokComment24hRepository.save(tiktokComment24h);
            }else {
                dataCommentRepository.update_Task_Comment_Fail(account_id.trim());
            }

            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                accountTask_New.setComment_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask_New);
            }else{
                accountTask.setComment_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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

    public Boolean tiktok_view(String account_id,String task_key){
        try{
            TikTokViewHistory tikTokViewHistory=tikTokViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(tikTokViewHistory!=null){
                tikTokViewHistory.setList_id(tikTokViewHistory.getList_id()+task_key.trim()+"|");
                tikTokViewHistory.setUpdate_time(System.currentTimeMillis());
                tikTokViewHistoryRepository.save(tikTokViewHistory);
            }else{
                TikTokViewHistory tikTokViewHistory_new=new TikTokViewHistory();
                tikTokViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                tikTokViewHistory_new.setUpdate_time(System.currentTimeMillis());
                tikTokViewHistory_new.setList_id(task_key.trim()+"|");
                tikTokViewHistoryRepository.save(tikTokViewHistory_new);
            }
            TiktokView24h tiktokView24h =new TiktokView24h();
            tiktokView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
            tiktokView24h.setUpdate_time(System.currentTimeMillis());
            tikTokView24hRepository.save(tiktokView24h);


            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                accountTask_New.setView_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask_New);
            }else{
                accountTask.setView_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask);
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

    public Boolean tiktok_delete_task_24h(){
        try{
            tikTokComment24hRepository.deleteAllByThan24h();
            tikTokView24hRepository.deleteAllByThan24h();
            tikTokLike24hRepository.deleteAllByThan24h();
            tikTokFollower24hRepository.deleteAllByThan24h();
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
