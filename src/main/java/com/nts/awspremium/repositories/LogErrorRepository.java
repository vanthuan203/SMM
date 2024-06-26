package com.nts.awspremium.repositories;
import com.nts.awspremium.model.GoogleKey;
import com.nts.awspremium.model.LogError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public  interface LogErrorRepository extends JpaRepository<LogError,String> {

}
