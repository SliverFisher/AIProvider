package com.aiprovider.service;

import com.aiprovider.repository.BusinessInsightsRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BusinessInsightsService {

    private final BusinessInsightsRepository insightsRepo;

    public BusinessInsightsService(BusinessInsightsRepository insightsRepo) {
        this.insightsRepo = insightsRepo;
    }

    public Map<String, Object> getCommand() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("counts", getCounts());
        result.put("runtime", insightsRepo.runtimeState());
        result.put("reminders", insightsRepo.activeReminders());
        result.put("notes", insightsRepo.recentNotes());
        result.put("voice", insightsRepo.recentVoiceLogs());
        result.put("videos", insightsRepo.recentVideos());
        result.put("remoteVideos", insightsRepo.recentRemoteVideos());
        result.put("voiceRoles", insightsRepo.voiceRoles());
        return result;
    }

    private Map<String, Long> getCounts() {
        return insightsRepo.countAll(Arrays.asList(
            "NotebookNotes", "Reminders", "VoiceConversations", "VoiceTriggerLogs",
            "VoiceRoles", "VideoItems", "RemoteVideoItems", "AiConversations",
            "ProactiveTriggerRules", "ProactiveTriggerStates"
        ));
    }
}