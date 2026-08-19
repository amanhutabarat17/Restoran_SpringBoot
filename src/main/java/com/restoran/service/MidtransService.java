package com.restoran.service;

import com.restoran.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Integrasi Midtrans Snap API.
 * PENTING: server-key & client-key di application.properties masih placeholder.
 * Ganti dengan key asli dari dashboard.midtrans.com (Settings -> Access Keys)
 * sebelum fitur pembayaran non-tunai bisa benar-benar dipakai.
 */
@Service
public class MidtransService {

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.is-production}")
    private boolean isProduction;

    @Value("${midtrans.snap-base-url-sandbox}")
    private String snapUrlSandbox;

    @Value("${midtrans.snap-base-url-production}")
    private String snapUrlProduction;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Membuat transaksi Snap untuk sebuah pesanan (mendukung QRIS, e-wallet, VA, kartu, dst
     * karena memakai Snap generik -- Midtrans yang menampilkan pilihan metodenya).
     * Mengembalikan map berisi "token" dan "redirect_url".
     */
    public Map<String, String> createSnapTransaction(Order order) {
        String url = isProduction ? snapUrlProduction : snapUrlSandbox;

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", order.getOrderNumber());
        transactionDetails.put("gross_amount", order.getTotal().longValue());
        body.put("transaction_details", transactionDetails);

        Map<String, Object> customerDetails = new HashMap<>();
        if (order.getUser() != null) {
            customerDetails.put("first_name", order.getUser().getNama());
            customerDetails.put("email", order.getUser().getEmail());
        } else {
            customerDetails.put("first_name", "Pelanggan Meja " + (order.getNoMeja() != null ? order.getNoMeja() : "-"));
        }
        body.put("customer_details", customerDetails);

        List<Map<String, Object>> itemDetails = new ArrayList<>();
        order.getItems().forEach(item -> {
            Map<String, Object> it = new HashMap<>();
            it.put("id", item.getMenu() != null ? String.valueOf(item.getMenu().getId()) : "menu");
            it.put("price", item.getHargaSatuan().longValue());
            it.put("quantity", item.getQty());
            it.put("name", item.getNamaMenu());
            itemDetails.add(it);
        });
        body.put("item_details", itemDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + base64ServerKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        Map<String, String> result = new HashMap<>();
        if (response != null) {
            result.put("token", String.valueOf(response.get("token")));
            result.put("redirect_url", String.valueOf(response.get("redirect_url")));
        }
        return result;
    }

    private String base64ServerKey() {
        return Base64.getEncoder().encodeToString((serverKey + ":").getBytes());
    }
}
