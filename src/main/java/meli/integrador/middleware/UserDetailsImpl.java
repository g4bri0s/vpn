package meli.integrador.middleware;

import java.util.Collection;
import java.util.List;
import meli.integrador.annotations.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import meli.integrador.model.User;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Email
    private String email;
    private String password;
    private GrantedAuthority authorities;
    private boolean isActive;
    private boolean accountNonLocked;

    public UserDetailsImpl() { }

    public UserDetailsImpl(User user) {
        super();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        this.isActive = user.isActive();
        this.accountNonLocked = user.isAccountNonLocked();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}