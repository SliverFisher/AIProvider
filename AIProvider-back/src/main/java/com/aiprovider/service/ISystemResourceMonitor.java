package com.aiprovider.service;

import com.aiprovider.model.vo.MonitorSummaryVO;

public interface ISystemResourceMonitor {
    MonitorSummaryVO.Resource memory();
    MonitorSummaryVO.Resource disk();
}
