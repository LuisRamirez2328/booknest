package com.luisramirez.booknest.auth;

import com.luisramirez.booknest.user.Role;

public record AuthResponse(
    String token,
    String name,
    String email,
    Role role
) {
}
