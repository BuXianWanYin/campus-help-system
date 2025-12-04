package com.server.campushelpserver.service.goods;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.goods.dto.GoodsDTO;
import com.server.campushelpserver.entity.goods.dto.GoodsSearchDTO;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {
    
    /**
     * 发布商品
     * @param dto 商品信息
     * @param userId 用户ID
     * @return 商品ID
     */
    Long publishGoods(GoodsDTO dto, Long userId);
    
    /**
     * 搜索商品列表
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<Goods> searchGoods(GoodsSearchDTO searchDTO);
    
    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品信息
     */
    Goods getGoodsDetail(Long id);
    
    /**
     * 编辑商品信息
     * @param id 商品ID
     * @param dto 商品信息
     * @param userId 用户ID
     */
    void updateGoods(Long id, GoodsDTO dto, Long userId);
    
    /**
     * 删除商品（逻辑删除）
     * @param id 商品ID
     * @param userId 用户ID
     */
    void deleteGoods(Long id, Long userId);
    
    /**
     * 下架商品（用户自行下架）
     * @param id 商品ID
     * @param userId 用户ID
     */
    void offshelfGoods(Long id, Long userId);
    
    /**
     * 重新上架商品
     * @param id 商品ID
     * @param userId 用户ID
     */
    void reshelfGoods(Long id, Long userId);
    
    /**
     * 获取当前用户发布的商品列表
     * @param searchDTO 搜索条件
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<Goods> getMyGoodsList(GoodsSearchDTO searchDTO, Long userId);
    
    /**
     * 管理员审核商品
     * @param id 商品ID
     * @param auditResult 审核结果：1-通过，0-拒绝
     * @param auditReason 拒绝原因（拒绝时必填）
     * @param adminId 管理员ID
     */
    void auditGoods(Long id, Integer auditResult, String auditReason, Long adminId);
    
    /**
     * 获取待审核的商品列表（管理员）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<Goods> getPendingAuditList(GoodsSearchDTO searchDTO);
}

