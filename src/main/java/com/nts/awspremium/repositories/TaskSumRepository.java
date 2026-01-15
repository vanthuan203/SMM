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

    @Query(value = "SELECT \n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\",status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id in(select profile_id from profile_task where device_id=?1) \n" +
            "    GROUP BY status\n" +
            ") AS grouped_data;",nativeQuery = true)
    public String task_Sum_By_DeviceId(String device_id);

    @Query(value = "SELECT \n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\",status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id in(select profile_id from profile_task where device_id=?1) \n" +
            " and update_time >= (UNIX_TIMESTAMP() - 24*3600) * 1000\n"+
            "    GROUP BY status\n" +
            ") AS grouped_data;",nativeQuery = true)
    public String task_Sum_By_DeviceId_24H(String device_id);

    @Query(value = "SELECT  \n" +
            "  profile_id,\n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\", status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        profile_id,\n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id IN (\n" +
            "        SELECT profile_id \n" +
            "        FROM profile_task \n" +
            "        WHERE device_id = ?1\n" +
            "    )\n" +
            "    GROUP BY profile_id, status\n" +
            ") AS grouped_data\n" +
            "GROUP BY profile_id;",nativeQuery = true)
    public List<Object[]> task_Sum_By_DeviceId_GroupBy_ProfileId(String device_id);

    @Query(value = "SELECT  \n" +
            "  profile_id,\n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\", status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        profile_id,\n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id IN (\n" +
            "        SELECT profile_id \n" +
            "        FROM profile_task \n" +
            "        WHERE device_id = ?1\n" +
            "    )\n" +
            "and update_time >= (UNIX_TIMESTAMP() - 24*3600) * 1000\n"+
            "    GROUP BY profile_id, status\n" +
            ") AS grouped_data\n" +
            "GROUP BY profile_id;",nativeQuery = true)
    public List<Object[]> task_Sum_By_DeviceId_GroupBy_ProfileId_24H(String device_id);

    @Query(value = "SELECT \n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\",status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id=?1\n" +
            "    GROUP BY status\n" +
            ") AS grouped_data;",nativeQuery = true)
    public String task_Sum_By_ProfileId(String profile_id);

    @Query(value = "SELECT \n" +
            "  GROUP_CONCAT(\n" +
            "    CONCAT(\n" +
            "      \"?\",status, '=>true:', success_true,\n" +
            "      ',false:', success_false\n" +
            "    ) \n" +
            "    ORDER BY status DESC\n" +
            "    SEPARATOR ''\n" +
            "  ) AS summary\n" +
            "FROM (\n" +
            "    SELECT \n" +
            "        status,\n" +
            "        SUM(success = true) AS success_true,\n" +
            "        SUM(success = false) AS success_false\n" +
            "    FROM task_sum\n" +
            "    WHERE profile_id=?1 and update_time >= (UNIX_TIMESTAMP() - 24*3600) * 1000 \n" +
            "    GROUP BY status\n" +
            ") AS grouped_data;",nativeQuery = true)
    public String task_Sum_By_ProfileId_24H(String profile_id);




    @Query(value ="SELECT COALESCE(MIN(update_time), 0) FROM Data.task_sum where profile_id=?1 and update_time >= (UNIX_TIMESTAMP() - 24*3600) * 1000",nativeQuery = true)
    public Long Min_Time_By_ProfileId_24H(String profile_id);

    @Modifying
    @Transactional
    @Query(value = "delete from task_sum where update_time < (UNIX_TIMESTAMP() - 15*86400) * 1000;",nativeQuery = true)
    public Integer deleteAllByThan24h();
}
