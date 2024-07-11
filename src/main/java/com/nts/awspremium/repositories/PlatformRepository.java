package com.nts.awspremium.repositories;
import com.nts.awspremium.model.Platform;
import com.nts.awspremium.model.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform,String> {
    @Query(value = "SELECT GROUP_CONCAT(platform SEPARATOR ',') AS concatenated_rows from platform  where priority>0 and state=1 order by rand()",nativeQuery = true)
    public String get_All_Platform();

    @Query(value = "SELECT platform from platform  where priority>0 and state=1 order by rand()",nativeQuery = true)
    public List<String> get_All_Platform_True();
    @Query(value = "SELECT priority FROM platform where platform=?1 limit 1",nativeQuery = true)
    public Integer get_Priority_By_Platform(String platform);

    @Query(value = "SELECT * FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public TaskPriority get_Priority_Task_By_Task(String task);


    @Query(value = "SELECT * FROM task_priority where priority>0 and platform=?1 and state=1",nativeQuery = true)
    public List<TaskPriority> get_Priority_Task_By_Platform(String platform);

    @Query(value = "SELECT state FROM task_priority where task=?1 limit 1",nativeQuery = true)
    public Integer get_State_Task(String task);
}