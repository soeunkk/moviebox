package com.example.moviebox.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MailUtils {
	private final JavaMailSender javaMailSender;

	public void sendMail(String mail, String subject, String text) throws MailException {
		MimeMessagePreparator message = mimeMessage -> {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			mimeMessageHelper.setTo(mail);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(text, true);
		};

		javaMailSender.send(message);
	}
}
