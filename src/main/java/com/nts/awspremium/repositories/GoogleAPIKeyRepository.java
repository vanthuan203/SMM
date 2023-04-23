package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.GoogleAPIKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface GoogleAPIKeyRepository extends JpaRepository<GoogleAPIKey,Long> {
    @Query(value = "Select * from googleapikey where state=1 order by count_get asc limit 1",nativeQuery = true)
    public List<GoogleAPIKey> getAllByState();
}
