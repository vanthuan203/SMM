package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TaskSum;
import com.nts.awspremium.model.TiktokFollower24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TaskSumRepository extends JpaRepository<TaskSum,String> {

}
