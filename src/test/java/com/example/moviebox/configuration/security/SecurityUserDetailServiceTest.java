package com.example.moviebox.configuration.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.user.domain.Role;
import com.example.moviebox.user.domain.User;
import com.example.moviebox.user.domain.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class SecurityUserDetailServiceTest {
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SecurityUserDetailService securityUserDetailService;

	@Test
	public void testLoadUserByUsername() {
		given(userRepository.findById(anyLong()))
			.willReturn(Optional.of(User.builder()
				.id(1)
				.password("password")
				.role(Role.ADMIN)
				.build()));

		UserDetails userDetails = securityUserDetailService.loadUserByUsername("1");

		assertEquals("1", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertEquals("[ROLE_ADMIN]", userDetails.getAuthorities().toString());
	}

	@Test
	public void testLoadUserByUsernameByNotExistUserId() {
		given(userRepository.findById(anyLong()))
			.willThrow(BusinessException.USER_NOT_FOUND_BY_USERID);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> securityUserDetailService.loadUserByUsername("1"));

		assertEquals(BusinessException.USER_NOT_FOUND_BY_USERID, exception);
	}
}
