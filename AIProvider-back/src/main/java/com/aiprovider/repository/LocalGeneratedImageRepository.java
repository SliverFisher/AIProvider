package com.aiprovider.repository;

import com.aiprovider.mapper.LocalGeneratedImageMapper;
import com.aiprovider.model.dto.LocalGeneratedImageItemDTO;
import org.springframework.stereotype.Repository;

@Repository
public class LocalGeneratedImageRepository {
    private final LocalGeneratedImageMapper mapper;
    public LocalGeneratedImageRepository(LocalGeneratedImageMapper mapper) { this.mapper = mapper; }
    public int upsert(String platform, String pathHash, LocalGeneratedImageItemDTO item) { return mapper.upsert(platform, pathHash, item); }
}
