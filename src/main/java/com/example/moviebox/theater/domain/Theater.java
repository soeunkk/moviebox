package com.example.moviebox.theater.domain;

import com.example.moviebox.common.status.PlaceStatus;
import javax.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Theater {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Enumerated(EnumType.STRING)
	private Region region;
	private double x;
	private double y;
	private String streetName;

	private PlaceStatus status;
}
