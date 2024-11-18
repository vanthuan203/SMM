package com.nts.awspremium.platform.threads;

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
public class ThreadsUpdate {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ThreadsLike24hRepository threadsLike24hRepository;
    @Autowired
    private ThreadsComment24hRepository threadsComment24hRepository;
    @Autowired
    private ThreadsView24hRepository threadsView24hRepository;
    @Autowired
    private ThreadsRepost24hRepository threadsRepost24hRepository;
    @Autowired
    private ThreadsFollowerHistoryRepository threadsFollowerHistoryRepository;

    @Autowired
    private ThreadsLikeHistoryRepository threadsLikeHistoryRepository;

    @Autowired
    private ThreadsCommentHistoryRepository threadsCommentHistoryRepository;

    @Autowired
    private ThreadsViewHistoryRepository threadsViewHistoryRepository;

    @Autowired
    private ThreadsRepostHistoryRepository threadsRepostHistoryRepository;

    @Autowired
    private ThreadsFollower24hRepository threadsFollower24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    @Autowired
    private DataCommentRepository dataCommentRepository;

    public Boolean threads_follower(String account_id,String task_key){
        try{
            ThreadsFollowerHistory threadsFollowerHistory=threadsFollowerHistoryRepository.get_By_AccountId(account_id.trim());
            if(threadsFollowerHistory!=null){
                threadsFollowerHistory.setList_id(threadsFollowerHistory.getList_id()+task_key.trim()+"|");
                threadsFollowerHistory.setUpdate_time(System.currentTimeMillis());
                threadsFollowerHistoryRepository.save(threadsFollowerHistory);
            }else{
                ThreadsFollowerHistory threadsFollowerHistory_new=new ThreadsFollowerHistory();
                threadsFollowerHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                threadsFollowerHistory_new.setUpdate_time(System.currentTimeMillis());
                threadsFollowerHistory_new.setList_id(task_key.trim()+"|");
                threadsFollowerHistoryRepository.save(threadsFollowerHistory_new);
            }
            ThreadsFollower24h threadsFollower24h =new ThreadsFollower24h();
            threadsFollower24h.setId(account_id.trim()+task_key.trim());
            threadsFollower24h.setUpdate_time(System.currentTimeMillis());
            threadsFollower24hRepository.save(threadsFollower24h);
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
    public Boolean threads_like(String account_id,String task_key){
        try{
            ThreadsLikeHistory threadsLikeHistory=threadsLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(threadsLikeHistory!=null){
                threadsLikeHistory.setList_id(threadsLikeHistory.getList_id()+task_key.trim()+"|");
                threadsLikeHistory.setUpdate_time(System.currentTimeMillis());
                threadsLikeHistoryRepository.save(threadsLikeHistory);
            }else{
                ThreadsLikeHistory threadsLikeHistory_new=new ThreadsLikeHistory();
                threadsLikeHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                threadsLikeHistory_new.setUpdate_time(System.currentTimeMillis());
                threadsLikeHistory_new.setList_id(task_key.trim()+"|");
                threadsLikeHistoryRepository.save(threadsLikeHistory_new);
            }
            ThreadsLike24h threadsLike24h =new ThreadsLike24h();
            threadsLike24h.setId(account_id.trim()+task_key.trim());
            threadsLike24h.setUpdate_time(System.currentTimeMillis());
            threadsLike24hRepository.save(threadsLike24h);
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
    public Boolean threads_view(String account_id,String task_key){
        try{
            ThreadsViewHistory threadsViewHistory=threadsViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(threadsViewHistory!=null){
                threadsViewHistory.setList_id(threadsViewHistory.getList_id()+task_key.trim()+"|");
                threadsViewHistory.setUpdate_time(System.currentTimeMillis());
                threadsViewHistoryRepository.save(threadsViewHistory);
            }else{
                ThreadsViewHistory threadsViewHistory_new=new ThreadsViewHistory();
                threadsViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                threadsViewHistory_new.setUpdate_time(System.currentTimeMillis());
                threadsViewHistory_new.setList_id(task_key.trim()+"|");
                threadsViewHistoryRepository.save(threadsViewHistory_new);
            }
            ThreadsView24h threadsView24h =new ThreadsView24h();
            threadsView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
            threadsView24h.setUpdate_time(System.currentTimeMillis());
            threadsView24hRepository.save(threadsView24h);
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

    public Boolean threads_repost(String account_id,String task_key){
        try{
            ThreadsRepostHistory threadsRepostHistory=threadsRepostHistoryRepository.get_By_AccountId(account_id.trim());
            if(threadsRepostHistory!=null){
                threadsRepostHistory.setList_id(threadsRepostHistory.getList_id()+task_key.trim()+"|");
                threadsRepostHistory.setUpdate_time(System.currentTimeMillis());
                threadsRepostHistoryRepository.save(threadsRepostHistory);
            }else{
                ThreadsRepostHistory threadsRepostHistory_new=new ThreadsRepostHistory();
                threadsRepostHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                threadsRepostHistory_new.setUpdate_time(System.currentTimeMillis());
                threadsRepostHistory_new.setList_id(task_key.trim()+"|");
                threadsRepostHistoryRepository.save(threadsRepostHistory_new);
            }
            ThreadsRepost24h threadsRepost24h =new ThreadsRepost24h();
            threadsRepost24h.setId(account_id.trim()+task_key.trim());
            threadsRepost24h.setUpdate_time(System.currentTimeMillis());
            threadsRepost24hRepository.save(threadsRepost24h);
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
    public Boolean threads_comment(String account_id,String task_key,Boolean status){
        try{
            if(status==true){
                ThreadsCommentHistory threadsCommentHistory=threadsCommentHistoryRepository.get_By_AccountId(account_id.trim());
                if(threadsCommentHistory!=null){
                    threadsCommentHistory.setList_id(threadsCommentHistory.getList_id()+task_key.trim()+"|");
                    threadsCommentHistory.setUpdate_time(System.currentTimeMillis());
                    threadsCommentHistoryRepository.save(threadsCommentHistory);
                }else{
                    ThreadsCommentHistory threadsCommentHistory_new=new ThreadsCommentHistory();
                    threadsCommentHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    threadsCommentHistory_new.setUpdate_time(System.currentTimeMillis());
                    threadsCommentHistory_new.setList_id(task_key.trim()+"|");
                    threadsCommentHistoryRepository.save(threadsCommentHistory_new);
                }
                dataCommentRepository.update_Task_Comment_Done(account_id.trim());

                ThreadsComment24h threadsComment24h =new ThreadsComment24h();
                threadsComment24h.setId(account_id.trim()+task_key.trim());
                threadsComment24h.setUpdate_time(System.currentTimeMillis());
                threadsComment24hRepository.save(threadsComment24h);

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

    public Boolean threads_delete_task_24h(){
        try{
            threadsComment24hRepository.deleteAllByThan24h();
            threadsView24hRepository.deleteAllByThan24h();
            threadsLike24hRepository.deleteAllByThan24h();
            threadsFollower24hRepository.deleteAllByThan24h();
            threadsRepost24hRepository.deleteAllByThan24h();
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
