package com.example.moviebox.configuration.security;

import com.example.moviebox.exception.*;
import com.example.moviebox.user.domain.User;
import com.example.moviebox.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SecurityUserDetailService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = userRepository.findById(Long.parseLong(userId))
			.orElseThrow(() -> BusinessException.USER_NOT_FOUND_BY_USERID);

		return new SecurityUser(user);
	}
}
