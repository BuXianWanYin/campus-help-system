package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper接口
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    
    /**
     * 使用悲观锁查询商品（防止并发）
     * @param id 商品ID
     * @return 商品信息
     */
    @Select("SELECT * FROM goods WHERE id = #{id} FOR UPDATE")
    Goods selectByIdForUpdate(Long id);
    
    /**
     * 使用乐观锁扣减库存
     * @param goodsId 商品ID
     * @param quantity 扣减数量
     * @param version 版本号
     * @return 更新行数
     */
    @Update("UPDATE goods SET stock = stock - #{quantity}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{goodsId} AND stock >= #{quantity} AND version = #{version} AND delete_flag = 0")
    int decreaseStockWithVersion(@Param("goodsId") Long goodsId, 
                                 @Param("quantity") Integer quantity, 
                                 @Param("version") Integer version);
    
    /**
     * 增加库存（取消订单时恢复库存）
     * @param goodsId 商品ID
     * @param quantity 增加数量
     * @return 更新行数
     */
    @Update("UPDATE goods SET stock = stock + #{quantity}, update_time = NOW() WHERE id = #{goodsId} AND delete_flag = 0")
    int increaseStock(@Param("goodsId") Long goodsId, @Param("quantity") Integer quantity);
    
    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 状态
     * @return 更新行数
     */
    @Update("UPDATE goods SET status = #{status}, update_time = NOW() WHERE id = #{id} AND delete_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 增加浏览量（查看详情时）
     * @param id 商品ID
     */
    @Update("UPDATE goods SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND delete_flag = 0")
    void incrementViewCount(Long id);
}

