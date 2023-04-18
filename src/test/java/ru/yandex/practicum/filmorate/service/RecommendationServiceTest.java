package ru.yandex.practicum.filmorate.service;

import net.minidev.json.writer.DefaultMapperOrdered;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class RecommendationServiceTest {
    private Map<Long, Map<Long, Double>> marks = new HashMap<>();
    private RecommendationService recommendationService;

    @Test
    void fillMatricesTest() {
        recommendationService = new RecommendationService();
        fillMarks();
        recommendationService.getRecommendation(marks, 1L);
    }

    private void fillMarks() {
        marks.put(1L, new HashMap<Long, Double>() {{
            put(1L, 5.0);
            put(2L, 3.0);
            put(3L, 2.0);}}
        );
        marks.put(2L, new HashMap<Long, Double>() {{
            put(1L, 3.0);
            put(2L, 4.0);}}
        );
        marks.put(3L, new HashMap<Long, Double>() {{
            put(2L, 2.0);
            put(3L, 5.0);}}
        );
    }
}
