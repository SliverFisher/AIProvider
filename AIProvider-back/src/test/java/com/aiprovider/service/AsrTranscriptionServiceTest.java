package com.aiprovider.service;

import com.aiprovider.mapper.AsrRecordMapper;
import com.aiprovider.model.vo.AsrRecordVO;
import com.aiprovider.repository.AsrRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AsrTranscriptionServiceTest {
    @TempDir Path temp;

    @Test void persistsAndReturnsSuccessfulTranscriptionWithoutLoggingContent() throws Exception {
        AsrRecordRepository repository=mock(AsrRecordRepository.class);AsrProviderClient client=mock(AsrProviderClient.class);AsrTranscriptionService service=service(repository,client);when(repository.findByRequestId(anyString())).thenReturn(null);
        when(repository.findCharacterName("character_001")).thenReturn("小爱");doAnswer(invocation->{AsrRecordMapper.Record row=invocation.getArgument(0);row.setId(7L);return 1;}).when(repository).insert(any());when(repository.assignRecordId(eq(7L),anyString())).thenReturn(1);when(client.transcribe(any(),eq("whisper-large-v3"),eq("zh"))).thenReturn(new AsrProviderClient.Result("今天天气很好",8420L));when(repository.markSuccess(eq(7L),eq("今天天气很好"),eq(8420L),anyLong())).thenReturn(1);when(repository.findByRecordId(anyString())).thenReturn(successRow());
        AsrRecordVO result=service.transcribe(audio(),"character_001","session_001","zh","request_001");
        assertEquals("今天天气很好",result.getRecognizedText());assertEquals("小爱",result.getCharacterNameSnapshot());verify(client).transcribe(any(),eq("whisper-large-v3"),eq("zh"));verify(repository).markSuccess(eq(7L),eq("今天天气很好"),eq(8420L),anyLong());
    }

    @Test void replaysCompletedRequestWithoutCallingProviderAgain() throws Exception {
        AsrRecordRepository repository=mock(AsrRecordRepository.class);AsrProviderClient client=mock(AsrProviderClient.class);AsrTranscriptionService service=service(repository,client);when(repository.findByRequestId("request_001")).thenReturn(successRow());
        AsrRecordVO result=service.transcribe(audio(),"character_001",null,"zh","request_001");
        assertEquals("asr_20260722_000007",result.getRecordId());verifyNoInteractions(client);verify(repository,never()).insert(any());
    }

    @Test void recordsProviderFailureAndReturnsStablePublicError() throws Exception {
        AsrRecordRepository repository=mock(AsrRecordRepository.class);AsrProviderClient client=mock(AsrProviderClient.class);AsrTranscriptionService service=service(repository,client);when(repository.findByRequestId(anyString())).thenReturn(null);doAnswer(invocation->{AsrRecordMapper.Record row=invocation.getArgument(0);row.setId(7L);return 1;}).when(repository).insert(any());when(repository.assignRecordId(eq(7L),anyString())).thenReturn(1);when(client.transcribe(any(),anyString(),anyString())).thenThrow(new AsrProviderException("ASR_PROVIDER_HTTP_429","upstream rate limit",null));when(repository.markFailed(eq(7L),anyLong(),eq("ASR_PROVIDER_HTTP_429"),eq("语音识别失败"))).thenReturn(1);
        AsrTranscriptionException error=assertThrows(AsrTranscriptionException.class,()->service.transcribe(audio(),"character_001",null,"zh","request_001"));
        assertEquals("ASR_TRANSCRIPTION_FAILED",error.getCode());assertEquals("request_001",error.getRequestId());verify(repository).markFailed(eq(7L),anyLong(),eq("ASR_PROVIDER_HTTP_429"),eq("语音识别失败"));
    }

    private AsrTranscriptionService service(AsrRecordRepository repository,AsrProviderClient client){return new AsrTranscriptionService(repository,client,temp.toString(),"groq","whisper-large-v3",26214400L);}
    private MockMultipartFile audio(){return new MockMultipartFile("audio","voice.webm","audio/webm",new byte[]{1,2,3});}
    private Map<String,Object> successRow(){Map<String,Object> row=new HashMap<>();row.put("recordId","asr_20260722_000007");row.put("requestId","request_001");row.put("characterId","character_001");row.put("characterNameSnapshot","小爱");row.put("sessionId","session_001");row.put("audioFormat","webm");row.put("audioSize",3L);row.put("audioDurationMs",8420L);row.put("recognizedText","今天天气很好");row.put("provider","groq");row.put("model","whisper-large-v3");row.put("language","zh");row.put("processingTimeMs",1260L);row.put("status","SUCCESS");row.put("createdAt",LocalDateTime.of(2026,7,22,12,0));return row;}
}
