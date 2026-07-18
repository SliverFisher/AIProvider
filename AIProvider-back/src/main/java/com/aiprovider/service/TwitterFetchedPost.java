package com.aiprovider.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

class TwitterFetchedPost {
    final String id; final String text; final String url; final LocalDateTime publishedAt; final JsonNode raw;
    TwitterFetchedPost(String id,String text,String url,LocalDateTime publishedAt,JsonNode raw){this.id=id;this.text=text;this.url=url;this.publishedAt=publishedAt;this.raw=raw;}
}
