package com.in28minutes.springboot.rest.example.student;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.in28minutes.springboot.rest.example.student.dto.ReserveSeatDto;
import com.in28minutes.springboot.rest.example.student.dto.ScreenInfoDto;
import com.in28minutes.springboot.rest.example.student.dto.ScreenInfoDto.RowInfo;
import com.in28minutes.springboot.rest.example.student.persistence.Row;
import com.in28minutes.springboot.rest.example.student.persistence.Screen;
import com.in28minutes.springboot.rest.example.student.persistence.Seat;
import com.in28minutes.springboot.rest.example.student.persistence.Seat.Status;
import com.in28minutes.springboot.rest.example.student.persistence.Student;
import com.in28minutes.springboot.rest.example.student.repository.RowRepository;
import com.in28minutes.springboot.rest.example.student.repository.ScreenRepository;
import com.in28minutes.springboot.rest.example.student.repository.SeatRepository;
import com.in28minutes.springboot.rest.example.student.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class BookingResource {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RowRepository rowRepository;

	@GetMapping("/students")
	public List<Student> retrieveAllStudents() {
		return studentRepository.findAll();
	}

	@GetMapping("/screen")
	public List<Screen> retrieveAllScreens() {
		return screenRepository.findAll();
	}

	@GetMapping("/students/{id}")
	public Student retrieveStudent(@PathVariable long id) {
		Optional<Student> student = studentRepository.findById(id);

		if (!student.isPresent())
			throw new StudentNotFoundException("id-" + id);

		return student.get();
	}

    @GetMapping("/screen/{screenName}")
    public Screen getScreen(@PathVariable String screenName) {
        return screenRepository.getScreenByName(screenName);
    }

    @PostMapping("/screen/{screenName}/reserve")
    public ResponseEntity<Object> reserveSeats(@PathVariable String screenName, @RequestBody ReserveSeatDto reserveData) {
        screenRepository.save(getUpdatedScreen(reserveData, screenName));
        return ResponseEntity.noContent().build();
    }

    private Screen getUpdatedScreen(ReserveSeatDto reserveData, String screenName) {
        Screen screenByName = screenRepository.getScreenByName(screenName);
        if (screenByName != null) {
            reserveData.getSeats().forEach((rowName, seats) -> {
                List<Row> rowList = screenByName.getRows()
                        .stream()
                        .filter(r -> r.getRowName().equalsIgnoreCase(rowName))
                        .map(row -> {
                            List<Seat> seatList = row.getSeats().stream().map(s -> {
                                s.setStatus(seats.contains(s.getSeatId()) ? Status.RESERVED : Status.UN_RESERVED);
                                return s;
                            }).collect(Collectors.toList());
                            row.setSeats(seatList);
                            return row;
                        }).collect(Collectors.toList());
                screenByName.setRows(rowList);
            });
        }
        return screenByName;
    }

    @DeleteMapping("/students/{id}")
	public void deleteStudent(@PathVariable long id) {
		studentRepository.deleteById(id);
	}

    @PostMapping("/screen")
    public ResponseEntity<Object> createScreen(@RequestBody ScreenInfoDto screenData) {
        Screen screen = screenRepository.save(getScreenPersistenceObject(screenData));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(screen.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    private Screen getScreenPersistenceObject(@RequestBody ScreenInfoDto screenData) {
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

    @PutMapping("/students/{id}")
	public ResponseEntity<Object> updateStudent(@RequestBody Student student, @PathVariable long id) {

		Optional<Student> studentOptional = studentRepository.findById(id);

		if (!studentOptional.isPresent())
			return ResponseEntity.notFound().build();

		student.setId(id);

		studentRepository.save(student);

		return ResponseEntity.noContent().build();
	}
}
