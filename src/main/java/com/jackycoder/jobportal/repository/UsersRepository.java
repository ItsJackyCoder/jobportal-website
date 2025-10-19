package com.jackycoder.jobportal.repository;

import com.jackycoder.jobportal.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer>{
    //Behind the scene, based on this method name, Spring Data JPA will create the
    //appropriate query to query the database and give us a given result.
    Optional<Users> findByEmail(String email);
}
