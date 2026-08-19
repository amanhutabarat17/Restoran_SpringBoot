package com.restoran.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supaya Jackson bisa serialize entity JPA yang punya relasi FetchType.LAZY
 * (mis. Order.user, Order.kasir, Order.promo, OrderItem.menu) tanpa error
 * "Type definition error: [simple type, class org.hibernate.proxy...ByteBuddyInterceptor]".
 *
 * FORCE_LAZY_LOADING = true -> relasi lazy yang belum dimuat akan otomatis
 * di-load lalu diserialize (butuh session Hibernate masih terbuka, makanya
 * spring.jpa.open-in-view harus tetap true seperti sekarang).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true);
        return module;
    }
}