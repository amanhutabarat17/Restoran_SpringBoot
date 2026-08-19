package com.restoran.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateOrderRequest {
    private String noMeja; // diisi jika order lewat scan QR meja

    @NotEmpty(message = "Pesanan tidak boleh kosong")
    @Valid
    private List<OrderItemRequest> items;

    private String kodePromo;

    public String getNoMeja() { return noMeja; }
    public void setNoMeja(String noMeja) { this.noMeja = noMeja; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public String getKodePromo() { return kodePromo; }
    public void setKodePromo(String kodePromo) { this.kodePromo = kodePromo; }
}
