package com.nts.awspremium.system;

import com.nts.awspremium.controller.DataConmentController;
import com.nts.awspremium.controller.TaskController;
import com.nts.awspremium.platform.Instagram.InstagramUpdate;
import com.nts.awspremium.platform.facebook.FacebookUpdate;
import com.nts.awspremium.platform.threads.ThreadsUpdate;
import com.nts.awspremium.platform.tiktok.TiktokUpdate;
import com.nts.awspremium.platform.x.XUpdate;
import com.nts.awspremium.platform.youtube.YoutubeUpdate;
import com.nts.awspremium.repositories.AccountRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunAccountCheck {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OrderRunningRepository orderRunningRepository;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
            if(Integer.parseInt(env.getProperty("server.port"))==8000){
                new Thread(() -> {
                    //Random rand =new Random();
                    while (true) {
                        try {
                            try {
                                Thread.sleep(30000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            accountRepository.reset_Account_Error();
                            //orderRunningRepository.reset_Check_Count();
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




