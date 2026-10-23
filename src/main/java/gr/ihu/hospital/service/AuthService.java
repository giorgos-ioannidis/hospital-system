package gr.ihu.hospital.service;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.UserRepository;

/**
 * UC-01: σύνδεση με έλεγχο ρόλου. Μετρά τις αποτυχημένες προσπάθειες
 * και κλειδώνει τον λογαριασμό στην 3η (UC-01).
 */
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        User user = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .accountLocked(user.isLockedAt(java.time.LocalDateTime.now()))
            .disabled(!user.isEnabled())
            .build();
    }

    @EventListener
    @Transactional
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        users.findByUsername(event.getAuthentication().getName()).ifPresent(User::registerFailedLogin);
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        users.findByUsername(event.getAuthentication().getName()).ifPresent(User::resetFailedLogins);
    }
}
