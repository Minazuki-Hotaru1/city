package com.example.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.Utils.PasswordUtil;
import com.example.city.VO.AddressVO;
import com.example.city.VO.ConfirmVO;
import com.example.city.VO.EnterpriseVO;
import com.example.city.VO.UserVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.AdminService;
import com.example.city.Async.ConfirmAsyncService;
import com.example.city.Utils.JwtUtil;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private EnterpriseConfirmMapper enterpriseConfirmMapper;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AppointmentMapper appointmentMapper;
    @Resource
    private EnterpriseStatusMapper enterpriseStatusMapper;

    @Resource
    private ConfirmAsyncService confirmAsyncService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private GetLatAndLong getLatAndLong;
    @Resource
    private PasswordUtil passwordUtil;


    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null || !passwordUtil.matches(password, admin.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(
                admin.getUsername(),
                Collections.singletonMap("adminID", admin.getId())
        );

        //登录日期记录，方便查看
        String timeStr = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        admin.setLoginTime(timeStr);
        adminMapper.updateById(admin);

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", admin.getUsername());
        result.put("ID", admin.getId());
        result.put("loginTime", timeStr);
        return result;
    }


    //分页查询需要审核的注册用户
    //只查询have_see_it为1或2的用户，3表示审核通过，4表示审核不通过
    @Override
    public Page<ConfirmVO> getConfirm(long page, long number, String reviewStatus) {
        Page<EnterpriseConfirm> page1 = new Page<>(page, number);

        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        if ("reviewed".equals(reviewStatus)) {
            wrapper.in("have_see_it", "3", "4");
        } else if ("pending".equals(reviewStatus)) {
            wrapper.in("have_see_it", "1", "2");
        }
        wrapper.orderByDesc("id");

        Page<EnterpriseConfirm> result = enterpriseConfirmMapper.selectPage(page1, wrapper);
        List<EnterpriseConfirm> confirms = result.getRecords();

        List<ConfirmVO> voList = new ArrayList<>();

        for (EnterpriseConfirm confirm : confirms) {
            ConfirmVO vo = new ConfirmVO();
            BeanUtils.copyProperties(confirm, vo);

            Type type = typeMapper.selectOne(
                    new QueryWrapper<Type>().eq("type_id", confirm.getTypeID())
            );

            vo.setRoleName(type != null ? type.getTypeName() : "未知角色");

            voList.add(vo);
        }

        Page<ConfirmVO> voPage = new Page<>();
        BeanUtils.copyProperties(result, voPage);
        voPage.setRecords(voList);

        if ("pending".equals(reviewStatus)) {
            confirmAsyncService.updateHaveSeeIt(confirms);
        }

        return voPage;
    }

    @Override
    public Long getNewConfirmCount() {
        return enterpriseConfirmMapper.selectCount(
                new QueryWrapper<EnterpriseConfirm>().eq("have_see_it", "1")
        );
    }

    //企业用户账号审核通过方法方法
    //3表示审核通过
    @Override
    public Map<String, Object> approved(String id) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        EnterpriseConfirm confirm = enterpriseConfirmMapper.selectOne(wrapper);
        QueryWrapper<Enterprise> wapper1 = new QueryWrapper<>();
        wapper1.eq("username", confirm.getUsername());
        Enterprise enterprise = enterpriseMapper.selectOne(wapper1);
        //在同一个界面中，当用户继续点击确认按钮时，不继续添加用户，并给出提示
        try{
        if(confirm.getUsername().equals(enterprise.getUsername())){
            result.put("success", false);
            result.put("message", "请勿重复点击");
            return result;
        }}catch(Exception ignored){}

        //存储到企业表中
        Enterprise en = new Enterprise();
        en.setUsername(confirm.getUsername());
        en.setPassword(confirm.getPassword());
        en.setTypeID(confirm.getTypeID());
        en.setRoles(confirm.getRoles());

        //存到地址表中
        Address address = new Address();
        try {
            Map<String, Object> addressMap = getLatAndLong.getLatAndLong(confirm.getAddress());
            en.setId(null);
            enterpriseMapper.insert(en);
            //查询新添加的用户的id是多少
            Enterprise en1 = enterpriseMapper.selectOne(
                    new QueryWrapper<Enterprise>().eq("username", en.getUsername())
            );
            //添加到address表中
            address.setEnterpriseID(en1.getId());
            //通过地址获取lan和long
            address.setLatitude(String.valueOf(addressMap.get("lat")));
            address.setLongitude(String.valueOf(addressMap.get("lng")));
            address.setAddressName(confirm.getAddress());
            addressMapper.insert(address);
            confirm.setHaveSeeIt("3");
            enterpriseConfirmMapper.update(confirm, wrapper);
            result.put("success", true);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }

    }

    //企业审核不通过
    @Override
    public Map<String, Object> unApproved(String id) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        EnterpriseConfirm confirm = enterpriseConfirmMapper.selectOne(wrapper);
        //在同一个界面中，当用户继续点击确认按钮时，不继续修改用户，并给出提示
        try{
            if(confirm.getHaveSeeIt().equals("4")){
                result.put("success", false);
                result.put("message", "请勿重复点击");
                return result;
            }
        }catch (Exception ignored){}
        try{
            confirm.setHaveSeeIt("4");
            enterpriseConfirmMapper.update(confirm, wrapper);
            result.put("success", true);
            return result;
        }catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //获取所有地址的方法
    @Override
    public List<AddressVO> getAddress() {
        List<Address> addressList = addressMapper.selectList(null);
        List<AddressVO> voList = new ArrayList<>();
        for (Address address : addressList) {
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(address, vo);
            //查询对应企业的名字
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new QueryWrapper<Enterprise>().eq("id", address.getEnterpriseID())
            );

            //查询对应企业的类型
            Type type = typeMapper.selectOne(
                    new QueryWrapper<Type>().eq("type_id", enterprise.getTypeID())
            );

            vo.setEnterpriseName(enterprise != null ? enterprise.getRoles() : "未知企业");
            vo.setTypeName(type.getTypeName());


            //查询对应企业的状态并存储
            EnterpriseStatus enterpriseStatus = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", address.getEnterpriseID())
            );
            if(enterpriseStatus != null){
                vo.setReservedCount(enterpriseStatus.getReservedCount());
                vo.setReservationCapacity(enterpriseStatus.getReservationCapacity());
                vo.setOnlineCount(enterpriseStatus.getOnlineCount());
                vo.setOnlineCapacity(enterpriseStatus.getOnlineCapacity());
            }


            voList.add(vo);
        }
        return voList;

    }


    //分页查询注册完毕的企业用户列表
    @Override
    public Page<EnterpriseVO> getEnterprise(long page, long number) {
        Page<Enterprise> page1 = new Page<>(page, number);
        Page<Enterprise> result = enterpriseMapper.selectPage(page1, null);
        List<Enterprise> enterprises = result.getRecords();
        List<EnterpriseVO> voList = new ArrayList<>();

        for(Enterprise enterprise : enterprises){
            EnterpriseVO vo = new EnterpriseVO();
            BeanUtils.copyProperties(enterprise, vo);
            Type type = typeMapper.selectOne(
                    new QueryWrapper<Type>().eq("type_id", enterprise.getTypeID())
            );
            vo.setTypeName(type.getTypeName());
            voList.add(vo);
        }
        Page<EnterpriseVO> voPage = new Page<>();
        BeanUtils.copyProperties(result, voPage);
        voPage.setRecords(voList);

        return voPage;
    }

    //管理员用户查看普通用户的地图分布的数据
    @Override
    public List<Map<String, Object>> getAllUser() {
        List<Map<String, Object>> userList;
        userList = userMapper.selectMaps(null);
        return userList;
    }


    //返回用户信息及预约数量，按用户分页
    @Override
    public Page<UserVO> getAllUserPage(long page, long number) {
        Page<User> page1 = new Page<>(page, number);
        Page<User> result = userMapper.selectPage(page1, null);
        List<User> users = result.getRecords();

        // 批量查询每个用户的预约记录数
        Set<String> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        Map<String, Long> countMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Appointment> allAppointments = appointmentMapper.selectList(
                    new QueryWrapper<Appointment>().in("user_id", userIds));
            countMap = allAppointments.stream()
                    .collect(Collectors.groupingBy(
                            Appointment::getUserID,
                            Collectors.counting()));
        }

        List<UserVO> voList = new ArrayList<>();
        for (User user : users) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setAppointmentCount(countMap.getOrDefault(user.getId(), 0L).intValue());
            voList.add(vo);
        }

        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(result, voPage);
        voPage.setRecords(voList);

        return voPage;
    }

    //根据用户id查询该用户的所有预约记录
    @Override
    public List<Map<String, Object>> getUserAppointments(String userId) {
        QueryWrapper<Appointment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("id");
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("userId", a.getUserID());
            map.put("enterpriseId", a.getEnterpriseID());
            map.put("startTime", a.getStartTime());
            map.put("endTime", a.getEndTime());
            map.put("appStatus", a.getAppStatus());
            map.put("remarks", a.getRemarks());
            result.add(map);
        }
        return result;
    }

    //返回企业用户的预约、在线人数状态
    @Override
    public Map<String, Object> getEnStatus (@RequestParam String id) {
        Map<String,Object> result = new HashMap<>();
        EnterpriseStatus enterpriseStatus = enterpriseStatusMapper.selectOne(
                new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", id));
        try{
            if(enterpriseStatus == null){
                result.put("success", false);
                result.put("message", "未查询到信息");
                return result;
            }

            result.put("success", true);
            result.put("reservedCount",  enterpriseStatus.getReservedCount());
            result.put("reservationCapacity", enterpriseStatus.getReservationCapacity());
            result.put("onlineCount", enterpriseStatus.getOnlineCount());
            result.put("onlineCapacity", enterpriseStatus.getOnlineCapacity());
            return result;

        } catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return null;

    }

    //获取管理员个人信息
    @Override
    public Map<String, Object> getAdminProfile(String adminId) {
        Map<String, Object> result = new HashMap<>();
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("id", adminId));
        if (admin == null) {
            result.put("success", false);
            result.put("message", "管理员不存在");
            return result;
        }
        result.put("success", true);
        result.put("username", admin.getUsername());
        result.put("loginTime", admin.getLoginTime());
        return result;
    }

    //修改管理员密码
    @Override
    public Map<String, Object> updateAdminPassword(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String adminId = (String) data.get("adminId");
        String oldPassword = (String) data.get("oldPassword");
        String newPassword = (String) data.get("newPassword");

        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("id", adminId));
        if (admin == null) {
            result.put("success", false);
            result.put("message", "管理员不存在");
            return result;
        }
        if (!passwordUtil.matches(oldPassword, admin.getPassword())) {
            result.put("success", false);
            result.put("message", "原密码错误");
            return result;
        }

        admin.setPassword(passwordUtil.encode(newPassword));
        adminMapper.updateById(admin);
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }
}
