package com.example.city.Async;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.city.entity.Appointment;
import com.example.city.mapper.AppointmentMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentTimeoutService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Scheduled(cron = "0 */1 * * * *")
    public void markNoShowAppointments() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        UpdateWrapper<Appointment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("app_status", "3")
                .eq("app_status", "1")
                .lt("app_end_time", now);

        appointmentMapper.update(null, updateWrapper);
    }
}
