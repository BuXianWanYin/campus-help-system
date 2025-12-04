package com.server.campushelpserver.controller.address;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.address.Address;
import com.server.campushelpserver.entity.address.dto.AddressDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.address.AddressService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址管理控制器
 */
@RestController
@RequestMapping("/api/address")
@Tag(name = "收货地址管理", description = "收货地址管理相关接口")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
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
    
    @Operation(summary = "获取收货地址列表", description = "获取当前用户的收货地址列表")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/list")
    public Result<List<Address>> getAddressList() {
        Long userId = getCurrentUserId();
        List<Address> list = addressService.getAddressList(userId);
        return Result.success("查询成功", list);
    }
    
    @Operation(summary = "添加收货地址", description = "添加收货地址（最多10个）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/add")
    public Result<Long> addAddress(@Parameter(description = "地址信息") @Validated @RequestBody AddressDTO dto) {
        Long userId = getCurrentUserId();
        Long id = addressService.addAddress(dto, userId);
        return Result.success("添加成功", id);
    }
    
    @Operation(summary = "更新收货地址", description = "更新收货地址信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> updateAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            @Parameter(description = "地址信息") @Validated @RequestBody AddressDTO dto) {
        Long userId = getCurrentUserId();
        addressService.updateAddress(id, dto, userId);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除收货地址", description = "删除收货地址（逻辑删除）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@Parameter(description = "地址ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressService.deleteAddress(id, userId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "设置默认地址", description = "设置默认收货地址")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@Parameter(description = "地址ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressService.setDefaultAddress(id, userId);
        return Result.success("设置成功", null);
    }
}

