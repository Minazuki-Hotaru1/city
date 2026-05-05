package com.example.city.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.city.Utils.GetLatAndLong;
import com.example.city.Utils.JwtUtil;
import com.example.city.VO.AddressVO;
import com.example.city.entity.*;
import com.example.city.mapper.*;
import com.example.city.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{


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
            user.setPassword(data.get("password").toString());
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
        wrapper.eq("username", username)
                .eq("password", password);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
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

    //查询所有企业以及状态，在地图上显示
    @Override
    public List<AddressVO> getAllEn() {
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


    //创建一个用于这个方法内判断经纬度的对象


    //判断用户能否进行预约的方法
    @Override
    public Map<String, Object> userReserveEnterprise(String userId, String enterpriseId, String enterpriseType) {
        Map<String, Object> result = new HashMap<>();
        //获取用户的地址信息以及企业的预约、地址信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        EnterpriseStatus enterpriseStatus = enterpriseStatusMapper.selectOne(
                new QueryWrapper<EnterpriseStatus>().eq("enterprise_id", enterpriseId));
        Address address =  addressMapper.selectOne(new QueryWrapper<Address>().eq("enterprise_id", enterpriseId));

        //先判断当前用户选择的企业的预约情况，如果为满，则直接返回不能预约
        if(enterpriseStatus.getReservedCount().equals(enterpriseStatus.getReservationCapacity())){
            result.put("success", false);
            result.put("message", "当前企业预约为满，不可再进行预约");
            return result;
        }
        // 后判断企业拥挤情况，要是拥挤程度不严重，则可以预约
        if(Double.parseDouble(enterpriseStatus.getOnlineCount()) / Double.parseDouble(enterpriseStatus.getOnlineCapacity())  <= 0.8){
            result.put("success", true);
            return  result;
        }

        //当预约的程度大于0.8时，则使用以下的系统推荐方案

        //获取所有相同属性企业地址信息,方便后续对比
        List<Enterprise> enterpriseList = enterpriseMapper.selectList(
                new QueryWrapper<Enterprise>().eq("type_id", enterpriseType));
        List<Address> addressList =  new ArrayList<>();
        for (Enterprise enterprise : enterpriseList) {
            addressList.add(addressMapper.selectOne(
                    new QueryWrapper<Address>().eq("enterprise_id", enterprise.getId())));
        }

        //获取用户离得最近的三个企业，获取方式为直接对比经纬度相对位置
        Map<String, Double> distance = new HashMap<>();
        for (Address address1 : addressList) {
            double lonDiff = Math.abs(
                    Double.parseDouble(address1.getLongitude()) - Double.parseDouble(user.getLongitude())
            );
            double latDiff = Math.abs(
                    Double.parseDouble(address1.getLatitude()) - Double.parseDouble(user.getLatitude())
            );
            // 避免除0
            if (latDiff == 0) {
                continue;
            }
            distance.put(address1.getEnterpriseID(), lonDiff / latDiff);
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

        //通过高德地图api来获取用户到企业的距离
        //先获取用户的地址信息
        String userAddress = URLEncoder.encode(user.getLongitude() + "," + user.getLatitude(), StandardCharsets.UTF_8);


        https://restapi.amap.com/v5/direction/driving?

        return Map.of();
    }

    //企业用户预约条件成功的方法
    public Map<String, Object> userReserveEnterpriseSuccess(String userId, String enterpriseId) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));

        return result;
    }
}
