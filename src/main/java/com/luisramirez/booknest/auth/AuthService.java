package com.luisramirez.booknest.auth;

import com.luisramirez.booknest.exception.ConflictException;
import com.luisramirez.booknest.security.JwtService;
import com.luisramirez.booknest.user.Role;
import com.luisramirez.booknest.user.User;
import com.luisramirez.booknest.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ConflictException("Ya existe una cuenta con ese email");
        }

        User user = new User(
            request.name().trim(),
            request.email().toLowerCase().trim(),
            passwordEncoder.encode(request.password()),
            Role.USER
        );
        userRepository.save(user);

        return new AuthResponse(
            jwtService.generateToken(user),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email().toLowerCase().trim(),
                    request.password()
                )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
            .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        return new AuthResponse(
            jwtService.generateToken(user),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}
