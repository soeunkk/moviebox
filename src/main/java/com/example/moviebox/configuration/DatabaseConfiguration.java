package com.example.moviebox.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement	// 트랜잭션 활성화
@EnableJpaAuditing				// 생성일자/수정일자 자동 생성
public class DatabaseConfiguration {

}
