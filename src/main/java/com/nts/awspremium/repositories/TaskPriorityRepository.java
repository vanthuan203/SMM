package com.nts.awspremium.repositories;
import com.nts.awspremium.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskPriorityRepository extends JpaRepository<TaskPriority,String> {
    @Query(value = "SELECT * FROM task_priority where priority>0;",nativeQuery = true)
    public List<TaskPriority> getPriority_task();

    @Query(value = "SELECT state FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public Integer getState_Task(String task);
}