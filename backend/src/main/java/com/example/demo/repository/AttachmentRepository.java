package com.example.demo.repository;

import com.example.demo.entity.AttachmentDTO;
import com.example.demo.entity.CollectionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentDTO, String> {
    List<AttachmentDTO> findByCollection(CollectionDTO collection);
    void deleteByCollection(CollectionDTO collection);
}
