package com.aiprovider.repository;

import com.aiprovider.mapper.SyncMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SyncRepositoryTest {

    @Test
    void convertsIsoOffsetTextForMysqlDatetimeColumns() throws Exception {
        SyncMapper mapper = mock(SyncMapper.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(mapper.getTableColumns("maid_Test")).thenReturn(Arrays.asList(
            column("Id", "PRI", "integer"),
            column("GeneratedAt", "", "datetime")
        ));

        new SyncRepository(mapper, jdbc).upsert(
            "maid_Test",
            new ObjectMapper().readTree("{\"Id\":1,\"GeneratedAt\":\"2026-07-16T14:57:15.527815Z\"}")
        );

        ArgumentCaptor<Object> id = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Object> generatedAt = ArgumentCaptor.forClass(Object.class);
        verify(jdbc).update(anyString(), id.capture(), generatedAt.capture());
        assertEquals(1L, id.getValue());
        assertEquals(LocalDateTime.of(2026, 7, 16, 14, 57, 15, 527_815_000), generatedAt.getValue());
    }

    private static Map<String, Object> column(String name, String key, String type) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COLUMN_NAME", name);
        row.put("COLUMN_KEY", key);
        row.put("DATA_TYPE", type);
        return row;
    }
}
