package com.example.city.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.Utils.JwtUtil;
import com.example.city.Utils.PasswordUtil;
import com.example.city.VO.AddressVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.UserService;
import com.github.javafaker.App;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.substring;

@Service
public class UserServiceImpl implements UserService{

    private static final double KM_PER_LATITUDE_DEGREE = 111.32;
    private static final double DISTANCE_SCALE = 1000.0;

    @Resource
    private UserMapper userMapper;
    @Resource
    private GetLatAndLong getLatAndLong;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private EnterpriseStatusMapper enterpriseStatusMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RestTemplate restTemplate;
    @Value("${amap.web-service-key:}")
    private String amapWebServiceKey;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Resource
    private PasswordUtil passwordUtil;


    //普通用户注册方法
    @Override
    public Map<String, Object> userRegister(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username",data.get("username"));
        if(userMapper.selectCount(wrapper)>0){
            result.put("success", false);
            result.put("message", "该账号已有用户注册");
            return result;
        }

        try {
            User user = new User();
            user.setUsername(data.get("username").toString());
            user.setPassword(passwordUtil.encode(data.get("password").toString()));
            user.setAddress(data.get("address").toString());
            //获取用户传入的地址转变为经纬度
            Map map = getLatAndLong.getLatAndLong(data.get("address").toString());
            //纬度 横着的
            user.setLatitude(map.get("lat").toString());
            //经度 竖着的
            user.setLongitude(map.get("lng").toString());


            userMapper.insert(user);
            result.put("success", true);
            result.put("message", "注册成功，请返回登录界面进行登录");
            return result;
        }catch (Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //登录的方法
    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordUtil.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                Collections.singletonMap("userID", user.getId())
        );


        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("ID", user.getId());
        return result;
    }

