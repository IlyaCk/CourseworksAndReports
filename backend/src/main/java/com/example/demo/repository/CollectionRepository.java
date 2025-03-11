package com.example.demo.repository;

import com.example.demo.entity.CollectionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<CollectionDTO, String> {

}
