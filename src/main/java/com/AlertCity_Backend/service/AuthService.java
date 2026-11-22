package com.AlertCity_Backend.service;

import com.AlertCity_Backend.dto.AuthResponse;
import com.AlertCity_Backend.dto.LoginRequest;
import com.AlertCity_Backend.dto.RefreshTokenRequest;
import com.AlertCity_Backend.dto.RegisterRequest;
import com.AlertCity_Backend.exception.DuplicateResourceException;
import com.AlertCity_Backend.model.Rol;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.repository.UserRepository;
import com.AlertCity_Backend.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByUser(normalizedUsername)) {
            throw new DuplicateResourceException("El username '" + normalizedUsername + "' ya está en uso");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("El correo '" + normalizedEmail + "' ya está registrado");
        }

        User user = new User(
                normalizedUsername,
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                Rol.CIUDADANO,
                request.firstName().trim(),
                request.lastName().trim()
        );
        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail().trim(),
                        request.password()
                )
        );
        User user = (User) authentication.getPrincipal();
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken().trim();
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUser(username)
                    .orElseThrow(() -> new BadCredentialsException("Usuario asociado al refresh token no existe"));

            if (!jwtService.isRefreshTokenValid(token, user)) {
                throw new BadCredentialsException("Refresh token inválido o expirado");
            }
            return buildAuthResponse(user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Refresh token inválido o expirado", ex);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpiration(),
                user.getIdUser(),
                user.getUser(),
                user.getEmail(),
                user.getRol(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
