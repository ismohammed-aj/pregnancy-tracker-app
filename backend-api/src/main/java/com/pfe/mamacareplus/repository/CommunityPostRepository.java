package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Integer> {

    List<CommunityPost> findByFemmeFemmeIdOrderByCreatedAtDesc(Integer femmeId);

    List<CommunityPost> findAllByOrderByCreatedAtDesc();
}