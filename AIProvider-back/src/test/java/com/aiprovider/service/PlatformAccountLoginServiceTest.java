package com.aiprovider.service;

import com.aiprovider.model.vo.PlatformLoginSessionVO;
import com.aiprovider.repository.PlatformAccountRepository;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlatformAccountLoginServiceTest {
    @Test void storesXiaohongshuStateOnlyAfterRealConnectedSnapshot(){
        PlatformAccountRepository repository=mock(PlatformAccountRepository.class);XiaohongshuWebAdapter xhs=mock(XiaohongshuWebAdapter.class);DouyinWebAdapter douyin=mock(DouyinWebAdapter.class);PlatformAccountService accounts=mock(PlatformAccountService.class);
        when(repository.findAccount(7L)).thenReturn(Map.of("id",7L,"platform","XIAOHONGSHU","enabled",true));
        when(xhs.poll(7L,"session")).thenReturn(new XiaohongshuWebAdapter.LoginSnapshot("session","CONNECTED",null,"成功","storage-json"));
        PlatformLoginSessionVO result=new PlatformAccountLoginService(repository,accounts,xhs,douyin).poll(7L,"session");
        assertEquals("CONNECTED",result.getStatus());verify(accounts).storeConnectedSecret(7L,"XIAOHONGSHU","STORAGE_STATE","storage-json","扫码会话");
    }

    @Test void refusesLoginAdapterForGemini(){
        PlatformAccountRepository repository=mock(PlatformAccountRepository.class);when(repository.findAccount(8L)).thenReturn(Map.of("id",8L,"platform","GEMINI","enabled",true));
        IllegalStateException error=assertThrows(IllegalStateException.class,()->new PlatformAccountLoginService(repository,mock(PlatformAccountService.class),mock(XiaohongshuWebAdapter.class),mock(DouyinWebAdapter.class)).start(8L));
        assertEquals("ADAPTER_UNAVAILABLE",error.getMessage());
    }
}
