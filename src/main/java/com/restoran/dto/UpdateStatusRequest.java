package com.restoran.dto;

import com.restoran.entity.StatusPesanan;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {
    @NotNull
    private StatusPesanan status;

    public StatusPesanan getStatus() { return status; }
    public void setStatus(StatusPesanan status) { this.status = status; }
}
