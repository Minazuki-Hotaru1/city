package com.example.city.Utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class GetLatAndLong {

    private static final long RETRY_INTERVAL_MILLIS = 500L;

    @Resource
    private RestTemplate restTemplate;
    @Value("${legacy-geocode.id:}")
    private String legacyGeocodeId;
    @Value("${legacy-geocode.key:}")
    private String legacyGeocodeKey;

    // Use the legacy geocode API for all address-to-coordinate calls.
    public Map<String, Object> getLatAndLong(String address) {
        return getLegacyGeocodeWithRetry(address);
    }

    public Map<String, Object> getLatAndLongWithScore(String address) {
        return getLegacyGeocodeWithRetry(address);
    }

    private Map<String, Object> getLegacyGeocodeWithRetry(String address) {
        if (legacyGeocodeId == null || legacyGeocodeId.isBlank()
                || legacyGeocodeKey == null || legacyGeocodeKey.isBlank()) {
            throw new IllegalStateException("Legacy geocode id or key is not configured");
        }

        String encodeAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://cn.apihz.cn/api/other/jwjuhe.php?"
                + "id=" + legacyGeocodeId
                + "&key=" + legacyGeocodeKey
                + "&address=" + encodeAddress;

        while (true) {
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            if (hasLatAndLng(result)) {
                return result;
            }

            sleepBeforeRetry();
        }
    }

    private boolean hasLatAndLng(Map<String, Object> result) {
        if (result == null) {
            return false;
        }

        return hasText(result.get("lat")) && hasText(result.get("lng"));
    }

    private boolean hasText(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry legacy geocode API", e);
        }
    }
}
