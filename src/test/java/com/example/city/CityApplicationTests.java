package com.example.city;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.city.Utils.AddressUtil;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.Utils.PasswordUtil;
import com.example.city.VO.AddressVO;
import com.example.city.VO.EnterpriseVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.AdminService;
import com.example.city.service.EnterpriseService;
import com.github.javafaker.Faker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Random;

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
    @Resource
    private GetLatAndLong getLatAndLong;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private PasswordUtil passwordUtil;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AddressUtil addressUtil;

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

//    @Test
//    void test2() {
//        List<Address> addressList = addressMapper.selectList(null);
//        List<AddressVO> voList = new ArrayList<>();
//        AddressVO vo = new AddressVO();
//        System.out.println(new Address());
//        for (Address address : addressList) {
//            BeanUtils.copyProperties(address, vo);
//            Enterprise enterprise = enterpriseMapper.selectOne(
//                    new QueryWrapper<Enterprise>().eq("id", address.getEnterpriseID())
//            );
//            vo.setEnterpriseName(enterprise.getRoles());
//
//            voList.add(vo);
//        }
//        System.out.println(voList);
//    }

    @Test
    void test3() {
        Page<Enterprise> page1 = new Page<>(1, 10);
        Page<Enterprise> result = enterpriseMapper.selectPage(page1, null);
        List<Enterprise> enterprises = result.getRecords();
        List<EnterpriseVO> voList = new ArrayList<>();

        for (Enterprise enterprise : enterprises) {
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
        Map<String, Object> map = getLatAndLong.getLatAndLong("星星充电汽车充电站(永云新能源学府路充电站)");
        System.out.println(map);
    }

    @Test
    void test6() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("C:/Users/18767/Desktop/district.json");
        Map<String, Object> data = mapper.readValue(file, Map.class);
        List list = new ArrayList();

        // 第一层：districts
        List<Map<String, Object>> provinces =
                (List<Map<String, Object>>) data.get("districts");

        List<Map<String, Object>> cities =
                (List<Map<String, Object>>) data.get("districts");

        for (Map<String, Object> city : cities) {

            // 找昆明市
            if ("昆明市".equals(city.get("name"))) {

                List<Map<String, Object>> areas =
                        (List<Map<String, Object>>) city.get("districts");

                for (Map<String, Object> area : areas) {

                    // 找五华区
                    if ("五华区".equals(area.get("name"))) {

                        List<Map<String, Object>> streets =
                                (List<Map<String, Object>>) area.get("districts");

                        for (Map<String, Object> street : streets) {
                            Random random = new Random();
                            int num = random.nextInt(500);
                            String streetName = (String) street.get("name");

                            list.add("云南省昆明市五华区" + streetName + num + "号");
                        }
                    }
                }
            }
        }
    }

    //随机添加用户数据
    @Test
    void test7() throws IOException {
        List listqu = new ArrayList();
        listqu.add("西山区");
        listqu.add("五华区");
        listqu.add("盘龙区");
        listqu.add("官渡区");
        listqu.add("呈贡区");


        for (int i = 0; i < 1000; i++) {
            //随机生成账号
            User user = new User();
            Faker faker = new Faker();
            String username = faker.name().username();
            String password = passwordUtil.generatePassword(10);
            //生成随机的号数
            Random random = new Random();
            int num1 = random.nextInt(500);
            int num2 = random.nextInt(listqu.size());
            //获取方法中返回的长度，获取随机一个内容
            List list = addressUtil.AddressUtil((String) listqu.get(num2));
            int num3 = random.nextInt(list.size());
            user.setUsername(username);
            user.setPassword(password);
            user.setAddress((String) list.get(num3) + num1 + "号");
            Map<String, Object> map = getLatAndLong.getLatAndLong(user.getAddress());
            user.setLatitude((String) map.get("lat"));
            user.setLongitude((String) map.get("lng"));

            System.out.println(user);
            try {
                Thread.sleep(9000); //9秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            userMapper.insert(user);
        }

    }
}
