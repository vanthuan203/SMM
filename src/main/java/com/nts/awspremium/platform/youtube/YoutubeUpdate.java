package com.nts.awspremium.platform.youtube;

import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.platform.tiktok.TiktokTask;
import com.nts.awspremium.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class YoutubeUpdate {
    @Autowired
    private YoutubeViewHistoryRepository youtubeViewHistoryRepository;
    @Autowired
    private YoutubeSubscriberHistoryRepository youtubeChannelHistoryRepository;
    @Autowired
    private YoutubeLikeHistoryRepository youtubeLikeHistoryRepository;
    @Autowired
    private DataSubscriberRepository dataSubscriberRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private YoutubeLike24hRepository youtubeLike24hRepository;
    @Autowired
    private YoutubeView24hRepository youtubeView24hRepository;
    @Autowired
    private YoutubeSubscriber24hRepository youtubeSubscribe24hRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;
    public Boolean youtube_view(String account_id,String task_key){
        try{
            if(orderRunningRepository.get_ServiceId_By_TaskKey(task_key.trim())==33){
                return true;
            }
            YoutubeViewHistory youtubeVideoHistory=youtubeViewHistoryRepository.get_By_AccountId(account_id.trim());
            if(youtubeVideoHistory!=null){
                char target = '|';
                long count = youtubeVideoHistory.getList_id().trim().chars().filter(ch -> ch == target).count();
                if(count>=10){
                    int occurrence = (int)count-4;  // Lần xuất hiện thứ n cần tìm
                    OptionalInt position = IntStream.range(0, youtubeVideoHistory.getList_id().trim().length())
                            .filter(i -> youtubeVideoHistory.getList_id().trim().charAt(i) == target)
                            .skip(occurrence - 1)
                            .findFirst();
                    youtubeVideoHistory.setList_id(youtubeVideoHistory.getList_id().trim().substring(position.getAsInt()+1)+task_key.trim()+"|");
                }else{
                    youtubeVideoHistory.setList_id(youtubeVideoHistory.getList_id()+task_key.trim()+"|");
                }
                youtubeVideoHistory.setUpdate_time(System.currentTimeMillis());
                youtubeViewHistoryRepository.save(youtubeVideoHistory);
            }else{
                YoutubeViewHistory youtubeVideoHistory_New=new YoutubeViewHistory();
                youtubeVideoHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                youtubeVideoHistory_New.setUpdate_time(System.currentTimeMillis());
                youtubeVideoHistory_New.setList_id(task_key.trim()+"|");
                youtubeViewHistoryRepository.save(youtubeVideoHistory_New);
            }
            YoutubeView24h youtubeView24h =new YoutubeView24h();
            youtubeView24h.setId(account_id.trim()+task_key.trim());
            youtubeView24h.setUpdate_time(System.currentTimeMillis());
            youtubeView24hRepository.save(youtubeView24h);
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

    public Boolean youtube_subscriber(String account_id,String task_key){
        try{
            String order_Key= dataSubscriberRepository.get_ChannelId_By_VideoId(task_key.trim());
            if(order_Key!=null){
                YoutubeSubscriberHistory youtubeChannelHistory=youtubeChannelHistoryRepository.get_By_AccountId(account_id.trim());
                if(youtubeChannelHistory!=null){
                    youtubeChannelHistory.setList_id(youtubeChannelHistory.getList_id()+order_Key.trim()+"|");
                    youtubeChannelHistory.setUpdate_time(System.currentTimeMillis());
                    youtubeChannelHistoryRepository.save(youtubeChannelHistory);
                }else{
                    YoutubeSubscriberHistory youtubeChannelHistory_New=new YoutubeSubscriberHistory();
                    youtubeChannelHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                    youtubeChannelHistory_New.setUpdate_time(System.currentTimeMillis());
                    youtubeChannelHistory_New.setList_id(order_Key.trim()+"|");
                    youtubeChannelHistoryRepository.save(youtubeChannelHistory_New);
                }
                YoutubeSubscriber24h youtubeSubscribe24h =new YoutubeSubscriber24h();
                youtubeSubscribe24h.setId(account_id.trim()+order_Key.trim());
                youtubeSubscribe24h.setUpdate_time(System.currentTimeMillis());
                youtubeSubscribe24hRepository.save(youtubeSubscribe24h);
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
    public Boolean youtube_like(String account_id,String task_key){
        try{
            YoutubeLikeHistory youtubeLikeHistory=youtubeLikeHistoryRepository.get_By_AccountId(account_id.trim());
            if(youtubeLikeHistory!=null){
                youtubeLikeHistory.setList_id(youtubeLikeHistory.getList_id()+task_key.trim()+"|");
                youtubeLikeHistory.setUpdate_time(System.currentTimeMillis());
                youtubeLikeHistoryRepository.save(youtubeLikeHistory);
            }else{
                YoutubeLikeHistory youtubeLikeHistory_New=new YoutubeLikeHistory();
                youtubeLikeHistory_New.setAccount(accountRepository.get_Account_By_Account_id(account_id.trim()));
                youtubeLikeHistory_New.setUpdate_time(System.currentTimeMillis());
                youtubeLikeHistory_New.setList_id(task_key.trim()+"|");
                youtubeLikeHistoryRepository.save(youtubeLikeHistory_New);
            }
            YoutubeLike24h youtubeLike24h =new YoutubeLike24h();
            youtubeLike24h.setId(account_id.trim()+task_key.trim());
            youtubeLike24h.setUpdate_time(System.currentTimeMillis());
            youtubeLike24hRepository.save(youtubeLike24h);
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

    public Boolean youtube_delete_task_24h(){
        try{
            youtubeView24hRepository.deleteAllByThan24h();
            youtubeLike24hRepository.deleteAllByThan24h();
            youtubeSubscribe24hRepository.deleteAllByThan24h();
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
