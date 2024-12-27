package com.harsha.jobms.job.dto;

import com.harsha.jobms.job.Job;
import com.harsha.jobms.job.external.Company;
import com.harsha.jobms.job.external.Review;
import lombok.Data;

import java.util.List;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String minSalary;
    private String maxSalary;
    private String location;
    private Company company;
    private List<Review> reviews;
}
