package com.in28minutes.springboot.rest.example.student.service;

import static com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto.Seat.Seatstatus.AVAILABLE;
import static com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto.Seat.Seatstatus.UNAVAILABLE;
import static com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto.Status.FAILED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto;
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

    public Screen getUpdatedScreen(ReserveSeatDto reserveData, Screen screenByName, ReservationResponseDto responseDto) {
        Map<String, List<ReservationResponseDto.Seat>> responseSeatMap = new HashMap<>();

        List<Row> rowList = new ArrayList<>();
        reserveData.getSeats().forEach((rowName, seats) -> {
            List<ReservationResponseDto.Seat> responseSeats = new ArrayList<>();

            List<Row> rows = screenByName.getRows()
                    .stream()
                    .filter(r -> rowName.equalsIgnoreCase(r.getRowName()))
                    .collect(Collectors.toList());

            if (rows.isEmpty()){
                responseDto.status = FAILED;
                List<ReservationResponseDto.Seat> seatList = seats.stream().map(seatNo -> {
                    ReservationResponseDto.Seat seat = new ReservationResponseDto.Seat();
                    seat.seatstatus = UNAVAILABLE;
                    seat.seatNo = seatNo;
                    return seat;
                }).collect(Collectors.toList());
                responseSeatMap.put(rowName, seatList);
            }else {
                rowList.addAll(screenByName.getRows()
                        .stream()
                        .map(row -> {
                            if (row.getRowName().equalsIgnoreCase(rowName)) {
                                List<Seat> seatList = row.getSeats().stream().map(s -> {
                                    if (seats.contains(s.getSeatId())) {
                                        ReservationResponseDto.Seat rSeat = new ReservationResponseDto.Seat();
                                        rSeat.seatNo = s.getSeatId();
                                        if (s.getStatus().equals(Status.UN_RESERVED)) {
                                            s.setStatus(Status.RESERVED);
                                            rSeat.seatstatus = AVAILABLE;
                                        } else {
                                            responseDto.setStatus(FAILED);
                                            rSeat.seatstatus = UNAVAILABLE;
                                        }
                                        responseSeats.add(rSeat);
                                    }
                                    return s;
                                }).collect(Collectors.toList());
                                row.setSeats(seatList);
                            }
                            return row;
                        }).collect(Collectors.toList()));
                responseSeatMap.put(rowName, responseSeats);
            }
        });
        screenByName.setRows(rowList);
        responseDto.setSeats(responseSeatMap);
        return screenByName;
    }

    //Converts API exposed DTO to persistance Object
    //So that this can be used to save in database
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
