package com.example.city.Async;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.city.entity.Appointment;
import com.example.city.entity.EnterpriseStatus;
import com.example.city.mapper.AppointmentMapper;
import com.example.city.mapper.EnterpriseStatusMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AppointmentTimeoutService {

    @Resource
    private AppointmentMapper appointmentMapper;
    @Resource
    private EnterpriseStatusMapper enterpriseStatusMapper;

    @Scheduled(cron = "0 */1 * * * *")
    public void markNoShowAppointments() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 查询所有已过期的未到场预约
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_status", "1")
                .lt("app_end_time", now);
        List<Appointment> expiredAppointments = appointmentMapper.selectList(queryWrapper);

        if (expiredAppointments.isEmpty()) {
            return;
        }

        // 按企业统计过期预约数，更新企业已预约人数
        Map<String, Long> enterpriseCounts = expiredAppointments.stream()
                .collect(Collectors.groupingBy(
                        Appointment::getEnterpriseID,
                        Collectors.counting()));

        for (Map.Entry<String, Long> entry : enterpriseCounts.entrySet()) {
            EnterpriseStatus status = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", entry.getKey()));
            if (status != null) {
                int currentCount = Integer.parseInt(status.getReservedCount());
                int newCount = Math.max(0, currentCount - entry.getValue().intValue());
                status.setReservedCount(String.valueOf(newCount));
                enterpriseStatusMapper.updateById(status);
            }
        }

        // 批量将过期预约状态从 1（已预约未到场）改为 3（预约未到场）
        UpdateWrapper<Appointment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("app_status", "3")
                .eq("app_status", "1")
                .lt("app_end_time", now);
        appointmentMapper.update(null, updateWrapper);
    }
}
