package com.springboot.architectural.repository;

import com.springboot.architectural.entity.Movie_Buy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieBuyRepository extends JpaRepository<Movie_Buy, Integer> {

}
