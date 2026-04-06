package com.example.pts.controller;

import com.example.pts.model.Job;
import com.example.pts.dto.JobDTO;
import com.example.pts.repository.JobRepository;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*") // Allow React frontend
public class JobController {

    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;

    public JobController(JobRepository jobRepository, ModelMapper modelMapper) {
        this.jobRepository = jobRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<JobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(job -> modelMapper.map(job, JobDTO.class))
                .collect(Collectors.toList());
    }

    @PostMapping
    public JobDTO createJob(@RequestBody JobDTO jobDTO) {
        Job job = modelMapper.map(jobDTO, Job.class);
        Job savedJob = jobRepository.save(job);
        return modelMapper.map(savedJob, JobDTO.class);
    }

    @PutMapping("/{id}")
    public JobDTO updateJob(@PathVariable Long id, @RequestBody JobDTO jobDetails) {  
        return jobRepository.findById(id).map(job -> {
            job.setTitle(jobDetails.getTitle());
            job.setCompany(jobDetails.getCompany());
            job.setLocation(jobDetails.getLocation());
            job.setSalary(jobDetails.getSalary());
            job.setType(jobDetails.getType());
            job.setDeadline(jobDetails.getDeadline());
            job.setEligibility(jobDetails.getEligibility());
            return modelMapper.map(jobRepository.save(job), JobDTO.class);
        }).orElseGet(() -> {
            Job newJob = modelMapper.map(jobDetails, Job.class);
            newJob.setId(id);
            return modelMapper.map(jobRepository.save(newJob), JobDTO.class);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobRepository.deleteById(id);
    }
}
