package com.server.campushelpserver.service.impl.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.address.Address;
import com.server.campushelpserver.entity.chat.dto.CreateSessionDTO;
import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.order.Order;
import com.server.campushelpserver.entity.order.dto.OrderDTO;
import com.server.campushelpserver.entity.order.dto.OrderSearchDTO;
import com.server.campushelpserver.entity.order.dto.ShipOrderDTO;
import com.server.campushelpserver.entity.order.dto.UpdateOrderPriceDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.address.AddressMapper;
import com.server.campushelpserver.mapper.goods.GoodsMapper;
import com.server.campushelpserver.mapper.order.OrderMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.chat.ChatSessionService;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.service.message.EmailService;
import com.server.campushelpserver.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderDTO dto, Long userId) {
        // 1. 验证实名认证
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getIsVerified() == null || user.getIsVerified() != 1) {
            throw new BusinessException("该功能需要实名认证，请先完成实名认证");
        }
        
        // 2. 验证购买数量
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("购买数量必须大于0");
        }
        
        // 3. 查询商品（使用悲观锁，防止并发）
        Goods goods = goodsMapper.selectByIdForUpdate(dto.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 4. 验证商品状态
        if (!"ON_SALE".equals(goods.getStatus())) {
            throw new BusinessException("商品已下架或已售出");
        }
        
        // 5. 不能购买自己的商品
        if (goods.getUserId().equals(userId)) {
            throw new BusinessException("不能购买自己的商品");
        }
        
        // 6. 检查库存（防止超卖）
        if (goods.getStock() < dto.getQuantity()) {
            throw new BusinessException("库存不足，当前库存：" + goods.getStock() + "件");
        }
        
        // 7. 使用乐观锁扣减库存
        int rows = goodsMapper.decreaseStockWithVersion(
            dto.getGoodsId(),
            dto.getQuantity(),
            goods.getVersion()
        );
        
        if (rows == 0) {
            throw new BusinessException("库存不足或商品已被其他买家购买，请刷新后重试");
        }
        
        // 8. 如果库存为0，更新商品状态为已售完
        Goods updatedGoods = goodsMapper.selectById(dto.getGoodsId());
        if (updatedGoods.getStock() == 0) {
            goodsMapper.updateStatus(dto.getGoodsId(), "SOLD_OUT");
        }
        
        // 9. 生成唯一订单号
        String orderNo = generateOrderNo(userId);
        
        // 10. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setGoodsId(dto.getGoodsId());
        order.setBuyerId(userId);
        order.setSellerId(goods.getUserId());
        order.setQuantity(dto.getQuantity());
        order.setPrice(goods.getCurrentPrice());
        order.setTotalAmount(goods.getCurrentPrice().multiply(new BigDecimal(dto.getQuantity())));
        order.setTradeMethod(dto.getTradeMethod());
        
        // 邮寄方式需要选择收货地址
        if ("MAIL".equals(dto.getTradeMethod())) {
            if (dto.getAddressId() == null) {
                throw new BusinessException("邮寄方式必须选择收货地址");
            }
            
            Address address = addressMapper.selectById(dto.getAddressId());
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException("收货地址不存在或无权使用");
            }
            
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.getFullAddress());
            order.setShippingFee(goods.getShippingFee() != null ? goods.getShippingFee() : BigDecimal.ZERO);
        }
        
        // 自提方式使用商品的自提地点
        if ("FACE_TO_FACE".equals(dto.getTradeMethod())) {
            order.setPickupLocation(goods.getTradeLocation());
        }
        
        // 设置初始订单状态
        order.setStatus("PENDING_PAYMENT");
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        
        // 11. 创建或获取私信会话
        CreateSessionDTO sessionDTO = new CreateSessionDTO();
        sessionDTO.setTargetUserId(goods.getUserId());
        sessionDTO.setRelatedType("GOODS");
        sessionDTO.setRelatedId(goods.getId());
        Long sessionId = chatSessionService.createOrGetSession(sessionDTO, userId);
        
        // 12. 关联订单和会话
        order.setSessionId(sessionId);
        orderMapper.updateById(order);
        
        // 13. 发送通知给卖家
        systemMessageService.sendMessage(
            goods.getUserId(),
            "ORDER_CREATED",
            "收到新订单",
            "您的商品《" + goods.getTitle() + "》被购买了，订单号：" + orderNo,
            "ORDER",
            order.getId()
        );
        
        // 14. 异步发送邮件通知给卖家
        try {
            User seller = userMapper.selectById(goods.getUserId());
            if (seller != null && seller.getEmail() != null) {
                String sellerNickname = seller.getNickname() != null ? seller.getNickname() : "用户";
                emailService.sendOrderCreatedEmailAsync(
                    seller.getEmail(),
                    sellerNickname,
                    goods.getTitle(),
                    orderNo
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响订单创建，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
        
        return order.getId();
    }
    
    @Override
    public Page<Order> getMyOrderList(OrderSearchDTO searchDTO, Long userId) {
        Page<Order> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        // 根据角色筛选
        if ("BUYER".equals(searchDTO.getRole())) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("SELLER".equals(searchDTO.getRole())) {
            wrapper.eq(Order::getSellerId, userId);
        } else {
            // ALL：查询作为买家或卖家的订单
            wrapper.and(w -> w.eq(Order::getBuyerId, userId)
                             .or()
                             .eq(Order::getSellerId, userId));
        }
        
        // 状态筛选
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(Order::getStatus, searchDTO.getStatus());
        }
        
        // 交易方式筛选
        if (StringUtils.hasText(searchDTO.getTradeMethod())) {
            wrapper.eq(Order::getTradeMethod, searchDTO.getTradeMethod());
        }
        
        // 关键词搜索（订单号、商品标题）
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(Order::getOrderNo, searchDTO.getKeyword())
                             .or()
                             .exists("SELECT 1 FROM goods g WHERE g.id = order.goods_id AND g.title LIKE {0}", 
                                     "%" + searchDTO.getKeyword() + "%"));
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);
        
        // 填充商品和用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (Order order : resultPage.getRecords()) {
                fillOrderInfo(order, userId);
            }
        }
        
        return resultPage;
    }
    
    @Override
    public Order getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证权限（只有买家或卖家可以查看）
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }
        
        fillOrderInfo(order, userId);
        
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPrice(Long orderId, UpdateOrderPriceDTO dto, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权修改此订单");
        }
        
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("只有待付款订单才能改价");
        }
        
        if (dto.getNewPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("价格必须大于0");
        }
        
        // 更新订单价格和总金额
        order.setPrice(dto.getNewPrice());
        order.setTotalAmount(dto.getNewPrice().multiply(new BigDecimal(order.getQuantity())));
        order.setPriceUpdateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送通知给买家
        systemMessageService.sendMessage(
            order.getBuyerId(),
            "ORDER_PRICE_UPDATED",
            "订单价格已修改",
            "订单号：" + order.getOrderNo() + " 的价格已修改为：" + dto.getNewPrice() + "元",
            "ORDER",
            orderId
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，无法付款");
        }
        
        // 根据交易方式设置订单状态
        if ("MAIL".equals(order.getTradeMethod())) {
            order.setStatus("PAID");
            systemMessageService.sendMessage(
                order.getSellerId(),
                "ORDER_PAID",
                "订单已付款",
                "订单号：" + order.getOrderNo() + " 已付款，请及时发货",
                "ORDER",
                orderId
            );
        } else if ("FACE_TO_FACE".equals(order.getTradeMethod())) {
            // 自提方式：付款后直接进入待自提状态（无需发货）
            order.setStatus("PENDING_PICKUP");
            systemMessageService.sendMessage(
                order.getSellerId(),
                "ORDER_PAID",
                "订单已付款",
                "订单号：" + order.getOrderNo() + " 已付款（自提方式，无需发货），请等待买家自提",
                "ORDER",
                orderId
            );
        }
        
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, ShipOrderDTO dto, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，无法发货");
        }
        
        if (!"MAIL".equals(order.getTradeMethod())) {
            throw new BusinessException("该订单不是邮寄方式，无法发货");
        }
        
        if (!StringUtils.hasText(dto.getTrackingNumber())) {
            throw new BusinessException("快递单号不能为空");
        }
        
        // 更新订单状态和物流信息
        order.setStatus("SHIPPED");
        order.setTrackingNumber(dto.getTrackingNumber().trim());
        order.setLogisticsCompany(StringUtils.hasText(dto.getLogisticsCompany()) ? dto.getLogisticsCompany().trim() : null);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送通知给买家
        systemMessageService.sendMessage(
            order.getBuyerId(),
            "ORDER_SHIPPED",
            "订单已发货",
            "订单号：" + order.getOrderNo() + " 已发货，快递单号：" + dto.getTrackingNumber(),
            "ORDER",
            orderId
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 邮寄方式：需要已发货状态
        // 自提方式：需要待自提状态（付款后自动进入）
        if ("MAIL".equals(order.getTradeMethod())) {
            if (!"SHIPPED".equals(order.getStatus())) {
                throw new BusinessException("订单状态不正确，无法确认收货（邮寄方式需要已发货）");
            }
        } else if ("FACE_TO_FACE".equals(order.getTradeMethod())) {
            if (!"PENDING_PICKUP".equals(order.getStatus())) {
                throw new BusinessException("订单状态不正确，无法确认收货（自提方式需要待自提状态）");
            }
        } else {
            throw new BusinessException("订单交易方式不正确");
        }
        
        // 更新订单状态
        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送通知给卖家
        systemMessageService.sendMessage(
            order.getSellerId(),
            "ORDER_COMPLETED",
            "订单已完成",
            "订单号：" + order.getOrderNo() + " 买家已确认收货，订单完成",
            "ORDER",
            orderId
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 只有买家或卖家可以取消订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 只有待付款订单可以取消
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("只有待付款订单可以取消");
        }
        
        // 恢复库存
        goodsMapper.increaseStock(order.getGoodsId(), order.getQuantity());
        
        // 如果商品状态是已售完，恢复为在售
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if ("SOLD_OUT".equals(goods.getStatus())) {
            goodsMapper.updateStatus(order.getGoodsId(), "ON_SALE");
        }
        
        // 更新订单状态
        order.setStatus("CANCELLED");
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送通知给对方
        Long otherUserId = order.getBuyerId().equals(userId) ? order.getSellerId() : order.getBuyerId();
        systemMessageService.sendMessage(
            otherUserId,
            "ORDER_CANCELLED",
            "订单已取消",
            "订单号：" + order.getOrderNo() + " 已取消，原因：" + reason,
            "ORDER",
            orderId
        );
    }
    
    @Override
    public Order getOrderBySessionId(Long sessionId, Long userId) {
        if (sessionId == null) {
            return null;
        }
        
        // 根据sessionId查询订单（可能有多个订单，取最新的）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSessionId, sessionId)
               .eq(Order::getDeleteFlag, 0)
               .orderByDesc(Order::getCreateTime)
               .last("LIMIT 1");
        
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            return null;
        }
        
        // 验证权限（只有买家或卖家可以查看）
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return null;
        }
        
        // 填充订单信息
        fillOrderInfo(order, userId);
        
        return order;
    }
    
    /**
     * 生成唯一订单号（使用时间戳）
     * 格式：GO + 时间戳（13位毫秒） + 用户ID后2位
     */
    private String generateOrderNo(Long userId) {
        long timestamp = System.currentTimeMillis();
        String userIdSuffix = String.format("%02d", userId % 100);
        return "GO" + timestamp + userIdSuffix;
    }
    
    /**
     * 填充订单信息（商品、买家、卖家）
     */
    private void fillOrderInfo(Order order, Long currentUserId) {
        // 填充商品信息
        if (order.getGoodsId() != null) {
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            order.setGoods(goods);
        }
        
        // 填充买家信息
        if (order.getBuyerId() != null) {
            User buyer = userMapper.selectById(order.getBuyerId());
            if (buyer != null) {
                User simpleBuyer = new User();
                simpleBuyer.setId(buyer.getId());
                simpleBuyer.setNickname(buyer.getNickname());
                simpleBuyer.setAvatar(buyer.getAvatar());
                order.setBuyer(simpleBuyer);
            }
        }
        
        // 填充卖家信息
        if (order.getSellerId() != null) {
            User seller = userMapper.selectById(order.getSellerId());
            if (seller != null) {
                User simpleSeller = new User();
                simpleSeller.setId(seller.getId());
                simpleSeller.setNickname(seller.getNickname());
                simpleSeller.setAvatar(seller.getAvatar());
                order.setSeller(simpleSeller);
            }
        }
    }
}

