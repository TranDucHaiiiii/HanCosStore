package com.example.demodatn2.service;

import com.example.demodatn2.util.AddressNormalizer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DvhcvnLocationService {
    private final List<Province> provinces;

    public DvhcvnLocationService(ObjectMapper objectMapper) {
        this.provinces = loadLocations(objectMapper).getData();
    }

    public LocationNames resolveFromIds(String provinceId, String districtId, String wardId) {
        if (provinceId == null || provinceId.isBlank()) {
            throw new IllegalArgumentException("provinceId is required");
        }
        if (districtId == null || districtId.isBlank()) {
            throw new IllegalArgumentException("districtId is required");
        }

        Province province = provinces.stream()
                .filter(p -> provinceId.equals(p.getLevel1Id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Province not found: " + provinceId));

        District district = province.getLevel2s().stream()
                .filter(d -> districtId.equals(d.getLevel2Id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));

        String wardName = null;
        if (wardId != null && !wardId.isBlank() && district.getLevel3s() != null) {
            Ward ward = district.getLevel3s().stream()
                    .filter(w -> wardId.equals(w.getLevel3Id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + wardId));
            wardName = AddressNormalizer.normalize(ward.getName());
        }

        return new LocationNames(
                AddressNormalizer.normalize(province.getName()),
                AddressNormalizer.normalize(district.getName()),
                wardName
        );
    }

    public LocationNames resolveFromNames(String provinceName, String districtName, String wardName) {
        String provinceKey = AddressNormalizer.normalizeForCompare(provinceName);
        String districtKey = AddressNormalizer.normalizeForCompare(districtName);
        String wardKey = AddressNormalizer.normalizeForCompare(wardName);

        if (provinceKey.isBlank()) {
            throw new IllegalArgumentException("province is required");
        }
        if (districtKey.isBlank()) {
            throw new IllegalArgumentException("district is required");
        }
        if (wardKey.isBlank()) {
            throw new IllegalArgumentException("ward is required");
        }

        Province province = provinces.stream()
                .filter(p -> AddressNormalizer.normalizeForCompare(p.getName()).equals(provinceKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Province not found: " + provinceName));

        District district = province.getLevel2s().stream()
                .filter(d -> AddressNormalizer.normalizeForCompare(d.getName()).equals(districtKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtName));

        if (district.getLevel3s() == null) {
            throw new IllegalArgumentException("Ward not found: " + wardName);
        }

        Ward ward = district.getLevel3s().stream()
                .filter(w -> AddressNormalizer.normalizeForCompare(w.getName()).equals(wardKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ward not found: " + wardName));

        return new LocationNames(
                AddressNormalizer.normalize(province.getName()),
                AddressNormalizer.normalize(district.getName()),
                AddressNormalizer.normalize(ward.getName())
        );
    }

    private DvhcvnData loadLocations(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("static/data/dvhcvn.json");
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, DvhcvnData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dvhcvn.json", e);
        }
    }

    @Data
    private static class DvhcvnData {
        private List<Province> data;
    }

    @Data
    private static class Province {
        @JsonProperty("level1_id")
        private String level1Id;
        private String name;
        private List<District> level2s;
    }

    @Data
    private static class District {
        @JsonProperty("level2_id")
        private String level2Id;
        private String name;
        private List<Ward> level3s;
    }

    @Data
    private static class Ward {
        @JsonProperty("level3_id")
        private String level3Id;
        private String name;
    }

    public record LocationNames(String province, String district, String ward) {
    }
}
