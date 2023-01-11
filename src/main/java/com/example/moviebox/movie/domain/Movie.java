package com.example.moviebox.movie.domain;

import com.example.moviebox.common.entity.BaseTimeEntity;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Movie extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String title;
	private String genre;
	private String summary;
	private String director;
	private String actors;
	private int runningTime;
	private Rating rating;
	private String posterUrl;
	private LocalDateTime openedAt;

	private int audience;
	private float grade;
	private LocalDateTime gradeUpdatedAt;
}
