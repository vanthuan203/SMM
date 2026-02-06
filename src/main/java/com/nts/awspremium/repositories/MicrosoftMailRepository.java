package com.nts.awspremium.repositories;

import antlr.collections.impl.LList;
import com.nts.awspremium.model.AccountSave;
import com.nts.awspremium.model.MicrosoftMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MicrosoftMailRepository extends JpaRepository<MicrosoftMail,Long> {

    @Query(value = "SELECT * FROM Data.microsoft_mail where id=1 limit 1;",nativeQuery = true)
    public MicrosoftMail get_Mail_By_AccountId();

    @Query(value = "SELECT * FROM Data.microsoft_mail where update_time <= (UNIX_TIMESTAMP() - 5*24*60*60) * 1000",nativeQuery = true)
    public List<MicrosoftMail> get_ALL_Mail_By_AccountId();

    @Query(value = "SELECT * FROM Data.microsoft_mail where email=?1 limit 1;",nativeQuery = true)
    public MicrosoftMail get_Mail_By_Username(String email);

}
