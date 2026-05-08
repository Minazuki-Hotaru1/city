package com.example.city.controller;


import com.example.city.Utils.GetLatAndLong;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class AddressController {

    @Resource
    private GetLatAndLong getLatAndLong;


    //企业用户注册时获取接口所给的经纬度
    @GetMapping("/getAddressLatAndLong")
    public Map<String, Object> getAddressLatAndLong(@RequestParam String address){
        return getLatAndLong.getLatAndLongWithScore(address);

    }

}
