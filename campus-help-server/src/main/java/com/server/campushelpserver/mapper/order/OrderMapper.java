package com.server.campushelpserver.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.order.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo} AND delete_flag = 0")
    Order selectByOrderNo(String orderNo);
}

