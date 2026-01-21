package com.nts.awspremium.repositories;

import com.nts.awspremium.model.AccountClone;
import com.nts.awspremium.model.Keywords;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordsRepository extends JpaRepository<Keywords,Long> {
    @Query(value = "SELECT * FROM keywords where id=1 limit 1",nativeQuery = true)
    public Keywords getKeywords();
    @Query(value = "SELECT key_list FROM keywords where id=1 limit 1",nativeQuery = true)
    public String getKeyList();
}
