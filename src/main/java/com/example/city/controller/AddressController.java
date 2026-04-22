package com.example.city.controller;


import com.example.city.Utils.GetLatAndLong;
import com.example.city.entity.Address;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class AddressController {


    //企业用户注册时获取接口所给的经纬度
    @GetMapping("/getAddressLatAndLong")
    public Map getAddressLatAndLong(@RequestParam String address){
        GetLatAndLong getLatAndLong = new GetLatAndLong();
        return getLatAndLong.getLatAndLong(address);
    }

}
