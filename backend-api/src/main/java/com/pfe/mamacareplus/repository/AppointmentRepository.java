package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByFemmeFemmeIdOrderByAppointmentDateAsc(Integer femmeId);

    List<Appointment> findByFemmeFemmeIdAndStatusOrderByAppointmentDateAsc(Integer femmeId, String status);
}