package com.nts.awspremium.system;

import com.nts.awspremium.controller.AccountController;
import com.nts.awspremium.repositories.AccountRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RunAccountClone {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountController accountController;
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
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            accountController.updateAccountClone();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            accountController.updateAccountTiktok();
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




