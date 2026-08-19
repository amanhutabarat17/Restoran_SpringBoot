package com.restoran.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 40)
    private String orderNumber;

    /** Pemesan. Null jika guest (non-member) yang order lewat scan QR meja. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Kasir yang memproses/menerima pembayaran (diisi kalau transaksi lewat kasir). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kasir_id")
    private User kasir;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promo promo;

    /** Nomor meja, diisi kalau order berasal dari scan QR meja. */
    @Column(name = "no_meja", length = 20)
    private String noMeja;

    @Column(name = "nomor_antrian")
    private Integer nomorAntrian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPesanan status = StatusPesanan.MENUNGGU;

    @Enumerated(EnumType.STRING)
    @Column(name = "metode_pembayaran", length = 20)
    private MetodePembayaran metodePembayaran;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pembayaran", nullable = false, length = 20)
    private StatusPembayaran statusPembayaran = StatusPembayaran.UNPAID;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal diskon = BigDecimal.ZERO;

    @Column(name = "biaya_layanan", nullable = false, precision = 12, scale = 2)
    private BigDecimal biayaLayanan = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // ---- Info integrasi Midtrans (diisi saat pelanggan bayar non-tunai) ----
    @Column(name = "midtrans_order_id", length = 60)
    private String midtransOrderId;

    @Column(name = "midtrans_snap_token", length = 120)
    private String midtransSnapToken;

    @Column(name = "midtrans_redirect_url", length = 255)
    private String midtransRedirectUrl;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getKasir() { return kasir; }
    public void setKasir(User kasir) { this.kasir = kasir; }
    public Promo getPromo() { return promo; }
    public void setPromo(Promo promo) { this.promo = promo; }
    public String getNoMeja() { return noMeja; }
    public void setNoMeja(String noMeja) { this.noMeja = noMeja; }
    public StatusPesanan getStatus() { return status; }
    public void setStatus(StatusPesanan status) { this.status = status; }
    public MetodePembayaran getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(MetodePembayaran metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    public StatusPembayaran getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(StatusPembayaran statusPembayaran) { this.statusPembayaran = statusPembayaran; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiskon() { return diskon; }
    public void setDiskon(BigDecimal diskon) { this.diskon = diskon; }
    public BigDecimal getBiayaLayanan() { return biayaLayanan; }
    public void setBiayaLayanan(BigDecimal biayaLayanan) { this.biayaLayanan = biayaLayanan; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMidtransOrderId() { return midtransOrderId; }
    public void setMidtransOrderId(String midtransOrderId) { this.midtransOrderId = midtransOrderId; }
    public String getMidtransSnapToken() { return midtransSnapToken; }
    public void setMidtransSnapToken(String midtransSnapToken) { this.midtransSnapToken = midtransSnapToken; }
    public String getMidtransRedirectUrl() { return midtransRedirectUrl; }
    public void setMidtransRedirectUrl(String midtransRedirectUrl) { this.midtransRedirectUrl = midtransRedirectUrl; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getNomorAntrian() { return nomorAntrian; }
    public void setNomorAntrian(Integer nomorAntrian) { this.nomorAntrian = nomorAntrian; }
}
