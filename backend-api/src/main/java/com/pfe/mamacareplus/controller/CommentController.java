package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.CommentRequestDTO;
import com.pfe.mamacareplus.dto.CommentResponseDTO;
import com.pfe.mamacareplus.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponseDTO> getCommentsByPostId(@PathVariable Integer postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @PostMapping
    public CommentResponseDTO createComment(@Valid @RequestBody CommentRequestDTO request) {
        return commentService.createComment(request);
    }

    @PutMapping("/{commentId}")
    public CommentResponseDTO updateComment(
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentRequestDTO request
    ) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}