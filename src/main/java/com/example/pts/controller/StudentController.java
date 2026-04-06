package com.example.pts.controller;

import com.example.pts.model.Student;
import com.example.pts.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student details) {
        return studentRepository.findById(id).map(student -> {
            student.setName(details.getName());
            student.setRoll(details.getRoll());
            student.setDept(details.getDept());
            student.setYear(details.getYear());
            student.setCgpa(details.getCgpa());
            student.setStatus(details.getStatus());
            student.setCompany(details.getCompany());
            return studentRepository.save(student);
        }).orElseGet(() -> {
            details.setId(id);
            return studentRepository.save(details);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
    }
}
