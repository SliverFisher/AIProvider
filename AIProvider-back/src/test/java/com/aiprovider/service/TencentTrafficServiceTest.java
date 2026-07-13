package com.aiprovider.service;

import com.tencentcloudapi.lighthouse.v20200324.models.TrafficPackage;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class TencentTrafficServiceTest {
    @Test void selectsActiveNormalPackageInsteadOfFirstItem() {
        Instant now=Instant.parse("2026-07-13T06:00:00Z");
        TrafficPackage expired=item("2026-05-01T00:00:00Z","2026-06-01T00:00:00Z","NETWORK_NORMAL",1L);
        TrafficPackage disabled=item("2026-07-01T00:00:00Z","2026-08-01T00:00:00Z","OVERDUE_NETWORK_DISABLED",2L);
        TrafficPackage active=item("2026-07-01T00:00:00Z","2026-08-01T00:00:00Z","NETWORK_NORMAL",3L);
        assertSame(active,TencentTrafficService.selectCurrent(Arrays.asList(expired,disabled,active),now));
    }
    @Test void returnsNullWhenNoPackageContainsCurrentTime() {
        TrafficPackage expired=item("2026-05-01T00:00:00Z","2026-06-01T00:00:00Z","NETWORK_NORMAL",1L);
        assertNull(TencentTrafficService.selectCurrent(java.util.Collections.singletonList(expired),Instant.parse("2026-07-13T06:00:00Z")));
    }
    private static TrafficPackage item(String start,String end,String status,Long used){TrafficPackage item=new TrafficPackage();item.setStartTime(start);item.setEndTime(end);item.setStatus(status);item.setTrafficUsed(used);return item;}
}
