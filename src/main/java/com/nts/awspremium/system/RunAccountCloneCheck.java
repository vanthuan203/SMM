package com.nts.awspremium.system;

import com.nts.awspremium.model_system.AccountCloneVideoCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.model_system.OrderThreadFollowerCheck;
import com.nts.awspremium.model_system.OrderThreadSpeedUpCheck;
import com.nts.awspremium.repositories.AccountCloneRepository;
import com.nts.awspremium.repositories.OrderRunningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Component
public class RunAccountCloneCheck {
    @Autowired
    private AccountCloneRepository accountCloneRepository;
    @Autowired
    private AccountCloneVideoCheck accountCloneVideoCheck;
    @PostConstruct
    public void init() throws InterruptedException {
        try{
                new Thread(() -> {
                    Random rand =new Random();
                    while (true) {
                        try {
                            accountCloneVideoCheck.setValue(accountCloneRepository.get_ListID_By_VideoTiktok());
                            try {
                                Thread.sleep(rand.nextInt(1000));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            accountCloneRepository.delete_Account_Clone_Die();
                            try {
                                Thread.sleep(rand.nextInt(1000*300));
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




