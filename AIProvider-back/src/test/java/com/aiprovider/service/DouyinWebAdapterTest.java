package com.aiprovider.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DouyinWebAdapterTest {
    @Test void recognizesOnlyAuthenticatedCreatorRoutes(){
        assertTrue(DouyinWebAdapter.isAuthenticatedUrl("https://creator.douyin.com/creator-micro/content/manage"));
        assertFalse(DouyinWebAdapter.isAuthenticatedUrl("https://creator.douyin.com/creator-micro/login"));
    }
    @Test void recognizesKnownAuthenticatedCookies(){
        assertTrue(DouyinWebAdapter.isAuthenticatedCookieName("sessionid"));
        assertTrue(DouyinWebAdapter.isAuthenticatedCookieName("passport_auth_status"));
        assertFalse(DouyinWebAdapter.isAuthenticatedCookieName("ttwid"));
    }
}
