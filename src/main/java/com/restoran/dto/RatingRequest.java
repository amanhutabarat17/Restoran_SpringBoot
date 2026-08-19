package com.restoran.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RatingRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long menuId;
    @NotNull @Min(1) @Max(5)
    private Integer bintang;
    private String komentar;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public Integer getBintang() { return bintang; }
    public void setBintang(Integer bintang) { this.bintang = bintang; }
    public String getKomentar() { return komentar; }
    public void setKomentar(String komentar) { this.komentar = komentar; }
}
