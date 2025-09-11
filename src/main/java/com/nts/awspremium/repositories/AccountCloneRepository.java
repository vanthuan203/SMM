package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Account;
import com.nts.awspremium.model.AccountClone;
import com.nts.awspremium.model.AccountSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountCloneRepository extends JpaRepository<AccountClone,Long> {
    @Query(value = "SELECT * FROM account_clone where account_id=?1 limit 1",nativeQuery = true)
    public AccountClone get_Account_Clone_By_Account_id(String account_id);

    @Query(value = "SELECT count(*) FROM account_clone where id_clone=?1 limit 1",nativeQuery = true)
    public Integer check_Id_Clone_By_Id_Clone(String id_clone);

}
