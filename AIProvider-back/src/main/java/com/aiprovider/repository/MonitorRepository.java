package com.aiprovider.repository;

import com.aiprovider.mapper.MonitorMapper;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class MonitorRepository {
    private final MonitorMapper mapper;
    public MonitorRepository(MonitorMapper mapper){this.mapper=mapper;}
    public Map<String,Object> todayOverview(java.time.LocalDateTime since){return mapper.todayOverview(since);}
    public long todayP95(java.time.LocalDateTime since){Long value=mapper.todayP95(since);return value==null?0:value;}
    public List<Map<String,Object>> timeseries(int hours){return mapper.timeseries(hours);}
    public List<Map<String,Object>> timeseriesP95(int hours){return mapper.timeseriesP95(hours);}
    public Map<String,Object> selection(){return mapper.providerSelection();}
    public List<Map<String,Object>> providerActivity(){return mapper.providerActivity();}
    public List<Map<String,Object>> failures(int hours,String provider,String model,int limit,int offset){return mapper.failures(hours,provider,model,limit,offset);}
    public long failureCount(int hours,String provider,String model){return mapper.failureCount(hours,provider,model);}
    public int deleteExpired(int days){return mapper.deleteExpired(days);}
}
