package com.nts.awspremium.controller;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.OrderSpeedTrue;
import com.nts.awspremium.model.OrderTrue;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.IpV4Repository;
import com.nts.awspremium.repositories.VideoViewRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class AutoRunCheckProxy {
    @Autowired
    private ProxyController proxyController;
    @Autowired
    private IpV4Repository ipV4Repository;
    @Autowired
    private OrderSpeedTrue orderSpeedTrue;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            int num_Cron= Integer.parseInt(env.getProperty("server.port"))-8000;
            for(int i=1;i<=(num_Cron==0?11:10);i++){
                int finalI = i+(num_Cron==0?0:(num_Cron*10));
                //System.out.println(finalI);
                if(num_Cron==0&&i==11){
                    new Thread(() -> {
                        Long id=0L;
                        while(true) {
                            try{
                                Balance balance=balanceRepository.getBalanceByMaxId();
                                if(0!=Long.compare(id,balance.getId())){
                                    id=balance.getId();
                                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

                                    Request request = null;

                                    request = new Request.Builder().url("https://maker.ifttt.com/trigger/pending/with/key/eh3Ut1_iinzl4yCeH5-BC2d21WpaAKdzXTWzVfXurdc?value1=" + balance.getUser().replace("@gmail.com","")+"&value2="+balance.getService()+"&value3="+balance.getBalance()).get().build();

                                    Response response = client.newCall(request).execute();
                                }
                                Thread.sleep(2500);

                            }catch (Exception e){
                                continue;
                            }
                        }
                    }).start();
                }else{
                    new Thread(() -> {
                        Random rand =new Random();
                        while(true) {
                            try{
                                proxyController.checkproxyMain(finalI);
                            }catch (Exception e){
                                continue;
                            }
                        /*
                        try {
                            Thread.sleep(300000+rand.nextInt(50000));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                         */
                        }
                    }).start();
                }

            }
        }catch (Exception e){

        }



    }
}




