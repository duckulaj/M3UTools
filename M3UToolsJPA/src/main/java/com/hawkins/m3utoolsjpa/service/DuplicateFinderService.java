
package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DuplicateFinderService {

    static DownloadProperties dp = DownloadProperties.getInstance();

    public void findSimilarMovies() {
        String parentDirectory = dp.getDownloadPath() + File.separator + Constants.FOLDER_MOVIES;
        List<String> movieTitles;

        try {
            movieTitles = Files.list(Paths.get(parentDirectory))
                               .parallel()
                               .filter(Files::isDirectory)
                               .map(Path::getFileName)
                               .map(Path::toString)
                               .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error listing movie directories", e);
            return;
        }

        // Normalize titles to lowercase for comparison
        Set<String> normalizedTitles = movieTitles.stream()
                                                  .map(String::toLowerCase)
                                                  .collect(Collectors.toSet());

        // Set the similarity threshold
        double similarityThreshold = 0.90;

        // Find duplicates with similarity >= 0.90
        List<String> duplicates = findDuplicates(normalizedTitles, similarityThreshold);

        // Log the results
        log.info("Duplicate movie titles:");
        duplicates.forEach(log::info);
    }

    private static List<String> findDuplicates(Set<String> normalizedTitles, double similarityThreshold) {
        Map<String, String> duplicates = new ConcurrentHashMap<>();
        Map<String, Double> similarityCache = new ConcurrentHashMap<>();

        normalizedTitles.parallelStream().forEach(title1 -> {
            normalizedTitles.parallelStream().forEach(title2 -> {
                if (!title1.equals(title2) && !duplicates.containsKey(title1) && !duplicates.containsKey(title2)) {
                    String key = title1 + "|" + title2;
                    double score = similarityCache.computeIfAbsent(key, k -> getSimilarityScore(title1, title2));
                    if (score >= similarityThreshold) {
                        duplicates.put(title1, title2);
                    }
                }
            });
        });

        return duplicates.entrySet().stream()
                         .map(entry -> "(" + entry.getKey() + ", " + entry.getValue() + ")")
                         .collect(Collectors.toList());
    }

    private static double getSimilarityScore(String title1, String title2) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(title1, title2);
        int maxLength = Math.max(title1.length(), title2.length());
        return 1.0 - ((double) distance / maxLength);
    }
}
