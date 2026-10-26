package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.CommentRequestDTO;
import com.pfe.mamacareplus.dto.CommentResponseDTO;
import com.pfe.mamacareplus.entity.Comment;
import com.pfe.mamacareplus.entity.CommunityPost;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.repository.CommentRepository;
import com.pfe.mamacareplus.repository.CommunityPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CurrentUserService currentUserService;

    public CommentService(
            CommentRepository commentRepository,
            CommunityPostRepository communityPostRepository,
            CurrentUserService currentUserService
    ) {
        this.commentRepository = commentRepository;
        this.communityPostRepository = communityPostRepository;
        this.currentUserService = currentUserService;
    }

    public List<CommentResponseDTO> getCommentsByPostId(Integer postId) {
        return commentRepository.findByPostPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CommentResponseDTO createComment(CommentRequestDTO request) {

        if (request.getPostId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post ID is required");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content is required");
        }

        CommunityPost post = communityPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Femme currentFemme = currentUserService.getCurrentFemme();

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setFemme(currentFemme);
        comment.setContent(request.getContent());

        return mapToResponse(commentRepository.save(comment));
    }

    public CommentResponseDTO updateComment(Integer commentId, CommentRequestDTO request) {

        Comment comment = findCommentById(commentId);

        checkCommentOwner(comment);

        if (request.getContent() != null && !request.getContent().isBlank()) {
            comment.setContent(request.getContent());
        }

        return mapToResponse(commentRepository.save(comment));
    }

    public void deleteComment(Integer commentId) {

        Comment comment = findCommentById(commentId);

        checkCommentOwner(comment);

        commentRepository.delete(comment);
    }

    private Comment findCommentById(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private void checkCommentOwner(Comment comment) {

        if (currentUserService.isAdmin()) {
            return;
        }

        Integer currentFemmeId = currentUserService.getCurrentFemmeId();

        if (!comment.getFemme().getFemmeId().equals(currentFemmeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private CommentResponseDTO mapToResponse(Comment comment) {
        return new CommentResponseDTO(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getFemme().getFemmeId(),
                comment.getFemme().getUsername(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}