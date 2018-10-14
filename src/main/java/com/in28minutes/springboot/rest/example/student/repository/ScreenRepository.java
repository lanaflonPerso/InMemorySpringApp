package com.in28minutes.springboot.rest.example.student.repository;

import javax.websocket.server.PathParam;

import com.in28minutes.springboot.rest.example.student.persistence.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long>{

    @Query("select screen from Screen screen where screen.name = :screenName")
    Screen getScreenByName(@Param("screenName") String screenName);
}
