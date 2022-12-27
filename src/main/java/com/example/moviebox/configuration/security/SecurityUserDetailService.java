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
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO: 소셜로그인도 허용하려면 추후 이 부분 바꿔야 함
		User user = userRepository.findByEmail(username)
			.orElseThrow(() -> BusinessException.USER_NOT_FOUND_BY_EMAIL);

		return new SecurityUser(user);
	}
}
