package com.in28minutes.springboot.rest.example.student.repository;

import com.in28minutes.springboot.rest.example.student.persistence.Row;
import com.in28minutes.springboot.rest.example.student.persistence.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RowRepository extends JpaRepository<Row, Long>{

}
