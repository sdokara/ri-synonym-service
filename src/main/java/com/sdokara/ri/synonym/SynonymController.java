package com.sdokara.ri.synonym;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/synonyms")
public class SynonymController {
    private final SynonymService synonymService;

    public SynonymController(SynonymService synonymService) {
        this.synonymService = synonymService;
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestParam("words[]") String[] words) {
        try {
            synonymService.add(words);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Set<String>> get(@RequestParam String word) {
        if (word.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "String cannot be blank");
        }
        return ResponseEntity.ok(synonymService.get(word));
    }
}
