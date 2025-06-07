package meli.integrador.service;

import meli.integrador.dto.AuthRequest;
import meli.integrador.middleware.JwtAuthService;
import meli.integrador.model.User;
import meli.integrador.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository repository;
    private final JwtAuthService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Constructor injection
    public AuthService(UserRepository repository, 
                       JwtAuthService jwtService, 
                       AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    private void validateAuthRequest(AuthRequest request) {
        Objects.requireNonNull(request, "AuthRequest cannot be null.");

        String email = request.getEmail();
        String password = request.getPassword();

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }
    }

    public Optional<String> authenticateUser(Optional<AuthRequest> loginRequestOptional) {
        if (loginRequestOptional.isEmpty()) {
            return Optional.empty(); 
        }

        AuthRequest loginRequest = loginRequestOptional.get();
        validateAuthRequest(loginRequest); 

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        UsernamePasswordAuthenticationToken credentials =
          new UsernamePasswordAuthenticationToken(email, password);

        try {
            Authentication authentication = authenticationManager.authenticate(credentials);
            if (authentication.isAuthenticated()) {
                Optional<User> user = repository.findByEmail(email);
                if (user.isPresent()) {
                    String response = generateToken(email);
                    return Optional.of(response);
                }
            }
        } catch (BadCredentialsException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private String generateToken(String username) {
        return "Bearer " + jwtService.generateToken(username);
    }
}