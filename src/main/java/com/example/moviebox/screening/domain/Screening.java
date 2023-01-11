package com.example.moviebox.screening.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@TypeDef(name = "jsonb", typeClass = JsonType.class)
public class Screening {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private LocalDateTime screenedAt;
	@Column(nullable = false)
	private LocalDateTime endedAt;

	@Type(type = "jsonb")
	@Column(columnDefinition = "longtext", nullable = false)
	private Map<String, Boolean> seats = new HashMap<>();	// 좌석 별 예약 여부
	private boolean isReflectedInAudience;
}
