package com.example.moviebox.screen.domain;

import com.example.moviebox.common.status.PlaceStatus;
import javax.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Screen {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String seats;
	@Column(nullable = false)
	private int totalSeats;

	@Column(nullable = false)
	private PlaceStatus status;
}
