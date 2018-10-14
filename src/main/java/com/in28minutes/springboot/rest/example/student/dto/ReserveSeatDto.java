package com.in28minutes.springboot.rest.example.student.dto;

import java.util.List;
import java.util.Map;

/**
 * Created by slk on 13-Oct-18.
 */
public class ReserveSeatDto {
    public Map<String, List<Integer>> getSeats() {
        return seats;
    }

    public void setSeats(Map<String, List<Integer>> seats) {
        this.seats = seats;
    }

    public Map<String, List<Integer>> seats;

}
