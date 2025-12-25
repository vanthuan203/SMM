package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountClone;
import com.nts.awspremium.model.AccountSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface AccountCloneRepository extends JpaRepository<AccountClone,Long> {
    @Query(value = "SELECT * FROM account_clone where account_id=?1 limit 1",nativeQuery = true)
    public AccountClone get_Account_Clone_By_Account_id(String account_id);

    @Query(value = "SELECT * FROM account_clone where id=?1 limit 1",nativeQuery = true)
    public AccountClone get_Account_Clone_By_Id(Long id);

    @Query(value = "SELECT id FROM Data.account_clone where video_tiktok!=''",nativeQuery = true)
    public List<Long> get_ListID_By_VideoTiktok();

    @Query(value = "SELECT * FROM Data.account_clone where video_tiktok!='' order by rand() limit 1 ;",nativeQuery = true)
    public AccountClone get_Account_Clone_By_Task_View();


    @Query(value = "SELECT * FROM account_clone where check_video=1 and update_time!=0 and round((UNIX_TIMESTAMP()-update_time/1000)/60)>5 order by update_time asc limit 30",nativeQuery = true)
    public List<AccountClone> get_Account_Clone_By_CheckVideo();


    @Query(value = "SELECT count(*) FROM account_clone where id_clone=?1 limit 1",nativeQuery = true)
    public Integer check_Id_Clone_By_Id_Clone(String id_clone);

    @Modifying
    @Transactional
    @Query(value = "Delete FROM Data.account_clone where account_id in (select account_id from account where live>1 and platform='tiktok');",nativeQuery = true)
    public void delete_Account_Clone_Die();

}
