package com.example.demodatn2.service.impl;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.repository.DanhMucRepository;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.util.ProductScopeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DanhMucServiceImpl implements DanhMucService {

    private final DanhMucRepository danhMucRepository;

    @Override
    public List<DanhMuc> getAll() {
        return cloneCategoryGraph(danhMucRepository.findAll());
    }

    @Override
    public List<DanhMucDTO> getAllDTOs() {
        return buildCategoryDtoTree(danhMucRepository.findAll());
    }

    @Override
    public List<DanhMuc> getActive() {
        return cloneCategoryGraph(danhMucRepository.findByTrangThai("ACTIVE"));
    }

    @Override
    public List<DanhMuc> getParents() {
        return cloneCategoryGraph(danhMucRepository.findByDanhMucChaIsNullAndTrangThai("ACTIVE"));
    }

    @Override
    public Optional<DanhMuc> getById(Integer id) {
        return danhMucRepository.findById(id)
                .filter(ProductScopeUtil::isAllowedCategory)
                .map(this::cloneSingleCategory);
    }

    @Override
    public Optional<DanhMuc> getByMa(String ma) {
        return danhMucRepository.findByMa(ma)
                .filter(ProductScopeUtil::isAllowedCategory)
                .map(this::cloneSingleCategory);
    }

    @Override
    public List<DanhMuc> getByParentId(Integer parentId) {
        return cloneCategoryGraph(danhMucRepository.findByDanhMucCha_Id(parentId));
    }

    @Override
    @Transactional
    public DanhMuc save(DanhMuc danhMuc) {
        validateScope(danhMuc.getTen(), "Tên danh mục");
        if (danhMuc.getId() == null) {
            danhMuc.setNgayTao(LocalDateTime.now());
        }
        return danhMucRepository.save(danhMuc);
    }

    @Override
    @Transactional
    public void saveDTO(DanhMucDTO dto) {
        validateScope(dto.getTen(), "Tên danh mục");

        DanhMuc danhMuc;
        if (dto.getId() != null) {
            danhMuc = danhMucRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + dto.getId()));
        } else {
            danhMuc = new DanhMuc();
            danhMuc.setNgayTao(LocalDateTime.now());
        }

        danhMuc.setMa(dto.getMa());
        danhMuc.setTen(dto.getTen());
        danhMuc.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "ACTIVE");

        if (dto.getDanhMucChaId() != null) {
            DanhMuc cha = danhMucRepository.findById(dto.getDanhMucChaId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại: " + dto.getDanhMucChaId()));
            if (!ProductScopeUtil.isAllowedCategory(cha)) {
                throw new RuntimeException("Danh mục cha thuộc nhóm giày/dép nên không còn được sử dụng.");
            }
            danhMuc.setDanhMucCha(cha);
        } else {
            danhMuc.setDanhMucCha(null);
        }

        danhMucRepository.save(danhMuc);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        danhMucRepository.deleteById(id);
    }

    @Override
    public Page<DanhMuc> findByDanhMucChaIsNull(int page, int size) {
        List<DanhMuc> rootCategories = cloneCategoryGraph(danhMucRepository.findAll()).stream()
                .filter(dm -> dm.getDanhMucCha() == null)
                .collect(Collectors.toList());
        return paginate(rootCategories, page, size);
    }

    @Override
    public long countAll() {
        return danhMucRepository.findAll().stream()
                .filter(ProductScopeUtil::isAllowedCategory)
                .count();
    }

    @Override
    public long countParents() {
        return danhMucRepository.findAll().stream()
                .filter(dm -> dm.getDanhMucCha() == null)
                .filter(ProductScopeUtil::isAllowedCategory)
                .count();
    }

    @Override
    public long countChildren() {
        return danhMucRepository.findAll().stream()
                .filter(dm -> dm.getDanhMucCha() != null)
                .filter(ProductScopeUtil::isAllowedCategory)
                .count();
    }

    @Override
    public long countActive() {
        return danhMucRepository.findByTrangThai("ACTIVE").stream()
                .filter(ProductScopeUtil::isAllowedCategory)
                .count();
    }

    @Override
    public Page<DanhMuc> searchParents(String keyword, int page, int size) {
        List<Integer> matchedRootIds = danhMucRepository.searchParents(keyword, Pageable.unpaged()).getContent().stream()
                .filter(ProductScopeUtil::isAllowedCategory)
                .map(DanhMuc::getId)
                .collect(Collectors.toList());

        List<DanhMuc> matchedRoots = cloneCategoryGraph(danhMucRepository.findAll()).stream()
                .filter(dm -> dm.getDanhMucCha() == null && matchedRootIds.contains(dm.getId()))
                .collect(Collectors.toList());

        return paginate(matchedRoots, page, size);
    }

    private List<DanhMucDTO> buildCategoryDtoTree(List<DanhMuc> sourceCategories) {
        List<DanhMuc> allowedCategories = sourceCategories.stream()
                .filter(ProductScopeUtil::isAllowedCategory)
                .collect(Collectors.toList());

        Map<Integer, DanhMucDTO> dtoById = new LinkedHashMap<>();
        for (DanhMuc source : allowedCategories) {
            dtoById.put(source.getId(), DanhMucDTO.builder()
                    .id(source.getId())
                    .ma(source.getMa())
                    .ten(source.getTen())
                    .danhMucChaId(source.getDanhMucCha() != null ? source.getDanhMucCha().getId() : null)
                    .tenDanhMucCha(source.getDanhMucCha() != null ? source.getDanhMucCha().getTen() : null)
                    .trangThai(source.getTrangThai())
                    .ngayTao(source.getNgayTao())
                    .danhMucCon(new ArrayList<>())
                    .build());
        }

        for (DanhMuc source : allowedCategories) {
            DanhMuc parent = source.getDanhMucCha();
            if (parent != null && dtoById.containsKey(parent.getId())) {
                dtoById.get(parent.getId()).getDanhMucCon().add(dtoById.get(source.getId()));
            }
        }

        return allowedCategories.stream()
                .filter(source -> source.getDanhMucCha() == null
                        || !dtoById.containsKey(source.getDanhMucCha().getId()))
                .map(source -> dtoById.get(source.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<DanhMuc> cloneCategoryGraph(List<DanhMuc> sourceCategories) {
        List<DanhMuc> allowedCategories = sourceCategories.stream()
                .filter(ProductScopeUtil::isAllowedCategory)
                .collect(Collectors.toList());

        Map<Integer, DanhMuc> clones = new LinkedHashMap<>();
        for (DanhMuc source : allowedCategories) {
            clones.put(source.getId(), shallowClone(source));
        }

        for (DanhMuc source : allowedCategories) {
            DanhMuc clone = clones.get(source.getId());
            DanhMuc parent = source.getDanhMucCha();
            if (parent == null || !ProductScopeUtil.isAllowedCategory(parent)) {
                continue;
            }

            DanhMuc parentClone = clones.get(parent.getId());
            if (parentClone == null) {
                parentClone = shallowClone(parent);
            } else {
                parentClone.getDanhMucCon().add(clone);
            }
            clone.setDanhMucCha(parentClone);
        }

        return allowedCategories.stream()
                .map(source -> clones.get(source.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private DanhMuc cloneSingleCategory(DanhMuc source) {
        return cloneCategoryGraph(List.of(source)).stream().findFirst().orElse(null);
    }

    private DanhMuc shallowClone(DanhMuc source) {
        DanhMuc clone = new DanhMuc();
        clone.setId(source.getId());
        clone.setMa(source.getMa());
        clone.setTen(source.getTen());
        clone.setTrangThai(source.getTrangThai());
        clone.setNgayTao(source.getNgayTao());
        clone.setDanhMucCon(new ArrayList<>());
        clone.setSanPhams(new ArrayList<>());
        return clone;
    }

    private Page<DanhMuc> paginate(List<DanhMuc> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        return new PageImpl<>(items.subList(start, end), PageRequest.of(safePage, safeSize), items.size());
    }

    private void validateScope(String value, String fieldName) {
        if (ProductScopeUtil.isExcludedCategoryName(value)) {
            throw new RuntimeException(fieldName + " không được thuộc nhóm giày/dép vì website hiện chỉ bán quần áo.");
        }
    }
}
