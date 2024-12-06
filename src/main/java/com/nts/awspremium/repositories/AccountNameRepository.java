package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountName;
import com.nts.awspremium.model.DataComment;
import com.nts.awspremium.model.DataFollowerTiktok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountNameRepository extends JpaRepository<AccountName,Long> {

    @Query(value = "Select * from account_name  order by id asc limit ?1",nativeQuery = true)
    public List<AccountName> get_Acount_Name(Integer limit);
    @Query(value = "Select * from account_name where platform=?1  order by rand()  limit 1",nativeQuery = true)
    public AccountName get_AcountName_By_Platform(String  platform);
}
