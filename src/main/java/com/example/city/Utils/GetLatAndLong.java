package com.example.city.Utils;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GetLatAndLong {

    @Resource
    private RestTemplate restTemplate;

    //调用获取经纬度的接口
    public Map getLatAndLong(String address){
        String encodeAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://cn.apihz.cn/api/other/jwjuhe.php?id=10015738"
                + "&key=b34a527f8ba8f034f2490a7b6b365056"
                + "&address=" + encodeAddress;
        return restTemplate.getForObject(url, Map.class);
    }
}
