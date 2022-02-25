package com.example.ssilvermandistl1.Repositories;

import com.example.ssilvermandistl1.Models.UserPOJO;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RestResource
public interface UserRepository extends JpaRepository<UserPOJO, Integer> {
    Optional<UserPOJO> getFirstByEmail(String email);
    UserPOJO getByEmail(String email);
    UserPOJO findFirstByEmail(String email);
    Optional<UserPOJO> findByNameAndEmail(String name, String email);
}
