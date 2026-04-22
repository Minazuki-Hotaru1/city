package com.example.city.Async;

import com.example.city.entity.EnterpriseConfirm;
import com.example.city.mapper.EnterpriseConfirmMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


//异步处理类
@Service
public class ConfirmAsyncService {

    @Resource
    private EnterpriseConfirmMapper confirmMapper;

    @Async
    public void updateHaveSeeIt(List<EnterpriseConfirm> confirms) {
        for (EnterpriseConfirm confirm : confirms) {
            if ("1".equals(confirm.getHaveSeeIt())) {
                confirm.setHaveSeeIt("2");
            }
        }
        for (EnterpriseConfirm confirm : confirms) {
            confirmMapper.updateById(confirm);
        }
    }
}
