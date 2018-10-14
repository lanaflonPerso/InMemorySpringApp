package com.in28minutes.springboot.rest.example.student.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.in28minutes.springboot.rest.example.student.dto.ReserveSeatDto;
import com.in28minutes.springboot.rest.example.student.dto.ScreenInfoDto;
import com.in28minutes.springboot.rest.example.student.dto.ScreenInfoDto.RowInfo;
import com.in28minutes.springboot.rest.example.student.persistence.Row;
import com.in28minutes.springboot.rest.example.student.persistence.Screen;
import com.in28minutes.springboot.rest.example.student.persistence.Seat;
import com.in28minutes.springboot.rest.example.student.persistence.Seat.Status;
import com.in28minutes.springboot.rest.example.student.repository.ScreenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by slk on 14-Oct-18.
 */

@Component
public class BookingResourceImpl {

    @Autowired
    private ScreenRepository screenRepository;

    public Screen getUpdatedScreen(ReserveSeatDto reserveData, String screenName) {
        Screen screenByName = screenRepository.getScreenByName(screenName);
        if (screenByName != null) {
            List<Row> rowList = new ArrayList<>();
            reserveData.getSeats().forEach((rowName, seats) -> {
                rowList.addAll(screenByName.getRows()
                        .stream()
                        .map(row -> {
                            if (row.getRowName().equalsIgnoreCase(rowName)) {
                                List<Seat> seatList = row.getSeats().stream().map(s -> {
                                    s.setStatus(seats.contains(s.getSeatId()) ? Status.RESERVED : Status.UN_RESERVED);
                                    return s;
                                }).collect(Collectors.toList());
                                row.setSeats(seatList);
                            }
                            return row;
                        }).collect(Collectors.toList()));
            });
            screenByName.setRows(rowList);

        }
        return screenByName;
    }

    public Screen getScreenPersistenceObject(@RequestBody ScreenInfoDto screenData) {
        Screen screen = new Screen();
        List<Row> rows = new ArrayList<>();
        screen.setName(screenData.name);
        Map<String, RowInfo> seatInfo = screenData.seatInfo;

        seatInfo.forEach((rowName,rowInfo) -> {
            List<Seat> seats = new ArrayList<>();
            Row row = new Row();
            row.setRowName(rowName);
            row.setNoOfSeats(rowInfo.numberOfSeats);

            IntStream.range(0, rowInfo.numberOfSeats).forEach(seatNumber -> {
                Seat seat = new Seat();
                seat.setStatus(Status.UN_RESERVED);
                seat.setAisle(rowInfo.aisleSeats.contains(seatNumber) ? Boolean.TRUE : Boolean.FALSE);
                seat.setSeatId(seatNumber);
                seats.add(seat);
            });

            row.setSeats(seats);
            rows.add(row);
        });

        screen.setRows(rows);
        return screen;
    }
}
