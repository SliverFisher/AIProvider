package com.aiprovider.service;

import com.aiprovider.model.vo.XhsLoginSessionVO;
import com.aiprovider.repository.ContentOperationsRepository;
import org.springframework.stereotype.Service;

@Service
public class XiaohongshuAccountService {
    private final ContentOperationsRepository repository;private final ContentPlatformSecretCipher cipher;private final XiaohongshuWebAdapter adapter;
    public XiaohongshuAccountService(ContentOperationsRepository repository,ContentPlatformSecretCipher cipher,XiaohongshuWebAdapter adapter){this.repository=repository;this.cipher=cipher;this.adapter=adapter;}
    public XhsLoginSessionVO startLogin(long accountId){requiredAccount(accountId);XiaohongshuWebAdapter.LoginSnapshot snapshot=adapter.startLogin(accountId);return vo(snapshot);}
    public XhsLoginSessionVO poll(long accountId,String sessionId){requiredAccount(accountId);XiaohongshuWebAdapter.LoginSnapshot snapshot=adapter.poll(accountId,sessionId);if("CONNECTED".equals(snapshot.status)){if(snapshot.storageState==null)throw new IllegalStateException("小红书登录会话缺少状态数据");if(!repository.updateAccountSession(accountId,cipher.encrypt(snapshot.storageState),"扫码会话"))throw new IllegalStateException("小红书登录会话保存失败");}return vo(snapshot);}
    private void requiredAccount(long id){if(repository.findAccount(id)==null)throw new IllegalArgumentException("小红书账号不存在");}private XhsLoginSessionVO vo(XiaohongshuWebAdapter.LoginSnapshot s){return new XhsLoginSessionVO(s.sessionId,s.status,s.image,s.message);}
}
