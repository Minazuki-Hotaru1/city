package com.example.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.EnterpriseAppVO;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.EnterpriseService;
import com.example.city.Utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private EnterpriseStatusMapper enterpriseStatusMapper;
    @Resource
    private GetLatAndLong getLatAndLong;
    @Resource
    private TypeMapper typeMapper;

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

    //查询该企业所有预约（状态1/2/3），按预约开始时间从新到旧排列
    @Override
    public Page<EnterpriseAppVO> getAllAppSorted(String enId, Long page, Long number) {
        List<EnterpriseAppVO> voList = new ArrayList<>();
        Page<Appointment> page1 = appointmentMapper.selectPage(
                new Page<>(page, number),
                new QueryWrapper<Appointment>()
                        .eq("enterprise_id", enId)
                        .in("app_status", "1", "2", "3")
                        .orderByDesc("app_start_time")
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
            vo.setAppStatus(appointment.getAppStatus());
            vo.setRemarks(appointment.getRemarks());
            voList.add(vo);
        }

        Page<EnterpriseAppVO> voPage = new Page<>();
        BeanUtils.copyProperties(page1, voPage);
        voPage.setRecords(voList);
        return voPage;
    }

    //获取该企业预约柱状图数据，按日期分组，统计各状态数量
    @Override
    public Map<String, Object>  getAppointmentChart(String enId) {
        Map<String, Object> result = new HashMap<>();
        List<Appointment> appointments = appointmentMapper.selectList(
                new QueryWrapper<Appointment>().eq("enterprise_id", enId));

        if (appointments == null || appointments.isEmpty()) {
            result.put("dates", Collections.emptyList());
            result.put("status1", Collections.emptyList());
            result.put("status2", Collections.emptyList());
            result.put("status3", Collections.emptyList());
            return result;
        }

        // 按日期排序并提取日期列表
        List<String> dates = appointments.stream()
                .map(a -> a.getStartTime() != null && a.getStartTime().length() >= 10
                        ? a.getStartTime().substring(0, 10) : "未知")
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 按日期统计各状态数量
        List<Integer> status1Counts = new ArrayList<>();
        List<Integer> status2Counts = new ArrayList<>();
        List<Integer> status3Counts = new ArrayList<>();

        for (String date : dates) {
            long s1 = appointments.stream().filter(a ->
                    a.getStartTime() != null && a.getStartTime().startsWith(date) && "1".equals(a.getAppStatus())).count();
            long s2 = appointments.stream().filter(a ->
                    a.getStartTime() != null && a.getStartTime().startsWith(date) && "2".equals(a.getAppStatus())).count();
            long s3 = appointments.stream().filter(a ->
                    a.getStartTime() != null && a.getStartTime().startsWith(date) && "3".equals(a.getAppStatus())).count();
            status1Counts.add((int) s1);
            status2Counts.add((int) s2);
            status3Counts.add((int) s3);
        }

        result.put("dates", dates);
        result.put("status1", status1Counts);
        result.put("status2", status2Counts);
        result.put("status3", status3Counts);
        return result;
    }

    //获取企业个人信息
    @Override
    public Map<String, Object> getEnterpriseProfile(String enId) {
        Map<String, Object> result = new HashMap<>();
        Enterprise enterprise = enterpriseMapper.selectOne(
                new QueryWrapper<Enterprise>().eq("id", enId));
        if (enterprise == null) {
            result.put("success", false);
            result.put("message", "企业不存在");
            return result;
        }

        Address address = addressMapper.selectOne(
                new QueryWrapper<Address>().eq("enterprise_id", enId));
        Type type = typeMapper.selectOne(
                new QueryWrapper<Type>().eq("type_id", enterprise.getTypeID()));

        result.put("success", true);
        result.put("username", enterprise.getUsername());
        result.put("enterpriseName", enterprise.getRoles());
        result.put("typeName", type != null ? type.getTypeName() : "未知");
        result.put("address", address != null ? address.getAddressName() : "");
        result.put("latitude", address != null ? address.getLatitude() : "");
        result.put("longitude", address != null ? address.getLongitude() : "");
        return result;
    }

    //更新企业地址（重新获取经纬度）
    @Override
    public Map<String, Object> updateEnterpriseAddress(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String enId = (String) data.get("enId");
        String newAddress = (String) data.get("address");

        Address address = addressMapper.selectOne(
                new QueryWrapper<Address>().eq("enterprise_id", enId));
        if (address == null) {
            result.put("success", false);
            result.put("message", "未找到该企业的地址信息");
            return result;
        }

        try {
            Map<String, Object> coords = getLatAndLong.getLatAndLong(newAddress);
            address.setAddressName(newAddress);
            address.setLatitude(String.valueOf(coords.get("lat")));
            address.setLongitude(String.valueOf(coords.get("lng")));
            addressMapper.updateById(address);

            result.put("success", true);
            result.put("message", "地址更新成功");
            result.put("latitude", address.getLatitude());
            result.put("longitude", address.getLongitude());
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //修改企业密码
    @Override
    public Map<String, Object> updateEnterprisePassword(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String enId = (String) data.get("enId");
        String oldPassword = (String) data.get("oldPassword");
        String newPassword = (String) data.get("newPassword");

        Enterprise enterprise = enterpriseMapper.selectOne(
                new QueryWrapper<Enterprise>().eq("id", enId));
        if (enterprise == null) {
            result.put("success", false);
            result.put("message", "企业不存在");
            return result;
        }
        if (!enterprise.getPassword().equals(oldPassword)) {
            result.put("success", false);
            result.put("message", "原密码错误");
            return result;
        }

        enterprise.setPassword(newPassword);
        enterpriseMapper.updateById(enterprise);
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }

    //当预约的用户到线下后，企业用户确认到场，更改预约状态为已到场
    @Override
    public Map<String, Object> appPass(String appointmentId) {
        Map<String, Object> result = new HashMap<>();
        Appointment appointment = appointmentMapper.selectOne(
                new QueryWrapper<Appointment>().eq("id", appointmentId)
        );
        if (appointment == null) {
            result.put("success", false);
            result.put("message", "预约记录不存在");
            return result;
        }
        if ("2".equals(appointment.getAppStatus()) || "3".equals(appointment.getAppStatus())) {
            result.put("success", false);
            result.put("message", "该预约已处理，请勿重复操作");
            return result;
        }
        try {
            appointment.setAppStatus("2");
            appointmentMapper.updateById(appointment);

            // 企业在线人数 +1
            EnterpriseStatus status = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", appointment.getEnterpriseID()));
            if (status != null) {
                int onlineCount = Integer.parseInt(status.getOnlineCount());
                status.setOnlineCount(String.valueOf(onlineCount + 1));
                enterpriseStatusMapper.updateById(status);
            }

            result.put("success", true);
            result.put("message", "确认到场成功");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
}
