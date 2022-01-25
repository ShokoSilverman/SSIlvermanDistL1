package com.example.ssilvermandistl1.Repositories;

import com.example.ssilvermandistl1.Models.Offers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource
public interface OfferRepository extends JpaRepository<Offers, Integer> {
}
