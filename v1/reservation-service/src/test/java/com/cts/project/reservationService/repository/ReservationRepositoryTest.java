package com.cts.project.reservationService.repository;

import com.cts.project.reservationService.entity.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// uses an in-memory H2 db for testing — no real mysql needed
@DataJpaTest
@AutoConfigureTestDatabase
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private Reservation res1;
    private Reservation res2;

    @BeforeEach
    void setUp() {
        // sample data added before each test
        res1 = new Reservation();
        res1.setUserId(1L);
        res1.setSlotId(10L);
        res1.setVehicleNumber("TN01AB1234");
        res1.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        res1.setEndTime(LocalDateTime.of(2026, 5, 1, 12, 0));
        res1.setStatus("CONFIRMED");

        res2 = new Reservation();
        res2.setUserId(2L);
        res2.setSlotId(11L);
        res2.setVehicleNumber("KA02CD5678");
        res2.setStartTime(LocalDateTime.of(2026, 5, 2, 9, 0));
        res2.setEndTime(LocalDateTime.of(2026, 5, 2, 11, 0));
        res2.setStatus("CANCELLED");

        reservationRepository.save(res1);
        reservationRepository.save(res2);
    }

    @Test
    void shouldFindReservationsByUserId() {
        List<Reservation> result = reservationRepository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicleNumber()).isEqualTo("TN01AB1234");
    }

    @Test
    void shouldFindReservationsBySlotId() {
        List<Reservation> result = reservationRepository.findBySlotId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void shouldFindReservationsByStatus() {
        List<Reservation> confirmed = reservationRepository.findByStatus("CONFIRMED");
        List<Reservation> cancelled = reservationRepository.findByStatus("CANCELLED");

        assertThat(confirmed).hasSize(1);
        assertThat(cancelled).hasSize(1);
    }

    @Test
    void shouldDetectOverlappingReservation() {
        // try booking same slot during overlap window
        boolean overlap = reservationRepository.existsOverlappingReservation(
                10L,
                LocalDateTime.of(2026, 5, 1, 11, 0),  // starts during res1
                LocalDateTime.of(2026, 5, 1, 13, 0)   // ends after res1
        );

        assertThat(overlap).isTrue();
    }

    @Test
    void shouldNotDetectOverlapWhenTimesDontConflict() {
        // booking same slot but at a totally different time
        boolean overlap = reservationRepository.existsOverlappingReservation(
                10L,
                LocalDateTime.of(2026, 5, 1, 14, 0),
                LocalDateTime.of(2026, 5, 1, 16, 0)
        );

        assertThat(overlap).isFalse();
    }

    @Test
    void shouldSaveAndFindReservation() {
        Reservation newRes = new Reservation();
        newRes.setUserId(5L);
        newRes.setSlotId(20L);
        newRes.setVehicleNumber("MH12XY9999");
        newRes.setStartTime(LocalDateTime.now());
        newRes.setStatus("CONFIRMED");

        Reservation saved = reservationRepository.save(newRes);

        assertThat(saved.getId()).isNotNull();
        assertThat(reservationRepository.findById(saved.getId())).isPresent();
    }
}