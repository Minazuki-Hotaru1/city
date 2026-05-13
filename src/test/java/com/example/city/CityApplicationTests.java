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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Resource
    private EnterpriseStatusMapper enterpriseStatusMapper;

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
    void test4 () throws IOException {
        List<Address> addressList = addressMapper.selectList(null);
        for (Address address : addressList) {
            if(address.getLongitude() == null){
                Map<String, Object> map = getLatAndLong.getLatAndLong(address.getAddressName());
                address.setLatitude((String) map.get("lat"));
                address.setLongitude((String) map.get("lng"));
                addressMapper.updateById(address);
                try{
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
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
            // 生成随机密码并加密
            String rawPassword = faker.internet().password(8, 16);
            String password = passwordUtil.encode(rawPassword);
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

    @Test
    void test8() throws JsonProcessingException {
//        String url = "https://restapi.amap.com/v5/direction/driving?"
//                + "origin=" + "102.701,25.04782"
//                + "&destination=" + "102.707559,25.031808"
//                + "&key=e052f376d6489de2f784770cf32eba4d"
//                + "&show_fields=cost";
//        String resultJson = restTemplate.getForObject(url, String.class);
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(resultJson);
//
//        JsonNode paths = root.path("route").path("paths");
//
//        int duration = paths.get(0).path("cost").path("duration").asInt();
//
//        System.out.println("耗时：" + duration + "秒");


    }

    @Test
    void test9() throws IOException {
        Appointment appointment = appointmentMapper.selectOne(
                new QueryWrapper<Appointment>().eq("id", 100001)
        );
        String date = appointment.getStartTime().substring(0, 10);
        System.out.println(date);
    }

    @Test
    void generateEnterpriseStatus() {
        List<Enterprise> enterpriseList = enterpriseMapper.selectList(null);
        Random random = new Random();

        for (Enterprise enterprise : enterpriseList) {
            // 检查是否已存在该企业的状态记录，已存在则跳过
            EnterpriseStatus exist = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", enterprise.getId())
            );
            if (exist != null) {
                System.out.println("企业 " + enterprise.getRoles() + " (id=" + enterprise.getId() + ") 已有状态记录，跳过");
                continue;
            }

            EnterpriseStatus status = new EnterpriseStatus();
            status.setEnterpriseID(enterprise.getId());

            String typeId = enterprise.getTypeID();
            int reservationCapacity;
            int onlineCapacity;

            switch (typeId) {
                case "101" -> { // 医院：预约量大，在线容量高
                    reservationCapacity = 200 + random.nextInt(301);
                    onlineCapacity = 500 + random.nextInt(1001);
                }
                case "102" -> { // 停车场：车位有限，容量较小
                    reservationCapacity = 30 + random.nextInt(91);
                    onlineCapacity = 30 + random.nextInt(91);
                }
                case "103" -> { // 公园景点：游客容量大，预约量高
                    reservationCapacity = 500 + random.nextInt(1501);
                    onlineCapacity = 2000 + random.nextInt(3001);
                }
                case "104" -> { // 充电桩：充电位稀缺，容量很小
                    reservationCapacity = 8 + random.nextInt(23);
                    onlineCapacity = 8 + random.nextInt(23);
                }
                default -> { // 其他类型：默认中等容量
                    reservationCapacity = 50 + random.nextInt(151);
                    onlineCapacity = 100 + random.nextInt(201);
                }
            }

            // 当前已预约数：0 到 reservationCapacity 的 80% 之间随机
            int reservedCount = (int) (reservationCapacity * 0.2) + random.nextInt((int) (reservationCapacity * 0.6));
            // 当前在线数：0 到 onlineCapacity 的 90% 之间随机
            int onlineCount = (int) (onlineCapacity * 0.1) + random.nextInt((int) (onlineCapacity * 0.7));

            status.setReservedCount(String.valueOf(reservedCount));
            status.setReservationCapacity(String.valueOf(reservationCapacity));
            status.setOnlineCount(String.valueOf(onlineCount));
            status.setOnlineCapacity(String.valueOf(onlineCapacity));

            enterpriseStatusMapper.insert(status);
            System.out.println("已生成企业状态: " + enterprise.getRoles()
                    + " (type=" + typeId + ")"
                    + " | 预约 " + reservedCount + "/" + reservationCapacity
                    + " | 在线 " + onlineCount + "/" + onlineCapacity);
        }
    }

    // 密码加密迁移：将所有明文密码转为 BCrypt 哈希
    @Test
    void migratePasswords() {
        int totalUpdated = 0;

        // 1. 管理员表
        List<Admin> admins = adminMapper.selectList(null);
        for (Admin admin : admins) {
            if (!admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordUtil.encode(admin.getPassword()));
                adminMapper.updateById(admin);
                totalUpdated++;
            }
        }
        System.out.println("管理员密码迁移完成: " + admins.size() + " 条");

        // 2. 企业用户表
        List<Enterprise> enterprises = enterpriseMapper.selectList(null);
        for (Enterprise e : enterprises) {
            if (!e.getPassword().startsWith("$2a$")) {
                e.setPassword(passwordUtil.encode(e.getPassword()));
                enterpriseMapper.updateById(e);
                totalUpdated++;
            }
        }
        System.out.println("企业用户密码迁移完成: " + enterprises.size() + " 条");

        // 3. 企业审核表
        List<EnterpriseConfirm> confirms = confirmMapper.selectList(null);
        for (EnterpriseConfirm c : confirms) {
            if (c.getPassword() != null && !c.getPassword().startsWith("$2a$")) {
                c.setPassword(passwordUtil.encode(c.getPassword()));
                confirmMapper.updateById(c);
                totalUpdated++;
            }
        }
        System.out.println("企业审核表密码迁移完成: " + confirms.size() + " 条");

        // 4. 普通用户表
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordUtil.encode(user.getPassword()));
                userMapper.updateById(user);
                totalUpdated++;
            }
        }
        System.out.println("普通用户密码迁移完成: " + users.size() + " 条");

        System.out.println("========================================");
        System.out.println("密码迁移全部完成！共更新 " + totalUpdated + " 条记录");
    }

    @Test
    void generateAppointments() {
        List<User> userList = userMapper.selectList(null);
        List<Enterprise> enterpriseList = enterpriseMapper.selectList(null);
        Random random = new Random();

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);
        long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();

        for (int i = 0; i < 5000; i++) {
            User user = userList.get(random.nextInt(userList.size()));
            Enterprise enterprise = enterpriseList.get(random.nextInt(enterpriseList.size()));

            // 随机日期：2026-01-01 ~ 2026-04-30
            LocalDate date = startDate.plusDays(random.nextInt((int) daysBetween + 1));

            // 随机开始时间：8:00 ~ 18:00（给结束时间留空间）
            int startHour = 8 + random.nextInt(11);  // 8~18
            int startMinute = random.nextInt(60);

            // 随机持续时长：1~3 小时，且不晚于 20:00
            int maxDurationHours = 20 - startHour;
            int durationHours = 1 + random.nextInt(Math.min(3, maxDurationHours));
            int endHour = startHour + durationHours;
            int endMinute = startMinute + random.nextInt(60);
            if (endMinute >= 60) {
                endMinute -= 60;
                endHour += 1;
            }
            if (endHour > 20 || (endHour == 20 && endMinute > 0)) {
                endHour = 20;
                endMinute = 0;
            }

            String startTime = String.format("%s %02d:%02d:00", date, startHour, startMinute);
            String endTime = String.format("%s %02d:%02d:00", date, endHour, endMinute);

            // 预约状态：1=已预约未到, 2=已到现场, 3=预约未到
            int status = 1 + random.nextInt(3);

            Appointment appointment = new Appointment();
            appointment.setUserID(user.getId());
            appointment.setEnterpriseID(enterprise.getId());
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setAppStatus(String.valueOf(status));
            appointment.setRemarks(null);

            appointmentMapper.insert(appointment);
        }
        System.out.println("已生成 5000 条预约记录");
    }
}
