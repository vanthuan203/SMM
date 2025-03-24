package com.nts.awspremium.repositories;

import com.nts.awspremium.model.TaskSum;
import com.nts.awspremium.model.TiktokFollower24h;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface TaskSumRepository extends JpaRepository<TaskSum,String> {
    @Query(value = "SELECT \n" +
            "    DATE(FROM_UNIXTIME(update_time / 1000) + INTERVAL 7 HOUR) AS date, \n" +
            "    COUNT(*) AS total\n" +
            "FROM task_sum\n" +
            "WHERE DATE(FROM_UNIXTIME(update_time / 1000) + INTERVAL 7 HOUR) >= DATE(CONVERT_TZ(NOW(), @@session.time_zone, '+07:00') - INTERVAL 6 DAY)\n" +
            "GROUP BY date\n" +
            "ORDER BY date DESC;",nativeQuery = true)
    public List<String> get_Task_7d();
}
