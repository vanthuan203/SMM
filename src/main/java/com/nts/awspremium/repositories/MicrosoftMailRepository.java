package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountSave;
import com.nts.awspremium.model.MicrosoftMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MicrosoftMailRepository extends JpaRepository<MicrosoftMail,Long> {

    @Query(value = "SELECT * FROM Data.microsoft_mail where id=1 limit 1;",nativeQuery = true)
    public MicrosoftMail get_Mail_By_AccountId();

    @Query(value = "SELECT * FROM Data.microsoft_mail where email=?1 limit 1;",nativeQuery = true)
    public MicrosoftMail get_Mail_By_Username(String email);

}
