package com.example.moviebox.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.*;

@ExtendWith(MockitoExtension.class)
class MailUtilsTest {
	@Mock
	JavaMailSender javaMailSender;

	@InjectMocks
	private MailUtils mailUtils;

	@Test
	void testSendMail() {
		willDoNothing()
			.given(javaMailSender).send(any(MimeMessagePreparator.class));

		mailUtils.sendMail("example@email.com", "제목", "내용");
	}

	@Test
	void testSendMailWhenExceptionThrown() {
		willThrow(new MailSendException(""))
			.given(javaMailSender).send(any(MimeMessagePreparator.class));

		assertThrows(MailException.class,
			() -> mailUtils.sendMail("example@email.com", "제목", "내용"));
	}
}
