package com.example.city.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.VO.AddressVO;
import com.example.city.VO.ConfirmVO;
import com.example.city.VO.EnterpriseVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.AdminService;
import com.example.city.Async.ConfirmAsyncService;
import com.example.city.Utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private ConfirmAsyncService confirmAsyncService;
    @Resource
    private JwtUtil jwtUtil;


    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
                .eq("password", password);

        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
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
        result.put("adminId", admin.getId());
        result.put("loginTime", timeStr);
        return result;
    }


    //分页查询需要审核的注册用户
    //只查询have_see_it为1或2的用户，3表示审核通过，4表示审核不通过
    @Override
    public Page<ConfirmVO> getConfirm(long page, long number) {
        Page<EnterpriseConfirm> page1 = new Page<>(page, number);

        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("have_see_it", "1")
                .or()
                .eq("have_see_it", "2");

        Page<EnterpriseConfirm> result = enterpriseConfirmMapper.selectPage(page1, null);
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

        confirmAsyncService.updateHaveSeeIt(confirms);

        return voPage;
    }

    //企业用户账号审核通过方法方法
    //3表示审核通过
    @Override
    public Map<String, Object> approved(String id) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        EnterpriseConfirm confirm = enterpriseConfirmMapper.selectOne(wrapper);
        confirm.setHaveSeeIt("3");
        enterpriseConfirmMapper.update(confirm, wrapper);
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
        try {
            en.setId(null);
            enterpriseMapper.insert(en);
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
}
