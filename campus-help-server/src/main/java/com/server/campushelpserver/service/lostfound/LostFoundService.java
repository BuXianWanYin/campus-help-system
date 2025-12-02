package com.server.campushelpserver.service.lostfound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.lostfound.LostFound;
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
}

