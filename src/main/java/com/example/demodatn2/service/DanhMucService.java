package com.example.demodatn2.service;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
// Contract cho nghiệp vụ danh mục: CRUD, thống kê và tìm kiếm có phân trang.
public interface    DanhMucService {
    //LAY DANH SACH DANH MUC
    List<DanhMuc> getAll();
    //lay danh muc theo dto
    List<DanhMucDTO> getAllDTOs();
    //LAY DANH SACH DANH MUC ACTIVE
    List<DanhMuc> getActive();
    //Lay danh muc cha (parent) - danh muc khong co danh muc cha nao
    List<DanhMuc> getParents();
    // lay danh muc theo id
    Optional<DanhMuc> getById(Integer id);
    //lay theo ma danh muc
    Optional<DanhMuc> getByMa(String ma);
    // lay danh muc con theo danh muc cha
    List<DanhMuc> getByParentId(Integer parentId);
    // luu danh muc
    DanhMuc save(DanhMuc danhMuc);
    //nhận dữ liệu DTO để lưu hoặc cập nhật danh mục, tự động map sang entity
    void saveDTO(DanhMucDTO dto);
    //xóa danh mục theo ID, nếu là danh mục cha thì cũng xóa tất cả danh mục con liên quan
    void deleteById(Integer id);
    //lay danh  muc cha theo id
 Page<DanhMuc> findByDanhMucChaIsNull(int page, int size);
 //dem so luong danh muc cha, con, active
    long countAll();
    // dem so luong danh muc cha (khong co danh muc cha nao)
    long countParents();
    // dem so luong danh muc con (co danh muc cha)
    long countChildren();
    //dem danh muc active (trangThai = "ACTIVE")
    long countActive();
    //Tim kiem các danh muc cha con theo key và co phan trang
    Page<DanhMuc> searchParents(String keyword, int page, int size);
}
