package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountSaveRepository extends JpaRepository<AccountSave,String> {


    @Query(value = "call update_running_account_save(?1,?2,?3)", nativeQuery = true)
    public AccountSave get_Account_Save(String platform, Long time_check, String code);

}
