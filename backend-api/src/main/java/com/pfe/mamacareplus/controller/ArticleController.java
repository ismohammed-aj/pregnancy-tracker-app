package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.ArticleRequestDTO;
import com.pfe.mamacareplus.dto.ArticleResponseDTO;
import com.pfe.mamacareplus.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleResponseDTO> getAllArticles() {
        return articleService.getAllArticles();
    }

    @GetMapping("/{articleId}")
    public ArticleResponseDTO getArticleById(@PathVariable Integer articleId) {
        return articleService.getArticleById(articleId);
    }

    @GetMapping("/category/{category}")
    public List<ArticleResponseDTO> getArticlesByCategory(@PathVariable String category) {
        return articleService.getArticlesByCategory(category);
    }

    @GetMapping("/search")
    public List<ArticleResponseDTO> searchArticles(@RequestParam String keyword) {
        return articleService.searchArticlesByTitle(keyword);
    }

    @PostMapping
    public ArticleResponseDTO createArticle(@RequestBody ArticleRequestDTO request) {
        return articleService.createArticle(request);
    }

    @PutMapping("/{articleId}")
    public ArticleResponseDTO updateArticle(
            @PathVariable Integer articleId,
            @RequestBody ArticleRequestDTO request
    ) {
        return articleService.updateArticle(articleId, request);
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Integer articleId) {
        articleService.deleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }
}