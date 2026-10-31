package com.billing.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Business details from application.properties (prefix "shop"). Available in templates as @shopProperties. */
@Component
@ConfigurationProperties(prefix = "shop")
@Getter
@Setter
public class ShopProperties {
    private String name = "My Shop";
    private String address = "";
    private String phone = "";
    private String gstin = "";
    private String state = "Kerala";
    private int lowStockThreshold = 10;
}
