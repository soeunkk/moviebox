package com.example.moviebox.movie.domain;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum Rating {	// 상영 등급
	AGE0("전체 관람가"),
	AGE12("12세 관람가"),
	AGE15("15세 관람가"),
	AGE18("청소년 관람불가");

	private final String displayName;
//	private final String imageUrl;
}
