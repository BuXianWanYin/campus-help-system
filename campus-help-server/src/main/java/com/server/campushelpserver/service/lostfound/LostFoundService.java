package com.server.campushelpserver.service.lostfound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.lostfound.LostFound;
import com.server.campushelpserver.entity.lostfound.ClaimRecord;
import com.server.campushelpserver.entity.lostfound.dto.ClaimDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundSearchDTO;

/**
 * 失物招领服务接口
 */
public interface LostFoundService extends IService<LostFound> {
    
    /**
     * 发布失物
     * @param dto 失物信息
     * @param userId 用户ID
     * @return 失物ID
     */
    Long publishLostFound(LostFoundDTO dto, Long userId);
    
    /**
     * 搜索失物列表
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<LostFound> searchLostFound(LostFoundSearchDTO searchDTO);
    
    /**
     * 获取失物详情
     * @param id 失物ID
     * @return 失物信息
     */
    LostFound getLostFoundDetail(Long id);
    
    /**
     * 申请认领
     * @param dto 认领信息
     * @param userId 用户ID
     */
    void applyClaim(ClaimDTO dto, Long userId);
    
    /**
     * 确认认领
     * @param claimRecordId 认领记录ID
     * @param userId 用户ID
     */
    void confirmClaim(Long claimRecordId, Long userId);
    
    /**
     * 拒绝认领
     * @param claimRecordId 认领记录ID
     * @param reason 拒绝原因
     * @param userId 用户ID
     */
    void rejectClaim(Long claimRecordId, String reason, Long userId);
    
    /**
     * 获取当前用户发布的失物列表
     * @param searchDTO 搜索条件
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<LostFound> getMyPosts(LostFoundSearchDTO searchDTO, Long userId);
    
    /**
     * 获取认领记录列表
     * @param lostFoundId 失物ID
     * @return 认领记录列表
     */
    java.util.List<ClaimRecord> getClaimRecords(Long lostFoundId);
    
    /**
     * 编辑失物信息
     * @param id 失物ID
     * @param dto 失物信息
     * @param userId 用户ID
     */
    void updateLostFound(Long id, LostFoundDTO dto, Long userId);
    
    /**
     * 删除失物（逻辑删除）
     * @param id 失物ID
     * @param userId 用户ID
     */
    void deleteLostFound(Long id, Long userId);
    
    /**
     * 关闭失物（用户自行下架）
     * @param id 失物ID
     * @param userId 用户ID
     */
    void closeLostFound(Long id, Long userId);
    
    /**
     * 获取当前用户对某个失物的申请
     * @param lostFoundId 失物ID
     * @param userId 用户ID
     * @return 认领记录，如果不存在返回null
     */
    ClaimRecord getMyClaimRecord(Long lostFoundId, Long userId);
    
    /**
     * 更新认领申请
     * @param claimRecordId 认领记录ID
     * @param dto 认领信息（lostFoundId字段会被忽略，因为已有claimRecordId）
     * @param userId 用户ID
     */
    void updateClaimRecord(Long claimRecordId, ClaimDTO dto, Long userId);
    
    /**
     * 删除认领申请
     * @param claimRecordId 认领记录ID
     * @param userId 用户ID
     */
    void deleteClaimRecord(Long claimRecordId, Long userId);
    
    /**
     * 管理员审核失物招领
     * @param id 失物ID
     * @param auditResult 审核结果：1-通过，0-拒绝
     * @param auditReason 拒绝原因（拒绝时必填）
     * @param adminId 管理员ID
     */
    void auditLostFound(Long id, Integer auditResult, String auditReason, Long adminId);
    
    /**
     * 获取待审核的失物招领列表（管理员）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<LostFound> getPendingAuditList(LostFoundSearchDTO searchDTO);
}

