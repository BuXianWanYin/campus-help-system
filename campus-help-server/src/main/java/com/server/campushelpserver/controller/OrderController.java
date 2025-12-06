package com.server.campushelpserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.Order;
import com.server.campushelpserver.entity.dto.OrderDTO;
import com.server.campushelpserver.entity.dto.OrderSearchDTO;
import com.server.campushelpserver.entity.dto.ShipOrderDTO;
import com.server.campushelpserver.entity.dto.UpdateOrderPriceDTO;
import com.server.campushelpserver.entity.User;
import com.server.campushelpserver.service.OrderService;
import com.server.campushelpserver.service.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单管理相关接口")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new com.server.campushelpserver.exception.BusinessException("未登录");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new com.server.campushelpserver.exception.BusinessException("用户不存在");
        }
        return user.getId();
    }
    
    @Operation(summary = "创建订单", description = "创建订单（需要实名认证）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create")
    public Result<Long> createOrder(@Parameter(description = "订单信息") @Validated @RequestBody OrderDTO dto) {
        Long userId = getCurrentUserId();
        Long id = orderService.createOrder(dto, userId);
        return Result.success("订单创建成功", id);
    }
    
    @Operation(summary = "获取我的订单列表", description = "获取当前用户的订单列表（分页）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/list")
    public Result<Page<Order>> getMyOrderList(@Parameter(description = "搜索条件") OrderSearchDTO searchDTO) {
        Long userId = getCurrentUserId();
        Page<Order> page = orderService.getMyOrderList(searchDTO, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详细信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderService.getOrderDetail(id, userId);
        return Result.success("查询成功", order);
    }
    
    @Operation(summary = "根据会话ID获取订单", description = "根据聊天会话ID获取关联的订单信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/by-session/{sessionId}")
    public Result<Order> getOrderBySessionId(@Parameter(description = "会话ID") @PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        Order order = orderService.getOrderBySessionId(sessionId, userId);
        if (order == null) {
            return Result.success("未找到关联订单", null);
        }
        return Result.success("查询成功", order);
    }
    
    @Operation(summary = "卖家改价", description = "卖家修改订单价格（仅待付款订单）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/price")
    public Result<Void> updateOrderPrice(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "价格信息") @Validated @RequestBody UpdateOrderPriceDTO dto) {
        Long sellerId = getCurrentUserId();
        orderService.updateOrderPrice(id, dto, sellerId);
        return Result.success("改价成功", null);
    }
    
    @Operation(summary = "买家付款", description = "买家支付订单")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        Long buyerId = getCurrentUserId();
        orderService.payOrder(id, buyerId);
        return Result.success("付款成功", null);
    }
    
    @Operation(summary = "卖家发货", description = "卖家发货（仅邮寄方式）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "发货信息") @Validated @RequestBody ShipOrderDTO dto) {
        Long sellerId = getCurrentUserId();
        orderService.shipOrder(id, dto, sellerId);
        return Result.success("发货成功", null);
    }
    
    @Operation(summary = "买家确认收货", description = "买家确认收货（邮寄和自提都支持）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReceipt(@Parameter(description = "订单ID") @PathVariable Long id) {
        Long buyerId = getCurrentUserId();
        orderService.confirmReceipt(id, buyerId);
        return Result.success("确认收货成功", null);
    }
    
    @Operation(summary = "取消订单", description = "取消订单（仅待付款订单）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        Long userId = getCurrentUserId();
        orderService.cancelOrder(id, userId, reason);
        return Result.success("取消订单成功", null);
    }
}

