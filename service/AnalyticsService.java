package com.example.socialmedia.service;

import com.example.socialmedia.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    public Map<String, List<?>> getMockData() {
        // Simulated test server response
        List<User> users = List.of(
            new User(1, "Alice"),
            new User(2, "Bob"),
            new User(3, "Charlie")
        );

        List<Post> posts = List.of(
            new Post(101, 1, "2025-04-15T08:00:00Z"),
            new Post(102, 2, "2025-04-15T09:00:00Z"),
            new Post(103, 1, "2025-04-15T10:00:00Z")
        );

        List<Comment> comments = List.of(
            new Comment(101), new Comment(101), new Comment(103),
            new Comment(102), new Comment(102), new Comment(102)
        );

        Map<String, List<?>> data = new HashMap<>();
        data.put("users", users);
        data.put("posts", posts);
        data.put("comments", comments);

        return data;
    }

    public List<Map<String, Object>> getTopUsers() {
        Map<String, List<?>> data = getMockData();
        List<User> users = (List<User>) data.get("users");
        List<Post> posts = (List<Post>) data.get("posts");
        List<Comment> comments = (List<Comment>) data.get("comments");

        Map<Integer, Long> commentCountByPost = comments.stream()
                .collect(Collectors.groupingBy(Comment::getPostId, Collectors.counting()));

        Map<Integer, Long> userCommentCount = new HashMap<>();
        for (Post post : posts) {
            long count = commentCountByPost.getOrDefault(post.getId(), 0L);
            userCommentCount.put(post.getUserId(), userCommentCount.getOrDefault(post.getUserId(), 0L) + count);
        }

        return users.stream()
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getId());
                    userData.put("name", user.getName());
                    userData.put("commentCount", userCommentCount.getOrDefault(user.getId(), 0L));
                    return userData;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("commentCount"), (Long) a.get("commentCount")))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<Post> getPostsByType(String type) {
        Map<String, List<?>> data = getMockData();
        List<Post> posts = (List<Post>) data.get("posts");
        List<Comment> comments = (List<Comment>) data.get("comments");

        Map<Integer, Long> commentCountByPost = comments.stream()
                .collect(Collectors.groupingBy(Comment::getPostId, Collectors.counting()));

        if ("popular".equalsIgnoreCase(type)) {
            long maxCount = commentCountByPost.values().stream().max(Long::compare).orElse(0L);
            return posts.stream()
                    .filter(p -> commentCountByPost.getOrDefault(p.getId(), 0L) == maxCount)
                    .collect(Collectors.toList());
        } else if ("latest".equalsIgnoreCase(type)) {
            return posts.stream()
                    .sorted((a, b) -> Instant.parse(b.getTimestamp()).compareTo(Instant.parse(a.getTimestamp())))
                    .limit(5)
                    .collect(Collectors.toList());
        }

        return List.of(); // Invalid type
    }
}
