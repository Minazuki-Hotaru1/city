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

        // 1. 状态 1（已预约未到场）→ 状态 3（预约未到场），减少企业已预约人数
        processStatusTransition("1", "3", now, true);

        // 2. 状态 2（已到场）→ 状态 4（已离开），减少企业在线人数
        processStatusTransition("2", "4", now, false);
    }

    private void processStatusTransition(String fromStatus, String toStatus, String now, boolean isReserved) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_status", fromStatus)
                .lt("app_end_time", now);
        List<Appointment> expiredAppointments = appointmentMapper.selectList(queryWrapper);

        if (expiredAppointments.isEmpty()) {
            return;
        }

        // 按企业统计过期预约数，更新对应企业的人数
        Map<String, Long> enterpriseCounts = expiredAppointments.stream()
                .collect(Collectors.groupingBy(
                        Appointment::getEnterpriseID,
                        Collectors.counting()));

        for (Map.Entry<String, Long> entry : enterpriseCounts.entrySet()) {
            EnterpriseStatus status = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", entry.getKey()));
            if (status != null) {
                if (isReserved) {
                    int currentCount = Integer.parseInt(status.getReservedCount());
                    status.setReservedCount(String.valueOf(Math.max(0, currentCount - entry.getValue().intValue())));
                    enterpriseStatusMapper.updateById(status);
                }
                 else {
                     int currentCount = Integer.parseInt(status.getOnlineCount());
                     status.setOnlineCount(String.valueOf(Math.max(0, currentCount - entry.getValue().intValue())));
                     enterpriseStatusMapper.updateById(status);
                 }
            }
        }

        // 批量更新预约状态
        UpdateWrapper<Appointment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("app_status", toStatus)
                .eq("app_status", fromStatus)
                .lt("app_end_time", now);
        appointmentMapper.update(null, updateWrapper);
    }
}
