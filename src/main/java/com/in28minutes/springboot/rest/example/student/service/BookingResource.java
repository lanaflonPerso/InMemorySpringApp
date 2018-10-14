package com.in28minutes.springboot.rest.example.student.service;

import static com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto.Status.SUCCESS;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.in28minutes.springboot.rest.example.student.NotFoundException;
import com.in28minutes.springboot.rest.example.student.StudentNotFoundException;
import com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto;
import com.in28minutes.springboot.rest.example.student.dto.ReservationResponseDto.Status;
import com.in28minutes.springboot.rest.example.student.dto.ReserveSeatDto;
import com.in28minutes.springboot.rest.example.student.dto.ScreenInfoDto;
import com.in28minutes.springboot.rest.example.student.persistence.Row;
import com.in28minutes.springboot.rest.example.student.persistence.Screen;
import com.in28minutes.springboot.rest.example.student.persistence.Student;
import com.in28minutes.springboot.rest.example.student.repository.RowRepository;
import com.in28minutes.springboot.rest.example.student.repository.ScreenRepository;
import com.in28minutes.springboot.rest.example.student.repository.SeatRepository;
import com.in28minutes.springboot.rest.example.student.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    BookingResourceImpl bookingResourceImpl;

	@GetMapping("/students")
	public List<Student> retrieveAllStudents() {
		return studentRepository.findAll();
	}

	@GetMapping("/screen")
	public List<Screen> retrieveAllScreens() {
		return screenRepository.findAll();
	}

    @GetMapping("/screen/rows")
    public List<Row> getAllRows() {
        return rowRepository.findAll();
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
        Screen screenByName = screenRepository.getScreenByName(screenName);
        if (screenByName == null){
            throw new NotFoundException(screenName);
        }
        return screenByName;
    }

    @PostMapping("/screen/{screenName}/reserve")
    public ResponseEntity<?> reserveSeats(@RequestBody ReserveSeatDto reserveData, @PathVariable String screenName) {

        Screen screenByName = screenRepository.getScreenByName(screenName);
        if (screenByName == null){
            throw new NotFoundException(screenName);
        }
        ReservationResponseDto responseDto = new ReservationResponseDto();
        responseDto.status = SUCCESS;
        Screen updatedScreen = bookingResourceImpl.getUpdatedScreen(reserveData, screenByName, responseDto);

        if (responseDto.getStatus().equals(SUCCESS)) {
            screenRepository.save(updatedScreen);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/students/{id}")
	public void deleteStudent(@PathVariable long id) {
		studentRepository.deleteById(id);
	}

    @PostMapping("/screen")
    public ResponseEntity<Object> createScreen(@RequestBody ScreenInfoDto screenData) {
        Screen screen = screenRepository.save(bookingResourceImpl.getScreenPersistenceObject(screenData));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(screen.getId()).toUri();

        return ResponseEntity.created(location).build();
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
