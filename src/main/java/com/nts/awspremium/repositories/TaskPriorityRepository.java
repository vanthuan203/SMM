package com.nts.awspremium.repositories;
import com.nts.awspremium.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskPriorityRepository extends JpaRepository<TaskPriority,String> {
    @Query(value = "SELECT * FROM task_priority where priority>0 and state=1",nativeQuery = true)
    public List<TaskPriority> get_Priority_Task();

    @Query(value = "SELECT * FROM task_priority order by platform asc",nativeQuery = true)
    public List<TaskPriority> get_All_Priority_Task();

    @Query(value = "SELECT * FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public TaskPriority get_Priority_Task_By_Task(String task);

    @Query(value = "SELECT priority FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public Integer get_Priority_By_Task(String task);

    @Query(value = "SELECT time_waiting_task FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public Integer get_Wating_Time_By_Task(String task);


    @Query(value = "SELECT * FROM task_priority where priority>0 and platform=?1 and state=1",nativeQuery = true)
    public List<TaskPriority> get_Priority_Task_By_Platform(String platform);

    @Query(value = "SELECT state FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public Integer get_State_Task(String task);
}