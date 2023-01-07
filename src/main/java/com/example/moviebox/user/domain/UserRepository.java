package com.example.moviebox.user.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findByEmailAuthKey(String emailAuthKey);
}
