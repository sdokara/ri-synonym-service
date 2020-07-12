package com.sdokara.ri.synonym;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SynonymServiceTests {
    @Autowired
    private SynonymService synonymService;

    @AfterEach
    public void reset() {
        synonymService.clear();
    }

    @Test
    public void onePair() {
        synonymService.add("a", "b");

        assertSynonyms("a", "b");
        // by definition of a synonym - a word, morpheme, or phrase that means exactly or nearly the same as *another*
        // word, morpheme, or phrase - a word is not a synonym of itself
        assertNotSynonyms("a", "a");
        assertNotSynonyms("b", "b");
        assertNotSynonyms("a", "c");
    }

    @Test
    public void letterCases() {
        synonymService.add("a", "B");
        assertSynonyms("a", "b");
    }

    @Test
    public void idempotency() {
        synonymService.add("a", "b");
        synonymService.add("a", "b");
        synonymService.add("b", "a");
        assertSynonyms("a", "b");
    }

    @Test
    public void twoPairs() {
        synonymService.add("a", "b");
        synonymService.add("c", "d");

        assertSynonyms("a", "b");
        assertSynonyms("c", "d");
        assertNotSynonyms("a", "c");
        assertNotSynonyms("b", "d");
    }

    @Test
    public void transitive() {
        synonymService.add("a", "b");
        synonymService.add("b", "c");

        assertSynonyms("a", "b");
        assertSynonyms("a", "c");
    }

    @Test
    public void transitiveSecond() {
        synonymService.add("a", "b");
        synonymService.add("c", "b");

        assertSynonyms("a", "b");
        assertSynonyms("a", "c");
    }

    @Test
    public void transitivePairs() {
        synonymService.add("a", "b");
        synonymService.add("b", "c");
        synonymService.add("d", "e");
        synonymService.add("e", "f");
        synonymService.add("a", "f");

        assertSynonyms("a", "b", "c", "d", "e", "f");
    }

    @Test
    public void sameWords() {
        assertThrows(IllegalArgumentException.class, () -> synonymService.add("a", "a"));
    }

    /**
     * The concurrency test inserts x random pairs of words in one thread, then shuffles the pairs in the list and
     * repeats the process, and then inserts the shuffled pairs from y threads. All insertions must yield the same
     * end result.
     */
    @Test
    public void threadSafety() throws ExecutionException, InterruptedException {
        final int threadCount = 16;
        final int wordCount = 16384;
        final int pairCount = 4096;

        // generate pairs
        String[] words = new String[wordCount];
        RandomStringGenerator rsg = RandomStringGenerator.builder().withLower().build();
        Random random = new SecureRandom();
        for (int i = 0; i < wordCount; i++) {
            words[i] = rsg.next(random.nextInt(9) + 1);
        }
        List<String[]> pairs = new ArrayList<>(pairCount);
        for (int i = 0; i < pairCount; i++) {
            int j = random.nextInt(words.length), k;
            do {
                k = random.nextInt(words.length);
            } while (words[j].equals(words[k]));
            pairs.add(new String[]{words[j], words[k]});
        }

        // single-threaded
        for (String[] pair : pairs) {
            synonymService.add(pair[0], pair[1]);
        }
        Set<Set<String>> allSingle = new HashSet<>(synonymService.getAll());
        synonymService.clear();

        // single-threaded shuffled
        Collections.shuffle(pairs);
        for (String[] pair : pairs) {
            synonymService.add(pair[0], pair[1]);
        }
        Set<Set<String>> allShuffled = new HashSet<>(synonymService.getAll());
        synonymService.clear();
        assertEquals(allSingle, allShuffled);

        // multi-threaded shuffled
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>(threadCount);
        for (String[] pair : pairs) {
            futures.add(executor.submit(() -> synonymService.add(pair[0], pair[1])));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();
        Set<Set<String>> allMulti = new HashSet<>(synonymService.getAll());
        synonymService.clear();
        assertEquals(allSingle, allMulti);
    }

    private void assertSynonyms(String word1, String word2) {
        assertTrue(synonymService.get(word1).contains(word2));
        assertTrue(synonymService.get(word2).contains(word1));
    }

    private void assertNotSynonyms(String word1, String word2) {
        assertFalse(synonymService.get(word1).contains(word2));
        assertFalse(synonymService.get(word2).contains(word1));
    }

    private void assertSynonyms(String... words) {
        for (String word1 : words) {
            for (String word2 : words) {
                if (!word1.equals(word2)) {
                    assertTrue(synonymService.get(word1).contains(word2));
                }
            }
        }
    }
}
