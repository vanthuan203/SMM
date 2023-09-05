package com.nts.awspremium.repositories;

import com.nts.awspremium.model.LimitService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LimitServiceRepository extends JpaRepository<LimitService,String> {
    @Query(value = "SELECT maxorder from limitservice where user=?1 and service=?2 limit 1",nativeQuery = true)
    public Integer getLimitByServiceAndUser(String user,Integer service);

}