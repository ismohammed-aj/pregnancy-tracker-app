package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostPostIdOrderByCreatedAtAsc(Integer postId);

    List<Comment> findByFemmeFemmeIdOrderByCreatedAtDesc(Integer femmeId);
}