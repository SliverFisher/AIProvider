package com.aiprovider.service;

import com.aiprovider.repository.MonitorRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MonitorRetentionService {
    private static final Logger log=LogManager.getLogger(MonitorRetentionService.class);
    private final MonitorRepository repository; private final int retentionDays;
    public MonitorRetentionService(MonitorRepository repository,@Value("${monitor.detail-retention-days:30}") int retentionDays){this.repository=repository;this.retentionDays=Math.max(30,retentionDays);}
    @Scheduled(cron="0 25 3 * * *") public void cleanup(){try{int deleted=repository.deleteExpired(retentionDays);if(deleted>0)log.info("Removed {} expired AI monitor records using {} day retention",deleted,retentionDays);}catch(Exception exception){log.warn("AI monitor retention cleanup failed code=DATABASE_CLEANUP_ERROR");}}
}
