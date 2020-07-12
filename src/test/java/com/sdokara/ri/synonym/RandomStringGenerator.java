package com.sdokara.ri.synonym;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.Random;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomStringGenerator {
    private final Random random = new SecureRandom();
    private final char[] chars;

    public String next(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = this.chars[random.nextInt(this.chars.length)];
        }
        return new String(chars);
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private static final char[] UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        private static final char[] LOWER = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        private static final char[] NUMBERS = "0123456789".toCharArray();
        private static final char[] SPECIAL = "!#$%&".toCharArray();
        private static final char[] EXTRA_SPECIAL = "\"'()*+,-./:;<=>?@".toCharArray();

        private char[] chars = new char[0];

        private Builder() {
        }

        public Builder withUpper() {
            return with(UPPER);
        }

        public Builder withLower() {
            return with(LOWER);
        }

        public Builder withNumbers() {
            return with(NUMBERS);
        }

        public Builder withSpecial() {
            return with(SPECIAL);
        }

        public Builder withExtraSpecial() {
            return with(EXTRA_SPECIAL);
        }

        public Builder with(char[] chars) {
            char[] newChars = new char[this.chars.length + chars.length];
            System.arraycopy(this.chars, 0, newChars, 0, this.chars.length);
            System.arraycopy(chars, 0, newChars, this.chars.length, chars.length);
            this.chars = newChars;
            return this;
        }

        public RandomStringGenerator build() {
            return new RandomStringGenerator(chars);
        }
    }
}
