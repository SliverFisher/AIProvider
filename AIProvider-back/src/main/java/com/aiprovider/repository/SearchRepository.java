package com.aiprovider.repository;

import com.aiprovider.mapper.SearchMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SearchRepository {

    private final SearchMapper searchMapper;

    public SearchRepository(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
    }

    public List<Map<String, Object>> searchTable(String table, String textCol, String subCol,
                                                  String like, int limit) {
        try {
            if (subCol != null) {
                return searchMapper.searchWithSub(table, textCol, subCol, like, limit);
            } else {
                return searchMapper.searchWithoutSub(table, textCol, like, limit);
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}