package com.nts.awspremium.system;

import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.OrderRunningRepository;
import com.nts.awspremium.repositories.SettingSystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class RunMySQLCheck {
    @Autowired
    private SettingSystemRepository settingSystemRepository;
    @Autowired
    private MySQLCheck mySQLCheck;
    @Autowired
    private Environment env;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
                new Thread(() -> {
                    Random rand =new Random();
                    while (true) {
                        try {
                            mySQLCheck.setValue(settingSystemRepository.check_MySQL());
                            try {
                                Thread.sleep(1000+rand.nextInt(250));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }).start();
        }catch (Exception e){

        }



    }
}