    //查询所有企业以及状态，在地图上显示，并查询用户当前地址，方便做比较
    @Override
    public List<AddressVO> getAllEn() {
        List<Address> addressList = addressMapper.selectList(null);
        List<AddressVO> voList = new ArrayList<>();
        for (Address address : addressList) {
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(address, vo);
            vo.setEnId(address.getEnterpriseID());
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


    //判断用户能否进行预约的方法
    @Override
    public Map<String, Object> userReserveEnterprise(String userId, String enterpriseId, String enterpriseType) {
        Map<String, Object> result = new HashMap<>();
        //获取用户的地址信息以及企业的预约、地址信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        EnterpriseStatus enterpriseStatus = enterpriseStatusMapper.selectOne(
                new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", enterpriseId));
        List<Appointment> appointmentList = appointmentMapper.selectList(
                new QueryWrapper<Appointment>().eq("user_id", userId)
        );

        //先判断当前用户选择的企业的预约情况，如果为满，则直接返回不能预约
        if(Double.parseDouble(enterpriseStatus.getReservedCount()) >= Double.parseDouble(enterpriseStatus.getReservationCapacity())){
            result.put("success", false);
            result.put("message", "当前企业预约为满，不可再进行预约");
            return result;
        }
        //判断在线人数是否已满
        if(Double.parseDouble(enterpriseStatus.getOnlineCount()) >= Double.parseDouble(enterpriseStatus.getOnlineCapacity())){
            result.put("success", false);
            result.put("message", "当前企业在线人数已满，不推荐预约");
            return result;
        }
        //判断用户是否在今天预约了企业，如果预约了，则提示今天不能再预约企业了
        for (Appointment appointment : appointmentList) {
            LocalDate startDate = LocalDate.parse(appointment.getStartTime().substring(0, 10));
            if(startDate.isEqual(LocalDate.now())){
                result.put("success", false);
                result.put("message", "您已在今天预约了企业，不可再进行预约");
                return result;
            }
        }
        // 后判断企业拥挤情况，要是拥挤程度不严重，则可以预约
        if(Double.parseDouble(enterpriseStatus.getOnlineCount()) / Double.parseDouble(enterpriseStatus.getOnlineCapacity())  <= 0.8){
            result.put("success", true);
            return result;
        }

        //当在线的程度大于0.8时，则使用以下的系统推荐方案
        //获取所有相同属性企业地址信息,方便后续对比
        List<Enterprise> enterpriseList = enterpriseMapper.selectList(
                new QueryWrapper<Enterprise>().eq("type_id", enterpriseType));
        List<Address> addressList =  new ArrayList<>();
        for (Enterprise enterprise : enterpriseList) {
            addressList.add(addressMapper.selectOne(
                    new QueryWrapper<Address>().eq("enterprise_id", enterprise.getId())));
        }

        //获取用户离得最近的三个企业，排除用户原本选择的企业
        Map<String, Double> distance = new HashMap<>();
        for (Address address1 : addressList) {
            if (address1.getEnterpriseID().equals(enterpriseId)) {
                continue;
            }
            distance.put(address1.getEnterpriseID(), calculateStraightDistance(
                    user.getLongitude(),
                    user.getLatitude(),
                    address1.getLongitude(),
                    address1.getLatitude()
            ));
        }

        Map<String, Double> small = distance.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // 按 value 升序
                .limit(3) // 取最小的3个
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // 保持排序结果
                ));

        //分配权重，不同企业的驾车时间与拥挤情况权重不同
        double wTime = 0.5;
        double wCrowd = 0.5;
        switch (enterpriseType) {
            //医院, 就医更注重快速到达
            case  "101" -> {
                wTime = 0.7 ;
                wCrowd = 0.3;
            }
            //停车场, 车位紧张，拥挤程度很关键
            case  "102" -> {
                wTime = 0.5;
                wCrowd = 0.5;
            }
            //公园景点, 游玩体验受拥挤影响
            case  "103" -> {
                wTime = 0.4;
                wCrowd = 0.6;
            }
            //新能源充电桩, 充电时间和车位都有影响
            case  "104" -> {
                wTime = 0.6;
                wCrowd = 0.4;
            }
        }


        //通过高德地图api来获取用户到企业的距离
        //先获取用户的地址信息
        String userAddress = URLEncoder.encode(user.getLongitude() + "," + user.getLatitude(), StandardCharsets.UTF_8);
        String enAddress;
        //遍历三个数据的map，并存储到新map中
        Map<String, Object> enTime = new HashMap<>();
        for (String key : small.keySet()) {
            Address address1 = addressMapper.selectOne(new QueryWrapper<Address>().eq("enterprise_id", key));
            enAddress = URLEncoder.encode(address1.getLongitude() + "," + address1.getLatitude(), StandardCharsets.UTF_8);
            String url = "https://restapi.amap.com/v5/direction/driving?"
                    + "origin=" + userAddress
                    + "&destination=" + enAddress
                    + "&key=e052f376d6489de2f784770cf32eba4d";
            String resultJson = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resultJson);
            JsonNode paths = root.path("route").path("paths");
            int duration;
            if (paths.isArray() && paths.size() > 0) {
                duration = paths.get(0).path("cost").path("duration").asInt();
            } else {
                // 高德驾车路径获取失败时，用直线距离估算驾车时间（假设平均车速 30km/h）
                double straightKm = calculateStraightDistance(
                        user.getLongitude(), user.getLatitude(),
                        address1.getLongitude(), address1.getLatitude()
                );
                duration = (int) (straightKm / 30.0 * 3600);
                System.out.println("警告: 企业 " + key + " 高德驾车路径为空，使用直线距离估算 " + duration + "秒");
            }
            enTime.put(key, duration);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //遍历enTime MAp来计算权重, 其中场所越拥挤，驾车时间越长，Double越大。
        Map<String, Double> weight =  new HashMap<>();
        for(Map.Entry<String, Object> entry : enTime.entrySet()){
            EnterpriseStatus enterpriseStatus1 = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", entry.getKey()));
            double onlineSituation = Double.parseDouble(enterpriseStatus1.getOnlineCount()) /
                            Double.parseDouble(enterpriseStatus1.getOnlineCapacity());
            weight.put(entry.getKey(), (Double.parseDouble(String.valueOf(entry.getValue())) * wTime)
                    +
                    (onlineSituation * wCrowd));
        }

        List<String> sortedKeys = weight.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())  // 按 value 排序
                .map(Map.Entry::getKey)               // 取 key
                .collect(Collectors.toList());

