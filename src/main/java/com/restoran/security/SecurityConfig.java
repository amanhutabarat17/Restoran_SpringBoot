package com.restoran.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publik: auth, lihat menu & promo, buat pesanan (guest/QR), webhook midtrans
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/menus/**").permitAll()
                        // Gambar menu yang sudah diupload harus bisa dilihat semua orang (pelanggan lihat menu)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/promo/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/ratings/menu/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").permitAll()
                        // GET by-id dibuka publik: dipakai halaman /pembayaran/:id yang bisa diakses
                        // guest (belum login) untuk cek status & polling pembayaran pesanannya sendiri.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/{id:[0-9]+}").permitAll()
                        .requestMatchers("/api/payment/midtrans/notification").permitAll()
                        // Publik: buat transaksi Midtrans & cek status pembayaran — dipakai
                        // pelanggan yang scan QR meja dan BELUM login (guest), jadi wajib permitAll.
                        // Sebelumnya endpoint ini tidak ada di daftar permitAll sehingga jatuh ke
                        // .anyRequest().authenticated() dan selalu ditolak (401/403) untuk guest —
                        // inilah penyebab Midtrans "tidak jalan sama sekali", bukan soal key.
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/payment/midtrans/create/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/payment/status/**").permitAll()

                        // Admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/upload/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/menus").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/menus/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/menus/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/menus/**").hasAnyRole("ADMIN", "KASIR")
                        .requestMatchers("/api/promo/**").hasRole("ADMIN")
                        .requestMatchers("/api/laporan/**").hasRole("ADMIN")

                        // Kasir, admin & member: lihat/kelola pesanan (POST tanpa login sudah di-permit di atas)
                        .requestMatchers("/api/orders", "/api/orders/**").hasAnyRole("KASIR", "ADMIN", "CUSTOMER")

                        // Member only
                        .requestMatchers("/api/ratings/**").hasRole("CUSTOMER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}