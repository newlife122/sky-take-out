package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {
    @NotNull(message = "订单id不能为空")
    private Long id;

    //订单拒绝原因
    private String rejectionReason;

}
