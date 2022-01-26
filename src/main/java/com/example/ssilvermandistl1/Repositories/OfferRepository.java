package com.example.ssilvermandistl1.Repositories;

import com.example.ssilvermandistl1.Models.Offers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@RestResource
public interface OfferRepository extends JpaRepository<Offers, Integer> {
}
