package com.nts.awspremium.repositories;

import com.nts.awspremium.model.Cookie;
import com.nts.awspremium.model.PriorityTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface PriorityTasksRepository extends JpaRepository<PriorityTasks,String> {
    @Query(value = "SELECT * FROM AccPremium.priority_task where state=1;",nativeQuery = true)
    public List<PriorityTasks> getPriority_task();
}