package com.hawkins.m3utoolsjpa.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DuplicateService {

	@TrackExecutionTime
	public List<M3UItem[]> findSimilarTvgNames(List<M3UItem> items, double threshold) {
		JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

        return IntStream.range(0, items.size())
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, items.size())
                        .mapToObj(j -> new M3UItem[]{items.get(i), items.get(j)})
                        .filter(pair -> {
                            String tvgName1 = pair[0].getTvgName();
                            String tvgName2 = pair[1].getTvgName();
                            log.info("Comparing {} and {}", tvgName1, tvgName2);
                            return tvgName1 != null && tvgName2 != null && similarity.apply(tvgName1, tvgName2) >= threshold;
                        }))
                .collect(Collectors.toList());
        }
}