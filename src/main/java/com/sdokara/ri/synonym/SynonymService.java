package com.sdokara.ri.synonym;

import java.util.List;
import java.util.Set;

public interface SynonymService {
    /**
     * Adds the two words as synonyms in the dictionary. Words are case-insensitive.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @throws IllegalArgumentException if {@code word1} equals {@code word2}, as a word cannot be a synonym of itself
     */
    void add(String word1, String word2) throws IllegalArgumentException;

    /**
     * Adds all the words as synonyms in the dictionary. Words are case-insensitive.
     *
     * @param words the words
     * @throws IllegalArgumentException if duplicate words are found
     */
    void add(String... words) throws IllegalArgumentException;

    /**
     * @param word the word to search by
     * @return a set of words that are synonymous to {@code word}
     */
    Set<String> get(String word);

    /**
     * @return all synonym sets
     */
    List<Set<String>> getAll();

    /**
     * Clears the entire dictionary.
     */
    void clear();
}
