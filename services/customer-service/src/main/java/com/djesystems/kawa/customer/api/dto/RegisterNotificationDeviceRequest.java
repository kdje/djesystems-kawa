package com.djesystems.kawa.customer.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterNotificationDeviceRequest(

    @NotBlank
    String token,

    @NotBlank
    String platform

) {
}