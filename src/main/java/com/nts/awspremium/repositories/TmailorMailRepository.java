package com.nts.awspremium.repositories;

import com.nts.awspremium.model.MicrosoftMail;
import com.nts.awspremium.model.TmailorMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TmailorMailRepository extends JpaRepository<TmailorMail,Long> {
    @Query(value = "SELECT * FROM Data.tmailor_mail where email=?1 limit 1;",nativeQuery = true)
    public TmailorMail get_Mail_By_Username(String email);

}
