package com.example.moviebox.component;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MailUtil {
	private final JavaMailSender javaMailSender;

	public boolean sendMail(String mail, String subject, String text) {
		boolean result = false;

		MimeMessagePreparator message = mimeMessage -> {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			mimeMessageHelper.setTo(mail);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(text, true);
		};

		try {
			javaMailSender.send(message);
			result = true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return result;
	}
}
