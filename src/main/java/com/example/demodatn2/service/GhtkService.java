package com.example.demodatn2.service;

import com.example.demodatn2.dto.GhtkFeeIdRequest;
import com.example.demodatn2.dto.GhtkFeeRequest;
import com.example.demodatn2.dto.GhtkFeeResponse;
import com.example.demodatn2.util.AddressNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GhtkService {
    @Value("${ghtk.base-url}")
    private String baseUrl;

    @Value("${ghtk.token}")
    private String token;

    @Value("${ghtk.partner-code}")
    private String partnerCode;

    @Value("${ghtk.pick-province}")
    private String pickProvince;

    @Value("${ghtk.pick-district}")
    private String pickDistrict;

    @Value("${ghtk.pick-ward}")
    private String pickWard;

    @Value("${ghtk.pick-address}")
    private String pickAddress;

    @Value("${ghtk.pick-address-id:}")
    private String pickAddressId;

    private final RestTemplate restTemplate;
    private final DvhcvnLocationService locationService;

    public GhtkService(DvhcvnLocationService locationService) {
        this.locationService = locationService;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(0,
                new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public GhtkFeeResponse calculateFeeFromIds(GhtkFeeIdRequest input) {
        if (input == null) {
            throw new IllegalArgumentException("Input is required");
        }
        if (input.getWeight() == null || input.getWeight() <= 0) {
            throw new IllegalArgumentException("weight is required");
        }

        DvhcvnLocationService.LocationNames names = locationService.resolveFromIds(
                input.getProvinceId(),
                input.getDistrictId(),
                input.getWardId()
        );

        GhtkFeeRequest request = new GhtkFeeRequest();
        request.setProvince(AddressNormalizer.normalize(names.province()));
        request.setDistrict(AddressNormalizer.normalize(names.district()));
        request.setWard(AddressNormalizer.normalize(names.ward()));
        request.setAddress(input.getAddress());
        request.setWeight(input.getWeight());
        request.setValue(input.getValue());
        request.setTransport(input.getTransport());

        return calculateFeeWithDefaultPick(request);
    }

    public GhtkFeeResponse calculateFeeWithDefaultPick(GhtkFeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        return calculateFee(request);
    }

    public GhtkFeeResponse calculateFee(GhtkFeeRequest request) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("GHTK token is not configured");
        }
        if (partnerCode == null || partnerCode.isBlank()) {
            throw new IllegalStateException("GHTK partner code is not configured");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        System.out.println("TOKEN = " + token);
        System.out.println("SOURCE = " + partnerCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.set("X-Client-Source", partnerCode);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

        System.out.println("province=" + request.getProvince());
        System.out.println("district=" + request.getDistrict());
        System.out.println("ward=" + request.getWard());
        System.out.println("weight=" + request.getWeight());

        Map<String, Object> body = new HashMap<>();
        String normalizedPickProvince = AddressNormalizer.normalize(pickProvince);
        String normalizedPickDistrict = AddressNormalizer.normalize(pickDistrict);
        String normalizedPickWard = AddressNormalizer.normalize(pickWard);
        putIfNotBlank(body, "pick_province", normalizedPickProvince);
        putIfNotBlank(body, "pick_district", normalizedPickDistrict);
        putIfNotBlank(body, "pick_ward", normalizedPickWard);
        putIfNotBlank(body, "pick_address", pickAddress);
        putIfNotBlank(body, "pick_address_id", pickAddressId);

        putIfNotBlank(body, "province", request.getProvince());
        putIfNotBlank(body, "district", request.getDistrict());
        putIfNotBlank(body, "ward", request.getWard());

        if (request.getWeight() != null) {
            body.put("weight", request.getWeight());
        }
        if (request.getValue() != null) {
            body.put("value", request.getValue());
        } else {
            body.put("value", 0);
        }
        System.out.println("GHTK FEE BODY = " + body);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<GhtkFeeResponse> response = restTemplate.exchange(
                baseUrl + "/services/shipment/fee",
                HttpMethod.POST,
                entity,
                GhtkFeeResponse.class
        );

        return response.getBody();
    }

    private void putIfNotBlank(Map<String, Object> body, String key, String value) {
        if (value != null && !value.isBlank()) {
            body.put(key, value.trim());
        }
    }

}
