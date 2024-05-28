package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Balance;
import com.nts.awspremium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance,Long> {

}
