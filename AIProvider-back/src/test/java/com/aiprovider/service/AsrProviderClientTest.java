package com.aiprovider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AsrProviderClientTest {
    @TempDir Path temp;

    @Test void sendsAuthenticatedMultipartAndParsesSegmentDuration() throws Exception {
        RestTemplate http=mock(RestTemplate.class);String endpoint="https://api.groq.test/openai/v1/audio/transcriptions";
        when(http.exchange(eq(endpoint),eq(HttpMethod.POST),any(HttpEntity.class),eq(String.class))).thenReturn(ResponseEntity.ok("{\"text\":\"今天天气很好\",\"segments\":[{\"end\":2.5},{\"end\":8.42}]}"));
        AsrProviderClient client=new AsrProviderClient(new ObjectMapper(),http,endpoint,"test-secret");Path audio=temp.resolve("voice.webm");Files.write(audio,new byte[]{1,2,3});

        AsrProviderClient.Result result=client.transcribe(audio,"whisper-large-v3","zh");

        assertEquals("今天天气很好",result.getText());assertEquals(8420L,result.getDurationMs());ArgumentCaptor<HttpEntity> entity=ArgumentCaptor.forClass(HttpEntity.class);verify(http).exchange(eq(endpoint),eq(HttpMethod.POST),entity.capture(),eq(String.class));assertEquals("Bearer test-secret",entity.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));assertEquals(MediaType.MULTIPART_FORM_DATA,entity.getValue().getHeaders().getContentType());
    }

    @Test void rejectsEmptyProviderText() throws Exception {
        RestTemplate http=mock(RestTemplate.class);String endpoint="https://api.groq.test/openai/v1/audio/transcriptions";when(http.exchange(eq(endpoint),eq(HttpMethod.POST),any(HttpEntity.class),eq(String.class))).thenReturn(ResponseEntity.ok("{\"text\":\"  \"}"));AsrProviderClient client=new AsrProviderClient(new ObjectMapper(),http,endpoint,"test-secret");Path audio=temp.resolve("voice.wav");Files.write(audio,new byte[]{1});
        AsrProviderException error=assertThrows(AsrProviderException.class,()->client.transcribe(audio,"whisper-large-v3","zh"));assertEquals("ASR_EMPTY_TEXT",error.getCode());
    }
}
