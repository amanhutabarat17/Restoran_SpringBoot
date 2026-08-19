package com.restoran.dto;

public class AuthResponse {
    private String token;
    private Long id;
    private String nama;
    private String email;
    private String role;
    private Integer poin;

    public AuthResponse(String token, Long id, String nama, String email, String role, Integer poin) {
        this.token = token;
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.role = role;
        this.poin = poin;
    }

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Integer getPoin() { return poin; }
}
