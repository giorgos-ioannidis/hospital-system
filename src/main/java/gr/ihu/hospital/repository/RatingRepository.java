package gr.ihu.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.appointment.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
