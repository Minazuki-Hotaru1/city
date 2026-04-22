package com.example.city;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.VO.AddressVO;
import com.example.city.VO.EnterpriseVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.AdminService;
import com.example.city.service.EnterpriseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class CityApplicationTests {
    @Resource
    private AdminService adminService;
    @Resource
    private EnterpriseService enterpriseService;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private EnterpriseConfirmMapper confirmMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private AddressMapper addressMapper;
    @Test
    void contextLoads() {
    }

    @Test
    void getEnterpriseById() {
        Enterprise enterprise = enterpriseMapper.selectOne(new QueryWrapper<Enterprise>().eq("username", "root1"));
        System.out.println(enterprise);

    }

    @Test
    void setEnterprise() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "root123321");
        map.put("password", "1231312312");
        map.put("typeID", "103");
        map.put("roles", "测试");
        EnterpriseConfirm confirm = new EnterpriseConfirm();
        confirm.setUsername((String) map.get("username"));
        confirm.setPassword((String) map.get("password"));
        confirm.setTypeID((String) map.get("typeID"));
        confirm.setRoles((String) map.get("roles"));
        int i = confirmMapper.insert(confirm);
        System.out.println(i);
    }

    @Test
    void getConfirm() {
        Page<EnterpriseConfirm> page = new Page<>(1, 10);
        Page<EnterpriseConfirm> result = confirmMapper.selectPage(page, null);


        List<EnterpriseConfirm> confirms = result.getRecords();
        for (EnterpriseConfirm confirm : confirms) {
            QueryWrapper<Type> queryWrapper = new QueryWrapper<>();
            Type type = typeMapper.selectOne(queryWrapper.eq("type_id", confirm.getTypeID()));
            confirm.setTypeID(type.getTypeName());
        }
        System.out.println(confirms);


//        System.out.println(confirms);
//        for (Confirm confirm : confirms) {
//            confirmMapper.updateById(confirm);  // 根据主键进行更新
//        }


    }

    @Test
    void approved() {
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("id", "80002");
        EnterpriseConfirm confirm = confirmMapper.selectOne(wrapper);
        confirm.setHaveSeeIt("2");
        confirmMapper.update(confirm, wrapper);

        //存入到enterprise中
        Enterprise enterprise = new Enterprise();
        enterprise.setTypeID(confirm.getTypeID());
        enterprise.setId(confirm.getId());
        enterprise.setUsername(confirm.getUsername());
        enterprise.setPassword(confirm.getPassword());
        enterprise.setRoles(confirm.getRoles());
        enterpriseMapper.insert(enterprise);

    }

    @Test
    void test1() {
        Page<EnterpriseConfirm> page1 = new Page<>(1, 10);
        QueryWrapper<EnterpriseConfirm> wrapper = new QueryWrapper<>();
        wrapper.eq("have_see_it", "0")
                .or()
                .eq("have_see_it", "1");
        Page<EnterpriseConfirm> result = confirmMapper.selectPage(page1, wrapper);
        List<EnterpriseConfirm> confirms = result.getRecords();
        System.out.println(confirms);
    }

    @Test
    void test2() {
        List<Address> addressList = addressMapper.selectList(null);
        List<AddressVO> voList = new ArrayList<>();
        AddressVO vo = new AddressVO();
        System.out.println(new Address());
        for (Address address : addressList) {
            BeanUtils.copyProperties(address, vo);
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new QueryWrapper<Enterprise>().eq("id", address.getEnterpriseID())
            );
            vo.setEnterpriseName(enterprise.getRoles());

            voList.add(vo);
        }
        System.out.println(voList);
    }

    @Test
    void test3() {
        Page<Enterprise> page1 = new Page<>(1, 10);
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
        System.out.println(voPage);
    }

    @Test
    void test4() {
        GetLatAndLong getLatAndLong = new GetLatAndLong();
        Map map = getLatAndLong.getLatAndLong("云南省第一人民医院");
        System.out.println(map);
    }

}
