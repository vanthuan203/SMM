package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.Entity;
import javax.transaction.Transactional;
import java.security.PublicKey;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account,Long> {
    @Query(value = "Select * from account where username=?1",nativeQuery = true)
    public List<Account> findAccountByUsername(String username);

    @Query(value = "Select * from account where id=?1",nativeQuery = true)
    public List<Account> findAccountById(Long id);

    @Query(value = "Select * from account where username=?1",nativeQuery = true)
    public List<Account> getInfo(String username);
    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccount();
    @Query(value = "SELECT id  FROM account where (vps is null or vps='' or vps=' ') and running=0 and live=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand()  limit 1",nativeQuery = true)
    public Long getAccountbuffh();
    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=1 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVps(String vps);
    @Query(value = "SELECT id FROM account where vps=?1 and running=0 and live=0 and round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 order by rand() limit 1",nativeQuery = true)
    public Long getaccountByVpsbuffh(String vps);
    @Query(value = "SELECT * FROM account where round((endtrial/1000-UNIX_TIMESTAMP())/60/60/24) >=1 and username=?1",nativeQuery = true)
    public List<Account> checkEndTrial(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET running=0,vps='' where vps=?1",nativeQuery = true)
    public Integer resetAccountByVps(String vps);
}
