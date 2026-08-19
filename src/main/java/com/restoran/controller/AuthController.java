package com.restoran.controller;

import com.restoran.dto.AuthResponse;
import com.restoran.dto.LoginRequest;
import com.restoran.dto.RegisterRequest;
import com.restoran.entity.Role;
import com.restoran.entity.User;
import com.restoran.repository.UserRepository;
import com.restoran.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint login dipakai oleh SEMUA role (customer/member, kasir, admin) --
 * frontend cukup kirim email+password ke sini, lalu arahkan halaman
 * berdasarkan field "role" pada response.
 * Endpoint register hanya untuk pelanggan (member) mendaftar sendiri.
 * Akun kasir & admin dibuat lewat /api/admin/kasir oleh admin.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email sudah terdaftar"));
        }

        User user = new User();
        user.setNama(request.getNama());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setPoin(0);
        user.setAktif(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getNama(), user.getEmail(),
                user.getRole().name(), user.getPoin()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Email atau kata sandi salah"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Pengguna tidak ditemukan"));

        if (!Boolean.TRUE.equals(user.getAktif())) {
            return ResponseEntity.status(403).body(Map.of("message", "Akun ini sudah dinonaktifkan"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getNama(), user.getEmail(),
                user.getRole().name(), user.getPoin()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "nama", user.getNama(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "poin", user.getPoin()
        ));
    }
}
