package com.example.moviebox.theater.domain;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@RequiredArgsConstructor
public enum Region {
	SOEUL("서울"),
	GYEONGGI("경기"),
	INCHEON("인천"),
	GANGWON("강원"),
	CHUNGCHEONG("대전/충청"),
	GYEONGSANG("부산/대구/경상"),
	JEOLLA("광주/전라/제주");

	private final String displayName;

	@JsonCreator
	public static Region from(String value) {
		return Region.valueOf(value);
	}

	@JsonValue
	public String getDisplayName() {
		return displayName;
	}
}
