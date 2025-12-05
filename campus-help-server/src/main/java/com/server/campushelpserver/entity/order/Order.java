package com.server.campushelpserver.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.user.User;

/**
 * 订单实体类
 */
@Data
@TableName("`order`")
@Schema(description = "订单实体")
public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;
    
    @TableField("order_no")
    @Schema(description = "唯一订单号（格式：GO+时间戳13位+用户ID后2位）")
    private String orderNo;
    
    @TableField("goods_id")
    @Schema(description = "商品ID")
    private Long goodsId;
    
    @TableField("session_id")
    @Schema(description = "关联聊天会话ID")
    private Long sessionId;
    
    @TableField("buyer_id")
    @Schema(description = "买家ID")
    private Long buyerId;
    
    @TableField("seller_id")
    @Schema(description = "卖家ID")
    private Long sellerId;
    
    @TableField("quantity")
    @Schema(description = "购买数量")
    private Integer quantity;
    
    @TableField("price")
    @Schema(description = "单价")
    private BigDecimal price;
    
    @TableField("total_amount")
    @Schema(description = "订单总金额（单价×数量）")
    private BigDecimal totalAmount;
    
    @TableField("shipping_fee")
    @Schema(description = "邮寄费用（邮寄时）")
    private BigDecimal shippingFee;
    
    @TableField("trade_method")
    @Schema(description = "交易方式：FACE_TO_FACE-自提，MAIL-邮寄")
    private String tradeMethod;
    
    @TableField("receiver_name")
    @Schema(description = "收货人姓名")
    private String receiverName;
    
    @TableField("receiver_phone")
    @Schema(description = "收货人电话")
    private String receiverPhone;
    
    @TableField("receiver_address")
    @Schema(description = "收货地址")
    private String receiverAddress;
    
    @TableField("pickup_location")
    @Schema(description = "自提地点（自提方式时使用）")
    private String pickupLocation;
    
    @TableField("tracking_number")
    @Schema(description = "物流单号")
    private String trackingNumber;
    
    @TableField("logistics_company")
    @Schema(description = "物流公司")
    private String logisticsCompany;
    
    @TableField("ship_time")
    @Schema(description = "发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipTime;
    
    @TableField("status")
    @Schema(description = "订单状态：PENDING_PAYMENT-待付款，PAID-已付款，SHIPPED-已发货，PENDING_PICKUP-待自提，COMPLETED-已完成，CANCELLED-已取消，REFUNDED-已退款")
    private String status;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("pay_time")
    @Schema(description = "付款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    
    @TableField("complete_time")
    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    
    @TableField("cancel_time")
    @Schema(description = "取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    
    @TableField("cancel_reason")
    @Schema(description = "取消原因")
    private String cancelReason;
    
    @TableField("price_update_time")
    @Schema(description = "改价时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime priceUpdateTime;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "商品信息（非数据库字段）")
    private Goods goods;
    
    @TableField(exist = false)
    @Schema(description = "买家信息（非数据库字段）")
    private User buyer;
    
    @TableField(exist = false)
    @Schema(description = "卖家信息（非数据库字段）")
    private User seller;
}

