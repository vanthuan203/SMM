package com.nts.awspremium.system;

import com.nts.awspremium.Openai;
import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.Keywords;
import com.nts.awspremium.repositories.BalanceRepository;
import com.nts.awspremium.repositories.KeywordsRepository;
import com.nts.awspremium.repositories.OpenAiKeyRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class RunUpdateKeywords {
    @Autowired
    private KeywordsRepository keywordsRepository;
    @Autowired
    private OpenAiKeyRepository openAiKeyRepository;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    while (true) {
                        try {
                            String listKey= Openai.ListKeywords(openAiKeyRepository.get_OpenAI_Key());
                            if(listKey!=null){
                                Keywords keywords=keywordsRepository.getKeywords();
                                keywords.setKey_list(listKey);
                                keywords.setUpdate_time(System.currentTimeMillis());
                                keywordsRepository.save(keywords);
                            }
                            Thread.sleep(60*60*6*1000);
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




