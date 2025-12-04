package com.server.campushelpserver.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.order.Order;
import com.server.campushelpserver.entity.order.dto.OrderDTO;
import com.server.campushelpserver.entity.order.dto.OrderSearchDTO;
import com.server.campushelpserver.entity.order.dto.ShipOrderDTO;
import com.server.campushelpserver.entity.order.dto.UpdateOrderPriceDTO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     * @param dto 订单信息
     * @param userId 用户ID
     * @return 订单ID
     */
    Long createOrder(OrderDTO dto, Long userId);
    
    /**
     * 获取我的订单列表
     * @param searchDTO 搜索条件
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<Order> getMyOrderList(OrderSearchDTO searchDTO, Long userId);
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    Order getOrderDetail(Long orderId, Long userId);
    
    /**
     * 卖家改价
     * @param orderId 订单ID
     * @param dto 价格信息
     * @param sellerId 卖家ID
     */
    void updateOrderPrice(Long orderId, UpdateOrderPriceDTO dto, Long sellerId);
    
    /**
     * 买家付款
     * @param orderId 订单ID
     * @param buyerId 买家ID
     */
    void payOrder(Long orderId, Long buyerId);
    
    /**
     * 卖家发货（邮寄方式）
     * @param orderId 订单ID
     * @param dto 发货信息
     * @param sellerId 卖家ID
     */
    void shipOrder(Long orderId, ShipOrderDTO dto, Long sellerId);
    
    /**
     * 买家确认收货
     * @param orderId 订单ID
     * @param buyerId 买家ID
     */
    void confirmReceipt(Long orderId, Long buyerId);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     */
    void cancelOrder(Long orderId, Long userId, String reason);
    
    /**
     * 根据会话ID获取订单信息
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 订单信息，如果不存在则返回null
     */
    Order getOrderBySessionId(Long sessionId, Long userId);
}

