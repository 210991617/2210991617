
package com.example.social.controller;

import com.example.social.model.Post;
import com.example.social.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getTopUsers() {
        return service.getTopUsers();
    }

    @GetMapping("/posts")
    public List<Post> getPosts(@RequestParam String type) {
        return service.getPosts(type);
    }
}