        //获取三个企业的信息Map
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String key : sortedKeys) {
            Map<String, Object> map = new HashMap<>();
            EnterpriseStatus enterpriseStatus1 = enterpriseStatusMapper.selectOne(
                    new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", key)
            );
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new QueryWrapper<Enterprise>().eq("id", key)
            );
            map.put("enterpriseId", key);
            map.put("enterpriseName", enterprise.getRoles());
            map.put("enTime", enTime.get(key));
            map.put("onlineCount", enterpriseStatus1.getOnlineCount());
            map.put("onlineCapacity", enterpriseStatus1.getOnlineCapacity());
            resultList.add(map);
        }

        result.put("success", true);
        result.put("message", "当前企业较为繁忙，推荐您预约以下企业");
        result.put("enterpriseMap", resultList);
        return result;

    }

    private double calculateStraightDistance(String startLongitude, String startLatitude,
                                             String endLongitude, String endLatitude) {
        double startLon = Double.parseDouble(startLongitude);
        double startLat = Double.parseDouble(startLatitude);
        double endLon = Double.parseDouble(endLongitude);
        double endLat = Double.parseDouble(endLatitude);

        double latDiffKm = (endLat - startLat) * KM_PER_LATITUDE_DEGREE;
        double avgLatRadians = Math.toRadians((startLat + endLat) / 2);
        double lonDiffKm = (endLon - startLon) * KM_PER_LATITUDE_DEGREE * Math.cos(avgLatRadians);
        double distanceKm = Math.sqrt(Math.pow(latDiffKm, 2) + Math.pow(lonDiffKm, 2));
        return Math.round(distanceKm * DISTANCE_SCALE) / DISTANCE_SCALE;
    }

    //用户预约提交
    @Override
    public Map<String, Object> userReserveEnterpriseSuccess(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) data.get("userId");
        String enterpriseId = (String) data.get("enterpriseId");
        String date = (String) data.get("date");
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        String remarks = (String) data.get("remarks");

        String fullStartTime = date + " " + startTime;
        String fullEndTime = date + " " + endTime;

        // 判断该用户是否已在同一天预约了其他企业
        List<Appointment> appointmentList = appointmentMapper.selectList(
                new QueryWrapper<Appointment>().eq("user_id", userId)
        );
        for (Appointment appointment : appointmentList) {
            if (appointment.getStartTime() != null && appointment.getStartTime().length() >= 10) {
                LocalDate existDate = LocalDate.parse(appointment.getStartTime().substring(0, 10));
                LocalDate newDate = LocalDate.parse(date);
                if (existDate.isEqual(newDate)) {
                    result.put("success", false);
                    result.put("message", "您在该日期已有预约，请重新选择日期");
                    return result;
                }
            }
        }

        // 更新企业已预约人数
        EnterpriseStatus enterpriseStatus = enterpriseStatusMapper.selectOne(
                new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", enterpriseId)
        );
        if (enterpriseStatus != null) {
            int currentCount = Integer.parseInt(enterpriseStatus.getReservedCount());
            enterpriseStatus.setReservedCount(String.valueOf(currentCount + 1));
            enterpriseStatusMapper.updateById(enterpriseStatus);
        }

        Appointment appointment = new Appointment();
        appointment.setUserID(userId);
        appointment.setEnterpriseID(enterpriseId);
        appointment.setStartTime(fullStartTime);
        appointment.setEndTime(fullEndTime);
        appointment.setRemarks(remarks != null ? remarks : "");
        appointment.setAppStatus("1");

        try {
            appointmentMapper.insert(appointment);
            result.put("success", true);
            result.put("message", "预约成功");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //获取该用户的所有预约记录（含企业名称）
    @Override
    public List<Map<String, Object>> getUserAppointments(String userId) {
        List<Appointment> appointments = appointmentMapper.selectList(
                new QueryWrapper<Appointment>().eq("user_id", userId)
                        .orderByDesc("id"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Appointment a : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("startTime", a.getStartTime());
            map.put("endTime", a.getEndTime());
            map.put("appStatus", a.getAppStatus());
            map.put("remarks", a.getRemarks());

            Enterprise enterprise = enterpriseMapper.selectOne(
                    new QueryWrapper<Enterprise>().eq("id", a.getEnterpriseID()));
            map.put("enterpriseName", enterprise != null ? enterprise.getRoles() : "未知企业");

            result.add(map);
        }
        return result;
    }

    //获取用户个人信息
    @Override
    public Map<String, Object> getUserProfile(String userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        result.put("success", true);
        result.put("username", user.getUsername());
        result.put("address", user.getAddress());
        result.put("latitude", user.getLatitude());
        result.put("longitude", user.getLongitude());
        return result;
    }

    //更新用户地址（重新获取经纬度）
    @Override
    public Map<String, Object> updateUserAddress(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) data.get("userId");
        String newAddress = (String) data.get("address");

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        try {
            Map<String, Object> coords = getLatAndLong.getLatAndLong(newAddress);
            user.setAddress(newAddress);
            user.setLatitude(String.valueOf(coords.get("lat")));
            user.setLongitude(String.valueOf(coords.get("lng")));
            userMapper.updateById(user);

            result.put("success", true);
            result.put("message", "地址更新成功");
            result.put("latitude", user.getLatitude());
            result.put("longitude", user.getLongitude());
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    //修改用户密码
    @Override
    public Map<String, Object> updateUserPassword(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) data.get("userId");
        String oldPassword = (String) data.get("oldPassword");
        String newPassword = (String) data.get("newPassword");

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        if (!passwordUtil.matches(oldPassword, user.getPassword())) {
            result.put("success", false);
            result.put("message", "原密码错误");
            return result;
        }

        user.setPassword(passwordUtil.encode(newPassword));
        userMapper.updateById(user);
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }

    //获取用户地址信息
    @Override
    public Map<String, Object> getUserLocation(String userId){
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", user.getLatitude());
        result.put("longitude", user.getLongitude());
        result.put("address", user.getAddress());
        return result;
    }
}
