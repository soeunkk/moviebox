package com.example.moviebox.screen.domain;

import com.example.moviebox.common.status.PlaceStatus;
import com.example.moviebox.screening.domain.Screening;
import com.example.moviebox.theater.domain.Theater;
import java.util.List;
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

	@ManyToOne
	@JoinColumn(name = "theater_id", referencedColumnName = "id")
	private Theater theater;

	@OneToMany(mappedBy = "screen")
	private List<Screening> screenings;
}
