package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountChange;
import com.nts.awspremium.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountChangeRepository extends JpaRepository<AccountChange,Long> {
    @Query(value = "SELECT * FROM AccPremium.accountchange where running=0 and geo='vn' order by priority desc,id asc limit 1",nativeQuery = true)
    public List<AccountChange> getGeoChangerVN( );
    @Query(value = "SELECT * FROM AccPremium.accountchange where running=0 and geo='us' order by priority desc,id asc limit 1",nativeQuery = true)
    public List<AccountChange> getGeoChangerUS( );
}
