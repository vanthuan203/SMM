package com.nts.awspremium.repositories;
import com.nts.awspremium.model.GoogleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public  interface GoogleKeyRepository extends JpaRepository<GoogleKey,String> {

    @Query(value = "Select * from google_key where state=1 order by get_count asc limit 1",nativeQuery = true)
    public GoogleKey get_Google_Key();

}
