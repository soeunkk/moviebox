package com.example.moviebox.configuration.security;

import com.example.moviebox.user.domain.User;
import org.springframework.security.core.authority.AuthorityUtils;

public class SecurityUser extends org.springframework.security.core.userdetails.User {
	public SecurityUser(User user) {
		super(String.valueOf(user.getId()), user.getPassword(),
			AuthorityUtils.createAuthorityList(user.getRole().getAuthority()));
	}
}
