package uoc.edu.security;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

@Service
public class PrivUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PrivUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // loads a user from the database and converts it into a UserDetails object that Spring Security can use
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles(user.getRole().name()).build();
    }
}
