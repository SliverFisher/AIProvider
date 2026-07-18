package com.aiprovider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

class TwitterPublicWebClientLiveTest {
    @Test
    @EnabledIfEnvironmentVariable(named="TWITTER_PUBLIC_WEB_LIVE_TEST",matches="true")
    void fetchesLatestPublicPostForElonMusk(){String executable=System.getenv("TWITTER_PUBLIC_WEB_LIVE_BROWSER");assertNotNull(executable,"TWITTER_PUBLIC_WEB_LIVE_BROWSER is required");TwitterFetchedPost post=new TwitterPublicWebClient(new ObjectMapper(),true,30000,executable).fetchLatest("elonmusk");assertNotNull(post);assertTrue(post.id.matches("[0-9]+"));assertFalse(post.text.trim().isEmpty());assertTrue(post.url.endsWith(post.id));}
}
