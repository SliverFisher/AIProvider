package com.aiprovider.service;

import com.aiprovider.model.dto.PromptTranslationDTO;
import com.aiprovider.model.vo.PromptTranslationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibreTranslateService {
    private static final Logger log = LoggerFactory.getLogger(LibreTranslateService.class);
    private final String endpoint;
    private final String apiKey;
    private final RestTemplate http;

    @Autowired
    public LibreTranslateService(@Value("${prompt-translation.libretranslate-url:http://127.0.0.1:5000/translate}") String endpoint,
                                 @Value("${prompt-translation.libretranslate-api-key:}") String apiKey,
                                 @Value("${prompt-translation.connect-timeout-ms:3000}") int connectTimeoutMs,
                                 @Value("${prompt-translation.read-timeout-ms:60000}") int readTimeoutMs) {
        this(endpoint, apiKey, rest(connectTimeoutMs, readTimeoutMs));
    }

    LibreTranslateService(String endpoint, String apiKey, RestTemplate http) {
        if (endpoint == null || endpoint.trim().isEmpty()) throw new IllegalArgumentException("LibreTranslate API 地址未配置");
        URI uri;
        try { uri = URI.create(endpoint.trim()); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("LibreTranslate API 地址无效", exception); }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
            throw new IllegalArgumentException("LibreTranslate API 地址必须使用 HTTP 或 HTTPS");
        this.endpoint = uri.toString();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.http = http;
    }

    public PromptTranslationVO translateToChinese(PromptTranslationDTO dto) {
        if (dto == null) throw new IllegalArgumentException("长文翻译内容不能为空");
        String positive = validate(dto.getPositivePrompt(), "长文正向描述", false);
        String negative = validate(dto.getNegativePrompt(), "长文反向约束", true);
        List<String> source = new ArrayList<>();
        source.add(positive);
        if (!negative.isEmpty()) source.add(negative);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("q", source);
        request.put("source", "auto");
        request.put("target", "zh");
        request.put("format", "text");
        if (!apiKey.isEmpty()) request.put("api_key", apiKey);

        try {
            ResponseEntity<Map> response = http.postForEntity(endpoint, request, Map.class);
            List<String> translated = translatedTexts(response.getBody(), source.size());
            log.info("prose_prompt_translated operation=translate targetLanguage=zh requestedCount={} affectedRows={} sourceCharacters={}",
                    source.size(), translated.size(), positive.length() + negative.length());
            return new PromptTranslationVO(translated.get(0), negative.isEmpty() ? "" : translated.get(1));
        } catch (RestClientException exception) {
            log.warn("prose_prompt_translation_failed operation=translate targetLanguage=zh requestedCount={} affectedRows=0 sourceCharacters={} errorType={}",
                    source.size(), positive.length() + negative.length(), exception.getClass().getSimpleName());
            throw new PromptTranslationException("LibreTranslate 长文翻译调用失败，请检查翻译 API 配置和服务状态", exception);
        }
    }

    private List<String> translatedTexts(Map body, int requestedCount) {
        Object value = body == null ? null : body.get("translatedText");
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) if (item instanceof String) result.add((String) item);
        } else if (value instanceof String && requestedCount == 1) result.add((String) value);
        if (result.size() != requestedCount || result.stream().anyMatch(String::isEmpty)) {
            log.warn("prose_prompt_translation_mismatch operation=translate targetLanguage=zh requestedCount={} affectedRows={}", requestedCount, result.size());
            throw new PromptTranslationException("LibreTranslate 返回的译文数量不完整");
        }
        return result;
    }

    private String validate(String value, String label, boolean allowEmpty) {
        if (value == null) throw new IllegalArgumentException(label + "不能为空");
        if (value.length() > 16000) throw new IllegalArgumentException(label + "不能超过 16000 字符");
        String clean = value.trim();
        if (!allowEmpty && clean.isEmpty()) throw new IllegalArgumentException(label + "不能为空");
        return clean;
    }

    private static RestTemplate rest(int connectTimeoutMs, int readTimeoutMs) {
        if (connectTimeoutMs < 100 || connectTimeoutMs > 30000 || readTimeoutMs < 1000 || readTimeoutMs > 180000)
            throw new IllegalArgumentException("长文翻译超时配置不合法");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
