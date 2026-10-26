package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.ArticleRequestDTO;
import com.pfe.mamacareplus.dto.ArticleResponseDTO;
import com.pfe.mamacareplus.entity.Article;
import com.pfe.mamacareplus.repository.ArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<ArticleResponseDTO> getAllArticles() {
        return articleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Article::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToResponse)
                .toList();
    }

    public ArticleResponseDTO getArticleById(Integer articleId) {
        return mapToResponse(findArticleById(articleId));
    }

    public List<ArticleResponseDTO> getArticlesByCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }

        return articleRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ArticleResponseDTO> searchArticlesByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search keyword is required");
        }

        return articleRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ArticleResponseDTO createArticle(ArticleRequestDTO request) {
        validateCreateRequest(request);

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setContent(request.getContent());
        article.setImageUrl(request.getImageUrl());
        article.setVideoUrl(request.getVideoUrl());
        article.setVideoTitle(request.getVideoTitle());
        article.setReadingTimeMinutes(request.getReadingTimeMinutes());

        return mapToResponse(articleRepository.save(article));
    }

    public ArticleResponseDTO updateArticle(Integer articleId, ArticleRequestDTO request) {
        Article article = findArticleById(articleId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            article.setTitle(request.getTitle());
        }

        if (request.getCategory() != null) {
            article.setCategory(request.getCategory());
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            article.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            article.setImageUrl(request.getImageUrl());
        }

        if (request.getVideoUrl() != null) {
            article.setVideoUrl(request.getVideoUrl());
        }

        if (request.getVideoTitle() != null) {
            article.setVideoTitle(request.getVideoTitle());
        }

        if (request.getReadingTimeMinutes() != null) {
            validateReadingTime(request.getReadingTimeMinutes());
            article.setReadingTimeMinutes(request.getReadingTimeMinutes());
        }

        return mapToResponse(articleRepository.save(article));
    }

    public void deleteArticle(Integer articleId) {
        Article article = findArticleById(articleId);
        articleRepository.delete(article);
    }

    private Article findArticleById(Integer articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
    }

    private void validateCreateRequest(ArticleRequestDTO request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }

        if (request.getReadingTimeMinutes() != null) {
            validateReadingTime(request.getReadingTimeMinutes());
        }
    }

    private void validateReadingTime(Integer readingTimeMinutes) {
        if (readingTimeMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reading time must be positive");
        }
    }

    private ArticleResponseDTO mapToResponse(Article article) {
        return new ArticleResponseDTO(
                article.getArticleId(),
                article.getTitle(),
                article.getCategory(),
                article.getContent(),
                article.getImageUrl(),
                article.getVideoUrl(),
                article.getVideoTitle(),
                article.getReadingTimeMinutes(),
                article.getCreatedAt()
        );
    }
}
