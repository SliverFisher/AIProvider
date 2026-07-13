package com.aiprovider.service;

import com.aiprovider.model.vo.MonitorSummaryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

class TencentTrafficServiceLiveTest {
    @Test
    @EnabledIfEnvironmentVariable(named="TENCENT_CLOUD_SECRET_ID",matches=".+")
    void readsConfiguredLighthouseTrafficPackage() throws Exception {
        TencentTrafficService service=new TencentTrafficService(
            System.getenv("TENCENT_CLOUD_SECRET_ID"),System.getenv("TENCENT_CLOUD_SECRET_KEY"),
            System.getenv().getOrDefault("TENCENT_CLOUD_REGION","ap-shanghai"),
            System.getenv().getOrDefault("TENCENT_CLOUD_LIGHTHOUSE_INSTANCE_ID","lhins-20ht1gis"));
        MonitorSummaryVO.Traffic traffic=service.current();
        assertTrue(traffic.isAvailable(),"Tencent Cloud traffic package should be available, status="+traffic.getStatus());
        assertNotNull(traffic.getTotalBytes()); assertNotNull(traffic.getPeriodStart()); assertNotNull(traffic.getPeriodEnd());
    }
}
