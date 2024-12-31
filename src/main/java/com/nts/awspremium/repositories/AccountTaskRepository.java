package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountName;
import com.nts.awspremium.model.AccountTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountTaskRepository extends JpaRepository<AccountTask,Long> {

    @Query(value = "Select * from account_task  where account_id=?1 limit 1",nativeQuery = true)
    public AccountTask get_Acount_Task_By_AccountId(String  account_id);
    @Query(value = "Select * from account_name where platform=?1  order by rand()  limit 1",nativeQuery = true)
    public AccountName get_AcountName_By_Platform(String  platform);
}
