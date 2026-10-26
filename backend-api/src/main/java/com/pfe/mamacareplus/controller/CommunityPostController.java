package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.CommunityPostRequestDTO;
import com.pfe.mamacareplus.dto.CommunityPostResponseDTO;
import com.pfe.mamacareplus.service.CommunityPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/community-posts")
@CrossOrigin(origins = "*")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @GetMapping
    public List<CommunityPostResponseDTO> getAllPosts() {
        return communityPostService.getAllPosts();
    }

    @GetMapping("/{postId}")
    public CommunityPostResponseDTO getPostById(@PathVariable Integer postId) {
        return communityPostService.getPostById(postId);
    }

    @GetMapping("/femme/{femmeId}")
    public List<CommunityPostResponseDTO> getPostsByFemmeId(@PathVariable Integer femmeId) {
        return communityPostService.getPostsByFemmeId(femmeId);
    }

    @PostMapping
    public CommunityPostResponseDTO createPost(@Valid @RequestBody CommunityPostRequestDTO request) {
        return communityPostService.createPost(request);
    }

    @PutMapping("/{postId}")
    public CommunityPostResponseDTO updatePost(
            @PathVariable Integer postId,
            @Valid @RequestBody CommunityPostRequestDTO request
    ) {
        return communityPostService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer postId) {
        communityPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}