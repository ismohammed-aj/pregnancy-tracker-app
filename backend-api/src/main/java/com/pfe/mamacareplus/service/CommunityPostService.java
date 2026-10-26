package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.CommunityPostRequestDTO;
import com.pfe.mamacareplus.dto.CommunityPostResponseDTO;
import com.pfe.mamacareplus.entity.CommunityPost;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.repository.CommunityPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CurrentUserService currentUserService;

    public CommunityPostService(
            CommunityPostRepository communityPostRepository,
            CurrentUserService currentUserService
    ) {
        this.communityPostRepository = communityPostRepository;
        this.currentUserService = currentUserService;
    }

    public List<CommunityPostResponseDTO> getAllPosts() {
        return communityPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CommunityPostResponseDTO getPostById(Integer postId) {
        CommunityPost post = findPostById(postId);
        return mapToResponse(post);
    }

    public List<CommunityPostResponseDTO> getPostsByFemmeId(Integer femmeId) {
        return communityPostRepository.findByFemmeFemmeIdOrderByCreatedAtDesc(femmeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CommunityPostResponseDTO createPost(CommunityPostRequestDTO request) {
        validateRequest(request);

        Femme currentFemme = currentUserService.getCurrentFemme();

        CommunityPost post = new CommunityPost();
        post.setFemme(currentFemme);
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());

        return mapToResponse(communityPostRepository.save(post));
    }

    public CommunityPostResponseDTO updatePost(Integer postId, CommunityPostRequestDTO request) {
        CommunityPost post = findPostById(postId);

        checkPostOwner(post);

        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        return mapToResponse(communityPostRepository.save(post));
    }

    public void deletePost(Integer postId) {
        CommunityPost post = findPostById(postId);

        checkPostOwner(post);

        communityPostRepository.delete(post);
    }

    private CommunityPost findPostById(Integer postId) {
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community post not found"));
    }

    private void validateRequest(CommunityPostRequestDTO request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content is required");
        }
    }

    private void checkPostOwner(CommunityPost post) {
        if (currentUserService.isAdmin()) {
            return;
        }

        Integer currentFemmeId = currentUserService.getCurrentFemmeId();

        if (!post.getFemme().getFemmeId().equals(currentFemmeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private CommunityPostResponseDTO mapToResponse(CommunityPost post) {
        return new CommunityPostResponseDTO(
                post.getPostId(),
                post.getFemme().getFemmeId(),
                post.getFemme().getUsername(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}