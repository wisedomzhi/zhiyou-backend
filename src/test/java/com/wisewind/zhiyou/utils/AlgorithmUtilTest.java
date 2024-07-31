package com.wisewind.zhiyou.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmUtilTest {

    @Test
    void minDistance() {
        String word1 = "testword";
        String word2 = "testword2";
        String word3 = "testword4test";
        int score1 = AlgorithmUtil.minDistance(word1, word2);
        int score2 = AlgorithmUtil.minDistance(word1, word3);
        int score3 = AlgorithmUtil.minDistance(word3, word2);
        System.out.println(score1 + "," + score2 + "," + score3);
    }

    @Test
    void minDistance4Tags() {
        List<String> tags1 = Arrays.asList("java", "python", "male");
        List<String> tags2 = Arrays.asList("java", "c++", "male");
        List<String> tags3 = Arrays.asList("java", "c++", "female");
        int score1 = AlgorithmUtil.minDistance4Tags(tags1, tags2);
        int score2 = AlgorithmUtil.minDistance4Tags(tags1, tags3);
        int score3 = AlgorithmUtil.minDistance4Tags(tags3, tags2);
        System.out.println(score1 + "," + score2 + "," + score3);
    }
}
