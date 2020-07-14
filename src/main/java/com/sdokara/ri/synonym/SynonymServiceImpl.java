package com.sdokara.ri.synonym;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A naive implementation of a {@link SynonymService} that uses translation. Each word that is unique by meaning is
 * assigned a unique ID of type {@link Long} as its <i>machine meaning</i>. Each word in a synonym set therefore gets
 * assigned the same ID, and gets stored in the same {@link Set} in a {@link Map} entry keyed by that ID. <br/>
 * Implementation is thread-safe, with high performance penalties on inserts, but fast reads.
 */
@Service
public class SynonymServiceImpl implements SynonymService {
    private final Map<Long, Set<String>> keyWordsMap = new HashMap<>();
    private final Map<String, Long> wordKeyMap = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    /* Having concurrent hashmaps is not enough to ensure thread-safety as the methods perform multiple operations
       with multiple entries within the maps, so the solution here is to use a read-write lock with ordinary maps. */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    @Override
    public void add(String word1, String word2) throws IllegalArgumentException {
        if (word1 == null || word2 == null || word1.isBlank() || word2.isBlank()) {
            throw new IllegalArgumentException("Words cannot be null nor blank");
        }
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (word1.equals(word2)) {
            throw new IllegalArgumentException("A word cannot be a synonym of itself");
        }

        writeLock.lock();
        try {
            Long key1 = wordKeyMap.get(word1);
            Long key2 = wordKeyMap.get(word2);
            if (key1 == null && key2 == null) {
                // neither word1 nor word2 are present in the dictionary
                insert(word1, word2);
            } else if (key1 != null && key2 == null) {
                // word1 is present, but word2 is not
                link(key1, word2);
            } else if (key1 == null) {
                // word1 is not present, but word2 is (the condition key2 != null is redundant)
                link(key2, word1);
            } else if (!key1.equals(key2)) {
                // both words are present with different keys, so the two synonym sets must be merged
                relink(word1, word2);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void add(String... words) throws IllegalArgumentException {
        if (words == null || words.length < 2) {
            throw new IllegalArgumentException("At least two words must be passed");
        }
        words = Arrays.stream(words).map(String::toLowerCase).toArray(String[]::new);
        if (Set.of(words).size() != words.length) {
            throw new IllegalArgumentException("Words contain duplicates");
        }
        for (int i = 0; i < words.length - 1; i++) {
            add(words[i], words[i + 1]);
        }
    }

    private void insert(String word1, String word2) {
        Long key = nextKey();
        link(key, word1);
        link(key, word2);
    }

    private void link(Long key, String word) {
        wordKeyMap.put(word, key);
        keyWordsMap.compute(key, (l, words) -> {
            if (words == null) {
                words = new HashSet<>();
            }
            words.add(word);
            return words;
        });
    }

    private void relink(String word1, String word2) {
        Long key1 = wordKeyMap.remove(word1);
        Long key2 = wordKeyMap.remove(word2);
        Set<String> words1 = keyWordsMap.remove(key1);
        Set<String> words2 = keyWordsMap.remove(key2);

        words1.addAll(words2);
        Long key = nextKey();
        for (String word : words1) {
            wordKeyMap.put(word, key);
        }
        keyWordsMap.put(key, words1);
    }

    private long nextKey() {
        return sequence.incrementAndGet();
    }


    @Override
    public Set<String> get(String word) {
        readLock.lock();
        try {
            word = word.toLowerCase();
            Long key = wordKeyMap.get(word);
            if (key == null) {
                return Collections.emptySet();
            }
            // copying data to ensure external immutability
            Set<String> words = new HashSet<>(keyWordsMap.get(key));
            if (!words.isEmpty()) {
                words.remove(word);
            }
            return words;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Set<String>> getAll() {
        readLock.lock();
        try {
            // copying data to ensure external immutability
            return keyWordsMap.values().stream()
                    .map(HashSet::new)
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            keyWordsMap.clear();
            wordKeyMap.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
