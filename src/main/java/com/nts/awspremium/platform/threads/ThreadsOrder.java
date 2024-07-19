package com.nts.awspremium.platform.threads;

import com.nts.awspremium.model.*;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.LogErrorRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import com.nts.awspremium.ThreadsApi;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
public class ThreadsOrder {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private LogErrorRepository logErrorRepository;

    public JSONObject threads_like(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id= ThreadsApi.getPostId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject threads_view(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id=ThreadsApi.getPostId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject threads_repost(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id=ThreadsApi.getPostId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject threads_quote(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id=ThreadsApi.getPostId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject threads_follower(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id=ThreadsApi.getThreadsId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }

            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(System.currentTimeMillis());
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }
    public JSONObject threads_comment(DataRequest data,Service service,User user)  throws IOException, ParseException{
        JSONObject resp = new JSONObject();
        try{
            String x_Id=ThreadsApi.getPostId(data.getLink());
            if(x_Id==null){
                resp.put("error", "Invalid link");
                return resp;
            }
            if (orderRunningRepository.get_Order_By_Order_Key_And_Task(x_Id.trim(),service.getTask()) > 0) {
                resp.put("error", "This ID in process");
                return resp;
            }
            float priceorder = 0;
            priceorder = (data.getQuantity() / 1000F) * service.getService_rate() * ((float) (user.getRate()) / 100) * ((float) (100 - user.getDiscount()) / 100);
            if (priceorder > (float) user.getBalance()) {
                resp.put("error", "Your balance not enough");
                return resp;
            }
            OrderRunning orderRunning = new OrderRunning();
            orderRunning.setInsert_time(System.currentTimeMillis());
            orderRunning.setQuantity(data.getQuantity());
            orderRunning.setOrder_link(data.getLink());
            orderRunning.setComment_list(data.getList());
            orderRunning.setStart_count(0);
            orderRunning.setTotal(0);
            orderRunning.setOrder_key(x_Id);
            orderRunning.setUser(user);
            orderRunning.setUpdate_time(0L);
            orderRunning.setUpdate_current_time(0L);
            orderRunning.setStart_time(0L);
            orderRunning.setThread(service.getThread());
            orderRunning.setThread_set(service.getThread());
            orderRunning.setNote(data.getNote()==null?"":data.getNote());
            orderRunning.setCharge(priceorder);
            orderRunning.setService(service);
            orderRunning.setValid(1);
            orderRunning.setSpeed_up(0);
            orderRunning.setCurrent_count(0);
            orderRunning.setOrder_refill(data.getOrder_refill());
            orderRunning.setPriority(0);
            orderRunningRepository.save(orderRunning);

            Float balance_update=balanceRepository.update_Balance(-priceorder,user.getUsername().trim());
            Balance balance = new Balance();
            balance.setUser(user.getUsername().trim());
            balance.setAdd_time(System.currentTimeMillis());
            balance.setTotal_blance(balance_update);
            balance.setBalance(-priceorder);
            balance.setService(data.getService());
            balance.setNote("Order " + data.getQuantity()+" " +service.getTask()+ " for Id "+ orderRunning.getOrder_id());
            balanceRepository.save(balance);
            resp.put("order", orderRunning.getOrder_id());
            return resp;

        }catch (Exception e) {
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

            resp.put("error", "Cant insert link");
            return resp;
        }
    }

}
