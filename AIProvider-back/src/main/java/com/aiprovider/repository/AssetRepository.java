package com.aiprovider.repository;

import com.aiprovider.mapper.AssetMapper;
import com.aiprovider.model.dto.AssetItemDTO;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class AssetRepository {
    private final AssetMapper mapper;
    public AssetRepository(AssetMapper mapper) { this.mapper = mapper; }
    public int upsert(String platform, String pathHash, AssetItemDTO item) { return mapper.upsert(platform, pathHash, item); }
    public List<Map<String,Object>> findPage(String platform, int limit, int offset) { return mapper.findPage(platform, limit, offset); }
    public List<Map<String,Object>> findByPathHashes(String platform, List<String> pathHashes) { return mapper.findByPathHashes(platform, pathHashes); }
    public long count(String platform) { return mapper.count(platform); }
    public Map<String,Object> findById(long id) { return mapper.findById(id); }
    public int deleteByIds(String platform, List<Long> ids) { return mapper.deleteByIds(platform, ids); }
}
