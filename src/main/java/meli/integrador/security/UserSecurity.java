package meli.integrador.security;

import jakarta.servlet.http.HttpServletRequest;
import meli.integrador.model.User;
import meli.integrador.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class UserSecurity implements AuthorizationManager<RequestAuthorizationContext> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        boolean authorized = isOwner(authentication);
        return new AuthorizationDecision(authorized);
    }

    private boolean isOwner(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        String idPath = getPath();
        return user != null && (user.getId().equals(idPath) || user.getEmail().equals(idPath));
    }

    private String getPath() {
        String path = request.getRequestURI();
        String[] pathParts = path.split("/");
        if (pathParts[1].equals("product") && pathParts[2].equals("posts") && pathParts.length > 3) {
            return pathParts[3];
        }
        if (pathParts[1].equals("user")) {
            return pathParts[2];
        }
        return null;
    }
}
