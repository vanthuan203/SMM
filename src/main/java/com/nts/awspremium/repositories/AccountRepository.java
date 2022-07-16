package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Entity;
import java.security.PublicKey;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account,Long> {
    @Query(value = "Select * from account where username=?1",nativeQuery = true)
    public List<Account> findAccountByUsername(String username);
    @Query(value = "SELECT * FROM account where (vps is null or vps='' or vps=' ') and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() desc limit 1",nativeQuery = true)
    public List<Account> getAccount();
    @Query(value = "SELECT * FROM account where vps=?1 and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() desc limit 1",nativeQuery = true)
    public List<Account> getaccountByVps(String vps);

    @Query(value = "SELECT * FROM account where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and username=?1",nativeQuery = true)
    public List<Account> checkEndTrial(String username);
}
