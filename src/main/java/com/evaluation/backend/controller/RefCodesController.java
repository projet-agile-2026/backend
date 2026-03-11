package com.evaluation.backend.controller;

import com.evaluation.backend.entity.CgRefCodes;
import com.evaluation.backend.repository.CgRefCodesRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ref-codes")
public class RefCodesController {

    private final CgRefCodesRepository repo;

    public RefCodesController(CgRefCodesRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CgRefCodes> getByDomain(@RequestParam String domain) {
        return repo.findByRvDomainOrderByRvMeaning(domain);
    }
}