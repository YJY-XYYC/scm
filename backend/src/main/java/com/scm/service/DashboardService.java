package com.scm.service;

import com.scm.dto.DashboardDTO;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardDTO getDashboardData();
    List<Map<String, Object>> getRecentActivities();
} 