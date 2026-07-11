package com.aiprovider.service;

import com.aiprovider.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {

    private final SearchRepository searchRepo;

    public SearchService(SearchRepository searchRepo) {
        this.searchRepo = searchRepo;
    }

    public List<Map<String, Object>> search(String q, int limit) {
        if (q == null || q.trim().isEmpty() || q.length() < 2) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        String like = "%" + q + "%";

        results.addAll(searchRepo.searchTable("ChatMessages", "Content", "Role", like, limit));
        results.addAll(searchRepo.searchTable("LlmCallLogs", "UserPrompt", "Model", like, limit));
        results.addAll(searchRepo.searchTable("LlmChatMessages", "Content", "Role", like, limit));
        results.addAll(searchRepo.searchTable("NotebookNotes", "Title", "ContentPlainText", like, limit));
        results.addAll(searchRepo.searchTable("Reminders", "Title", null, like, limit));
        results.addAll(searchRepo.searchTable("DesktopContextSnapshots", "ForegroundWindowTitle", "ForegroundProcessName", like, limit));
        results.addAll(searchRepo.searchTable("ProactiveBroadcastTriggerLogs", "Message", "EventType", like, limit));

        results.sort((a, b) -> {
            Object at = a.get("_matchField");
            Object bt = b.get("_matchField");
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.toString().compareTo(at.toString());
        });

        return results.subList(0, Math.min(limit, results.size()));
    }
}