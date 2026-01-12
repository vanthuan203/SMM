package com.nts.awspremium.platform.tiktok;

import com.nts.awspremium.TikTokApi;
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
import java.util.OptionalInt;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class TiktokUpdate {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountProfileRepository accountProfileRepository;
    @Autowired
    private TikTokFollower24hRepository tikTokFollower24hRepository;
    @Autowired
    private IpTask24hRepository ipTask24hRepository;
    @Autowired
    private TikTokLike24hRepository tikTokLike24hRepository;
    @Autowired
    private TiktokShare24hRepository tiktokShare24hRepository;
    @Autowired
    private TiktokFavorites24hRepository tiktokFavorites24hRepository;
    @Autowired
    private TikTokView24hRepository tikTokView24hRepository;
    @Autowired
    private TikTokComment24hRepository tikTokComment24hRepository;
    @Autowired
    private TikTokAccountHistoryRepository tikTokAccountHistoryRepository;
    @Autowired
    private TikTokLikeHistoryRepository tikTokLikeHistoryRepository;
    @Autowired
    private TiktokFavoritesHistoryRepository tiktokFavoritesHistoryRepository;
    @Autowired
    private TiktokShareHistoryRepository tiktokShareHistoryRepository;
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
    @Autowired
    private TaskPriorityRepository taskPriorityRepository;
    @Autowired
    private TaskSumRepository taskSumRepository;
    @Autowired
    private ModeOptionRepository modeOptionRepository;
    @Autowired
    private ProfileTaskRepository profileTaskRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    public Boolean tiktok_follower(String account_id,String task_key,Boolean success,Boolean status,String profile_id){
        try{
            if(status){
                if(success){
                    TikTokFollowerHistory tikTokAccountHistory=tikTokAccountHistoryRepository.get_By_AccountId(account_id.trim());
                    if(tikTokAccountHistory!=null){
                        if(tikTokAccountHistory.getFollowing_realtime()==-1){
                            int following= TikTokApi.getFollowingCount(account_id.trim().replace("|tiktok","").split("@")[1]);
                            if(following>=0){
                                tikTokAccountHistory.setFollowing_realtime(following);
                                tikTokAccountHistory.setFollowing_count(following);
                            }
                            tikTokAccountHistory.setList_id(tikTokAccountHistory.getList_id()+task_key.trim()+"|");
                            tikTokAccountHistory.setUpdate_time(System.currentTimeMillis());
                            tikTokAccountHistoryRepository.save(tikTokAccountHistory);
                        }else if((System.currentTimeMillis()-tikTokAccountHistory.getUpdate_time())/1000/60/60>=6){
                            int following= TikTokApi.getFollowingCount(account_id.trim().replace("|tiktok","").split("@")[1]);
                            if(following>=0){
                                tikTokAccountHistory.setFollowing_realtime(following);
                            }
                            tikTokAccountHistory.setList_id(tikTokAccountHistory.getList_id()+task_key.trim()+"|");
                            tikTokAccountHistory.setUpdate_time(System.currentTimeMillis());
                            tikTokAccountHistory.setFollowing_count(tikTokAccountHistory.getFollowing_count()+1);
                            tikTokAccountHistoryRepository.save(tikTokAccountHistory);
                        }else{
                            tikTokAccountHistory.setList_id(tikTokAccountHistory.getList_id()+task_key.trim()+"|");
                            tikTokAccountHistory.setUpdate_time(System.currentTimeMillis());
                            tikTokAccountHistory.setFollowing_count(tikTokAccountHistory.getFollowing_count()+1);
                            tikTokAccountHistoryRepository.save(tikTokAccountHistory);
                        }
                    }else{
                        int following= TikTokApi.getFollowingCount(account_id.trim().replace("|tiktok","").split("@")[1]);
                        if(following>=0){
                            tikTokAccountHistory.setFollowing_realtime(following);
                            tikTokAccountHistory.setFollowing_count(following);
                        }
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

                    IpTask24h ipTask24h =new IpTask24h();
                    ipTask24h.setId(profileTaskRepository.get_Profile_By_ProfileId_JOIN_Device(profile_id.trim()).getDevice().getIp_address()+task_key.trim()+System.currentTimeMillis());
                    ipTask24h.setUpdate_time(System.currentTimeMillis());
                    ipTask24hRepository.save(ipTask24h);

                    try{
                        TaskSum taskSum =new TaskSum();
                        taskSum.setId(account_id.trim()+task_key.trim());
                        taskSum.setUpdate_time(System.currentTimeMillis());
                        taskSum.setTask("follower");
                        taskSum.setPlatform("tiktok");
                        taskSum.setStatus(true);
                        taskSum.setSuccess(true);
                        taskSum.setProfileTask(profileTaskRepository.get_Profile_By_ProfileId(profile_id));
                        taskSumRepository.save(taskSum);
                    }catch (Exception e){

                    }

                }else{
                    try{
                        TaskSum taskSum =new TaskSum();
                        taskSum.setId(account_id.trim()+task_key.trim());
                        taskSum.setUpdate_time(System.currentTimeMillis());
                        taskSum.setTask("follower");
                        taskSum.setPlatform("tiktok");
                        taskSum.setStatus(true);
                        taskSum.setSuccess(false);
                        taskSum.setProfileTask(profileTaskRepository.get_Profile_By_ProfileId(profile_id));
                        taskSumRepository.save(taskSum);
                    }catch (Exception e){

                    }

                }
                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
                ModeOption modeOption=modeOptionRepository.get_Mode_Option_By_ProfileId_And_Platform(profile_id.trim(),"tiktok","follower");
                if(accountTask==null){
                    AccountTask accountTask_New=new AccountTask();
                    accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                    accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                    if(success){
                        accountTask_New.setFollower_time(System.currentTimeMillis());
                        accountTask_New.setTask_success_24h(1);
                    }else{
                    /*
                    accountTask_New.setFollower_time(System.currentTimeMillis()+ 240* 60 * 1000); // lần đầu limit 240m
                    accountTask_New.setTask_success_24h(1); //set fail lần 1
                     */
                        accountTask_New.setFollower_time(System.currentTimeMillis()+modeOption.getTime_waiting_task()* 60 * 1000);
                    }
                    accountTaskRepository.save(accountTask_New);
                }else{
                    if(success){
                        accountTask.setFollower_time(System.currentTimeMillis());
                        accountTask.setTask_success_24h(accountTask.getTask_success_24h()+1);
                    }else{
                    /*
                        accountTask.setFollower_time(System.currentTimeMillis()+(modeOption==null?60:((int)(modeOption.getTime_waiting_task()*(0.2*(accountTask.getTask_success_24h()+1))))) * 60 * 1000);
                        if(accountTask.getTask_success_24h()>=4){
                            accountTask.setTask_success_24h(0);
                        }else{
                            accountTask.setTask_success_24h(accountTask.getTask_success_24h()+1);
                        }
                     */
                        accountTask.setFollower_time(System.currentTimeMillis()+modeOption.getTime_waiting_task()* 60 * 1000);
                    }
                    accountTaskRepository.save(accountTask);
                }
            }else{
                try{
                    TaskSum taskSum =new TaskSum();
                    taskSum.setId(account_id.trim()+task_key.trim());
                    taskSum.setUpdate_time(System.currentTimeMillis());
                    taskSum.setTask("follower");
                    taskSum.setPlatform("tiktok");
                    taskSum.setStatus(false);
                    taskSum.setSuccess(false);
                    taskSum.setProfileTask(profileTaskRepository.get_Profile_By_ProfileId(profile_id));
                    taskSumRepository.save(taskSum);
                }catch (Exception e){

                }
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

    public Boolean tiktok_share(String account_id,String task_key){
        try{

            TiktokShareHistory tiktokShareHistory=tiktokShareHistoryRepository.get_By_AccountId(account_id.trim());
            if(tiktokShareHistory!=null){
                tiktokShareHistory.setList_id(tiktokShareHistory.getList_id()+task_key.trim()+"|");
                tiktokShareHistory.setUpdate_time(System.currentTimeMillis());
                tiktokShareHistoryRepository.save(tiktokShareHistory);
            }else{
                TiktokShareHistory tiktokShareHistory_New=new TiktokShareHistory();
                tiktokShareHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                tiktokShareHistory_New.setUpdate_time(System.currentTimeMillis());
                tiktokShareHistory_New.setList_id(task_key.trim()+"|");
                tiktokShareHistoryRepository.save(tiktokShareHistory_New);
            }
            TiktokShare24h tiktokShare24h =new TiktokShare24h();
            tiktokShare24h.setId(account_id.trim()+task_key.trim());
            tiktokShare24h.setUpdate_time(System.currentTimeMillis());
            tiktokShare24hRepository.save(tiktokShare24h);


            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                accountTask_New.setShare_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask_New);
            }else{
                accountTask.setShare_time(System.currentTimeMillis());
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

    public Boolean tiktok_favorites(String account_id,String task_key){
        try{

            TiktokFavoritesHistory tiktokFavoritesHistory=tiktokFavoritesHistoryRepository.get_By_AccountId(account_id.trim());
            if(tiktokFavoritesHistory!=null){
                tiktokFavoritesHistory.setList_id(tiktokFavoritesHistory.getList_id()+task_key.trim()+"|");
                tiktokFavoritesHistory.setUpdate_time(System.currentTimeMillis());
                tiktokFavoritesHistoryRepository.save(tiktokFavoritesHistory);
            }else{
                TiktokFavoritesHistory tiktokFavoritesHistory_New=new TiktokFavoritesHistory();
                tiktokFavoritesHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                tiktokFavoritesHistory_New.setUpdate_time(System.currentTimeMillis());
                tiktokFavoritesHistory_New.setList_id(task_key.trim()+"|");
                tiktokFavoritesHistoryRepository.save(tiktokFavoritesHistory_New);
            }
            TiktokFavorites24h tiktokFavorites24h =new TiktokFavorites24h();
            tiktokFavorites24h.setId(account_id.trim()+task_key.trim());
            tiktokFavorites24h.setUpdate_time(System.currentTimeMillis());
            tiktokFavorites24hRepository.save(tiktokFavorites24h);


            AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
            if(accountTask==null){
                AccountTask accountTask_New=new AccountTask();
                accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                accountTask_New.setFavorites_time(System.currentTimeMillis());
                accountTaskRepository.save(accountTask_New);
            }else{
                accountTask.setFavorites_time(System.currentTimeMillis());
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

    public Boolean tiktok_view(String account_id,String task_key,String bonus){
        try{
            if(account_id.contains("@")){
                TikTokViewHistory tikTokViewHistory=tikTokViewHistoryRepository.get_By_AccountId(account_id.trim());
                if(tikTokViewHistory!=null){
                    char target = '|';
                    int MAX = 6;
                    String list = tikTokViewHistory.getList_id().trim();
                    String newTask = task_key.trim() + "|";

                    long count = list.chars().filter(ch -> ch == target).count();
                    if (count >= MAX) {
                        int needRemove = (int) (count - (MAX - 1)); // ⭐ mấu chốt

                        int found = 0;
                        int index = -1;

                        for (int i = 0; i < list.length(); i++) {
                            if (list.charAt(i) == target) {
                                found++;
                                if (found == needRemove) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        if (index != -1) {
                            list = list.substring(index + 1);
                        }
                    }

                    tikTokViewHistory.setList_id(list + newTask);
                    tikTokViewHistory.setUpdate_time(System.currentTimeMillis());
                    tikTokViewHistoryRepository.save(tikTokViewHistory);
                }else{
                    if(accountRepository.get_Account_By_Account_id(account_id.trim())!=null){
                        TikTokViewHistory tikTokViewHistory_new=new TikTokViewHistory();
                        tikTokViewHistory_new.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                        tikTokViewHistory_new.setUpdate_time(System.currentTimeMillis());
                        tikTokViewHistory_new.setList_id(task_key.trim()+"|");
                        tikTokViewHistoryRepository.save(tikTokViewHistory_new);
                    }
                }
                TiktokView24h tiktokView24h =new TiktokView24h();
                tiktokView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
                tiktokView24h.setUpdate_time(System.currentTimeMillis());
                tikTokView24hRepository.save(tiktokView24h);


                AccountTask accountTask=accountTaskRepository.get_Acount_Task_By_AccountId(account_id.trim());
                if(accountTask==null){
                    if(accountProfileRepository.get_Account_By_Account_id(account_id.trim())!=null){
                        AccountTask accountTask_New=new AccountTask();
                        accountTask_New.setPlatform(account_id.trim().split("\\|")[1]);
                        accountTask_New.setAccount(accountProfileRepository.get_Account_By_Account_id(account_id.trim()));
                        accountTask_New.setView_time(System.currentTimeMillis());
                        accountTaskRepository.save(accountTask_New);
                    }
                }else{
                    accountTask.setView_time(System.currentTimeMillis());
                    accountTaskRepository.save(accountTask);
                }


                if(bonus.length()>0){
                    if(bonus.equals("like")){
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

                    }else if(bonus.equals("share")){
                        TiktokShareHistory tiktokShareHistory=tiktokShareHistoryRepository.get_By_AccountId(account_id.trim());
                        if(tiktokShareHistory!=null){
                            tiktokShareHistory.setList_id(tiktokShareHistory.getList_id()+task_key.trim()+"|");
                            tiktokShareHistory.setUpdate_time(System.currentTimeMillis());
                            tiktokShareHistoryRepository.save(tiktokShareHistory);
                        }else{
                            TiktokShareHistory tiktokShareHistory_New=new TiktokShareHistory();
                            tiktokShareHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                            tiktokShareHistory_New.setUpdate_time(System.currentTimeMillis());
                            tiktokShareHistory_New.setList_id(task_key.trim()+"|");
                            tiktokShareHistoryRepository.save(tiktokShareHistory_New);
                        }

                        TiktokShare24h tiktokShare24h =new TiktokShare24h();
                        tiktokShare24h.setId(account_id.trim()+task_key.trim());
                        tiktokShare24h.setUpdate_time(System.currentTimeMillis());
                        tiktokShare24hRepository.save(tiktokShare24h);

                    }else if(bonus.equals("favorites")){
                        TiktokFavoritesHistory tiktokFavoritesHistory=tiktokFavoritesHistoryRepository.get_By_AccountId(account_id.trim());
                        if(tiktokFavoritesHistory!=null){
                            tiktokFavoritesHistory.setList_id(tiktokFavoritesHistory.getList_id()+task_key.trim()+"|");
                            tiktokFavoritesHistory.setUpdate_time(System.currentTimeMillis());
                            tiktokFavoritesHistoryRepository.save(tiktokFavoritesHistory);
                        }else{
                            TiktokFavoritesHistory tiktokFavoritesHistory_New=new TiktokFavoritesHistory();
                            tiktokFavoritesHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                            tiktokFavoritesHistory_New.setUpdate_time(System.currentTimeMillis());
                            tiktokFavoritesHistory_New.setList_id(task_key.trim()+"|");
                            tiktokFavoritesHistoryRepository.save(tiktokFavoritesHistory_New);
                        }

                        TiktokFavorites24h tiktokFavorites24h =new TiktokFavorites24h();
                        tiktokFavorites24h.setId(account_id.trim()+task_key.trim());
                        tiktokFavorites24h.setUpdate_time(System.currentTimeMillis());
                        tiktokFavorites24hRepository.save(tiktokFavorites24h);

                    }
                }
            }else{
                TiktokView24h tiktokView24h =new TiktokView24h();
                tiktokView24h.setId(account_id.trim()+task_key.trim()+System.currentTimeMillis());
                tiktokView24h.setUpdate_time(System.currentTimeMillis());
                tikTokView24hRepository.save(tiktokView24h);
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
            tiktokShare24hRepository.deleteAllByThan24h();
            tiktokFavorites24hRepository.deleteAllByThan24h();
            taskSumRepository.deleteAllByThan24h();
            ipTask24hRepository.deleteAllByThan24h();
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
