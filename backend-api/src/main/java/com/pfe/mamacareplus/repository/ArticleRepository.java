package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);

    List<Article> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
}