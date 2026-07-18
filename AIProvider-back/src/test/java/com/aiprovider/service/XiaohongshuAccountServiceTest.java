package com.aiprovider.service;

import com.aiprovider.repository.ContentOperationsRepository;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class XiaohongshuAccountServiceTest {
    @Test void encryptsAndStoresSessionAfterQrLogin(){ContentOperationsRepository repository=mock(ContentOperationsRepository.class);ContentPlatformSecretCipher cipher=mock(ContentPlatformSecretCipher.class);XiaohongshuWebAdapter adapter=mock(XiaohongshuWebAdapter.class);when(repository.findAccount(4)).thenReturn(new HashMap<>());when(adapter.poll(4,"session")).thenReturn(new XiaohongshuWebAdapter.LoginSnapshot("session","CONNECTED",null,"成功","storage-json"));when(cipher.encrypt("storage-json")).thenReturn("encrypted");when(repository.updateAccountSession(4,"encrypted","扫码会话")).thenReturn(true);assertEquals("CONNECTED",new XiaohongshuAccountService(repository,cipher,adapter).poll(4,"session").getStatus());verify(repository).updateAccountSession(4,"encrypted","扫码会话");}
}
