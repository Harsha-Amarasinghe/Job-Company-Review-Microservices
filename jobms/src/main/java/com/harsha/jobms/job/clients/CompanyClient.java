package com.harsha.jobms.job.clients;

import com.harsha.jobms.job.external.Company;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "COMPANY-SERVICE")
public interface CompanyClient {
    @GetMapping("/company/{id}")
    Company getCompany(@PathVariable("id") Long companyId);
}
