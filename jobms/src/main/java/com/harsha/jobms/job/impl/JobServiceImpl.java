package com.harsha.jobms.job.impl;

import com.harsha.jobms.job.Job;
import com.harsha.jobms.job.JobRepository;
import com.harsha.jobms.job.JobService;
import com.harsha.jobms.job.clients.CompanyClient;
import com.harsha.jobms.job.clients.ReviewClient;
import com.harsha.jobms.job.dto.JobDTO;
import com.harsha.jobms.job.external.Company;
import com.harsha.jobms.job.external.Review;
import com.harsha.jobms.job.mapper.JobMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {
    JobRepository jobRepository;

    @Autowired
    RestTemplate restTemplate;

    private CompanyClient companyClient;
    private ReviewClient reviewClient;

    int attempt=0;

    public JobServiceImpl(JobRepository jobRepository,
                          CompanyClient companyClient,
                          ReviewClient reviewClient) {
        this.jobRepository = jobRepository;
        this.companyClient = companyClient;
        this.reviewClient = reviewClient;
    }

    @Override
//    @CircuitBreaker(name = "companyBreaker",
//            fallbackMethod = "companyBreakerFallback")
    @Retry(name = "companyBreaker",fallbackMethod = "companyBreakerFallback")
    public List<JobDTO> findAll() {
        System.out.println("Attempt: "+ ++attempt);
        List<Job> jobs=jobRepository.findAll();
//        List<JobDTO> jobDTOS = new ArrayList<>();

        return jobs.stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<String> companyBreakerFallback(Exception e){
        List<String> list=new ArrayList<>();
        list.add("Dummy");
        return list;
    }

    private JobDTO convertToDTO(Job job){
//            Company company=restTemplate.getForObject(
//                    "http://COMPANY-SERVICE:8081/company/"+job.getCompanyId(),
//                    Company.class);

//            ResponseEntity<List<Review>> reviewResponse=restTemplate.exchange(
//                    "http://REVIEW-SERVICE:8083/reviews?companyId=" +
//                            job.getCompanyId(),
//                    HttpMethod.GET,
//                    null,
//                    new ParameterizedTypeReference<List<Review>>() {
//                    });
//        List<Review> reviews=reviewResponse.getBody();

        Company company= companyClient.getCompany(job.getCompanyId());
        List<Review> reviews=reviewClient.getReviews(job.getCompanyId());

            JobDTO jobDTO = JobMapper
                    .mapToJobWithCompanyDTO(job,company, reviews);

            return jobDTO;
    }

    @Override
    public void createJobs(Job job) {
        jobRepository.save(job);
    }

    @Override
    public JobDTO getJobById(Long id) {
        Job job=jobRepository.findById(id).orElse(null);
        return convertToDTO(job);
    }

    @Override
    public boolean deleteJobById(Long id) {
        try {
            jobRepository.deleteById(id);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean updateJob(Long id, Job updatedJob) {
        Optional<Job> jobOptional=jobRepository.findById(id);
            if (jobOptional.isPresent()){
                Job job=jobOptional.get();
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                job.setLocation(updatedJob.getLocation());
                job.setMaxSalary(updatedJob.getMaxSalary());
                job.setMinSalary(updatedJob.getMinSalary());
                jobRepository.save(job);
                return true;
            }
        return false;
    }
}
