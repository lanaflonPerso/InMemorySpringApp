package com.in28minutes.springboot.rest.example.student.repository;

import com.in28minutes.springboot.rest.example.student.persistence.Row;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Row, Long>{



}
