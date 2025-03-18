package com.nts.awspremium.system;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.repositories.BalanceRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class RunIFTTT {
    @Autowired
    private BalanceRepository balanceRepository;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    Long id=0L;
                    String value1="";
                    String value2="";
                    String value3="";
                    //Random rand =new Random();
                    while (true) {
                        try {
                            Balance balance=balanceRepository.getBalanceByMaxId();
                            if(0!=Long.compare(id,balance.getId())){
                                id=balance.getId();
                                value1="\uD83D\uDCF2 "+balance.getUser().replace("@gmail.com","")+" ▶\uFE0F "+balance.getService()+" \uD83D\uDD04 "+balance.getBalance()+"\uD83D\uDCB2";
                                value3=balance.getTotal_blance().toString()+"\uD83D\uDCB2";
                                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
                                Request request = null;

                                request = new Request.Builder().url("https://maker.ifttt.com/trigger/pending/with/key/eh3Ut1_iinzl4yCeH5-BC2d21WpaAKdzXTWzVfXurdc?value1="+value1+"&value2="+value2+"&value3="+value3).get().build();

                                Response response = client.newCall(request).execute();
                            }
                            Thread.sleep(1500);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }).start();
            }

        }catch (Exception e){

        }



    }
}




