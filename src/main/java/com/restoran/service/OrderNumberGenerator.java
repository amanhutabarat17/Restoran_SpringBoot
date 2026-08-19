package com.restoran.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderNumberGenerator {
    private final AtomicInteger counter = new AtomicInteger(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** Format: RK-yyyyMMddHHmmss-XXX */
    public String next() {
        String waktu = LocalDateTime.now().format(FMT);
        int urut = counter.incrementAndGet() % 1000;
        return "RK-" + waktu + "-" + String.format("%03d", urut);
    }
}
