package com.example.social.service;

import com.example.social.model.Post;
import com.example.social.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final String BASE_URL = "http://20.244.56.144/evaluation-service";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<User> fetchUsers() {
        Map<String, Object> result = restTemplate.getForObject(BASE_URL + "/users", Map.class);
        Map<String, String> usersMap = (Map<String, String>) result.get("users");

        return usersMap.entrySet().stream()
                .map(entry -> new User(Integer.parseInt(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<Post> fetchPostsByUser(int userId) {
        Map<String, Object> response = restTemplate.getForObject(
                BASE_URL + "/users/" + userId + "/posts", Map.class
        );
        List<Map<String, Object>> rawPosts = (List<Map<String, Object>>) response.get("posts");

        List<Post> posts = new ArrayList<>();
        for (Map<String, Object> rawPost : rawPosts) {
            Post post = new Post();
            post.setId((Integer) rawPost.get("id"));
            post.setUserId((Integer) rawPost.get("userid"));
            post.setContent((String) rawPost.get("content"));
            posts.add(post);
        }
        return posts;
    }

    public List<Map<String, Object>> getTopUsers() {
        List<User> users = fetchUsers();

        Map<Integer, Integer> commentCounts = new HashMap<>();

        for (User user : users) {
            List<Post> posts = fetchPostsByUser(user.getId());
            int totalComments = posts.size(); // Assuming each post = 1 comment (as comment data is missing)
            commentCounts.put(user.getId(), totalComments);
        }

        return users.stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getId());
                    userInfo.put("name", user.getName());
                    userInfo.put("commentCount", commentCounts.get(user.getId()));
                    return userInfo;
                })
                .sorted((a, b) -> (Integer) b.get("commentCount") - (Integer) a.get("commentCount"))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<Post> getPosts(String type) {
        List<User> users = fetchUsers();
        List<Post> allPosts = new ArrayList<>();

        for (User user : users) {
            allPosts.addAll(fetchPostsByUser(user.getId()));
        }

        if ("latest".equalsIgnoreCase(type)) {
            return allPosts.stream()
                    .sorted(Comparator.comparing(Post::getId).reversed()) // assuming ID is incremental
                    .limit(5)
                    .collect(Collectors.toList());
        } else if ("popular".equalsIgnoreCase(type)) {
            Map<String, Long> freqMap = allPosts.stream()
                    .collect(Collectors.groupingBy(Post::getContent, Collectors.counting()));
            long max = freqMap.values().stream().max(Long::compareTo).orElse(0L);

            return allPosts.stream()
                    .filter(post -> freqMap.get(post.getContent()) == max)
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
