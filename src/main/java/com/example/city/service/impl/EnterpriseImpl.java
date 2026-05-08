package com.example.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.EnterpriseAppVO;
import com.example.city.entity.Appointment;
import com.example.city.entity.EnterpriseConfirm;
import com.example.city.entity.Enterprise;
import com.example.city.entity.User;
import com.example.city.mapper.AppointmentMapper;
import com.example.city.mapper.EnterpriseConfirmMapper;
import com.example.city.mapper.EnterpriseMapper;
import com.example.city.mapper.UserMapper;
import com.example.city.service.EnterpriseService;
import com.example.city.Utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EnterpriseImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private EnterpriseConfirmMapper enterpriseConfirmMapper;

    @Resource
    private JwtUtil jwtUtil;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Enterprise> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .eq("password", password);

        Enterprise enterprise = enterpriseMapper.selectOne(wrapper);
        if (enterprise == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(
                enterprise.getUsername(),
                Collections.singletonMap("enterpriseID", enterprise.getId())
        );

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", enterprise.getUsername());
        result.put("ID", enterprise.getId());
        return result;

    }



    //企业用户注册
    @Override
    public Map<String, Object> registration(Map<String, Object> data) {
        Enterprise enterprise = enterpriseMapper.selectOne(new QueryWrapper<Enterprise>().eq("username", data.get("username")));
        EnterpriseConfirm co = enterpriseConfirmMapper.selectOne(new QueryWrapper<EnterpriseConfirm>().eq("username", data.get("username")));
        Map<String, Object> result = new HashMap<>();

        //判断账号重复(判断已在注册完毕的用户表)
        if (enterprise != null){
            result.put("success", false);
            result.put("message", "账号重复，请换个账号");
            return result;
        }

        //判断账号重复(判断还在审核的注册表)
        if (co != null){
            result.put("success", false);
            result.put("message", "该账号正在审核中，请勿重复注册");
            return result;
        }


        EnterpriseConfirm confirm = new EnterpriseConfirm();
        confirm.setUsername((String) data.get("username"));
        confirm.setPassword((String) data.get("password"));
        confirm.setTypeID((String) data.get("typeID"));
        confirm.setRoles((String) data.get("roles"));
        confirm.setAddress((String) data.get("address"));
        confirm.setHaveSeeIt("1");

        try {
            int i = enterpriseConfirmMapper.insert(confirm);
            result.put("success", true);
            result.put("message", "创建成功，请等待管理员审核账号");
        }catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }


    //企业用户分页获取自己的所有预约用户的方法，前端需要返回企业用户的id信息
    @Override
    public Page<EnterpriseAppVO> getAllApp(String enId, Long page, Long number) {
        List<EnterpriseAppVO> voList = new ArrayList<>();
        //分页查询该企业的预约信息
        Page<Appointment> page1 = appointmentMapper.selectPage(
                new Page<>(page, number), new QueryWrapper<Appointment>().eq("enterprise_id", enId)
        );
        List<Appointment> appList = page1.getRecords();
        if(appList == null){
            //返回null，前端来进行判断
            return null;
        }
        //根据表中的用户id来查询用户的信息,并存到voList中
        for (Appointment appointment : appList) {
            EnterpriseAppVO vo = new EnterpriseAppVO();
            BeanUtils.copyProperties(appointment, vo);
            //查询用户表
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().eq("id",  appointment.getUserID())
            );
            vo.setId(appointment.getId());
            vo.setUserId(user.getId());
            vo.setUserName(user.getUsername());
            vo.setUserAddress(user.getAddress());
            vo.setAppStartTime(appointment.getStartTime());
            vo.setAppEndTime(appointment.getEndTime());

            //传递给前端预约状态
            String status = appointment.getAppStatus();
            String statusText = switch (status) {
                case "1" -> "已预约";
                case "2" -> "已完成";
                case "3" -> "已预约但未到达";
                default -> "";
            };

            vo.setAppStatus(statusText);
            voList.add(vo);
        }

        Page<EnterpriseAppVO> voPage = new Page<>();
        BeanUtils.copyProperties(voList, voPage);
        voPage.setRecords(voList);

        return voPage;
    }


    //查询状态为1（已预约未到场）的预约记录
    @Override
    public Page<EnterpriseAppVO> getPendingApp(String enId, Long page, Long number) {
        List<EnterpriseAppVO> voList = new ArrayList<>();
        Page<Appointment> page1 = appointmentMapper.selectPage(
                new Page<>(page, number),
                new QueryWrapper<Appointment>()
                        .eq("enterprise_id", enId)
                        .eq("app_status", "1")
        );
        List<Appointment> appList = page1.getRecords();
        if (appList == null || appList.isEmpty()) {
            return null;
        }
        for (Appointment appointment : appList) {
            EnterpriseAppVO vo = new EnterpriseAppVO();
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().eq("id", appointment.getUserID())
            );
            vo.setId(appointment.getId());
            vo.setUserId(user.getId());
            vo.setUserName(user.getUsername());
            vo.setUserAddress(user.getAddress());
            vo.setAppStartTime(appointment.getStartTime());
            vo.setAppEndTime(appointment.getEndTime());
            vo.setRemarks(appointment.getRemarks());
            voList.add(vo);
        }

        Page<EnterpriseAppVO> voPage = new Page<>();
        BeanUtils.copyProperties(page1, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    //当预约的用户到线下后，企业用户便可以通过这个用户审核，并且更改预约用户的状态
    @Override
    public Map<String, Object> appPass(String userId) {
        Map<String, Object> result = new HashMap<>();
        Appointment appointment = appointmentMapper.selectOne(
                new QueryWrapper<Appointment>().eq("user_id", userId)
        );
        if(appointment.getUserID().equals("1") || appointment.getUserID().equals("2")){
            result.put("success", false);
            result.put("message", "请勿重复操作");
            return result;
        }
        try {
            appointment.setAppStatus("3");
            appointmentMapper.updateById(appointment);
            result.put("success", true);
            result.put("message", "通过成功");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
}
