package com.ticket.service.impl;

import com.ticket.mapper.TicketMapper;
import com.ticket.service.DashboardService;
import com.ticket.vo.DashboardVO;
import com.ticket.vo.DayTrendVO;
import com.ticket.vo.DeptStatVO;
import com.ticket.vo.StatusStatVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private TicketMapper ticketMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        Map<Integer, Long> statusCountMap = ticketMapper.selectStatusCounts().stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("status")).intValue(),
                        row -> ((Number) row.get("cnt")).longValue()));

        long pending = statusCountMap.getOrDefault(1, 0L);
        long processing = statusCountMap.getOrDefault(2, 0L);
        long verifying = statusCountMap.getOrDefault(3, 0L);
        long completed = statusCountMap.getOrDefault(4, 0L);
        long rejected = statusCountMap.getOrDefault(5, 0L);
        long total = pending + processing + verifying + completed + rejected;

        vo.setTotalTickets(total);
        vo.setPendingTickets(pending);
        vo.setProcessingTickets(processing);
        vo.setCompletedTickets(completed);
        vo.setRejectedTickets(rejected);

        Double avg = ticketMapper.selectAvgProcessHours();
        vo.setAvgProcessHours(avg != null ? Math.round(avg * 100.0) / 100.0 : 0);

        vo.setStatusStats(buildStatusStats(total, pending, processing, verifying, completed, rejected));
        vo.setDeptStats(ticketMapper.selectDeptStats());
        vo.setDayTrend(ticketMapper.selectDayTrend(30));

        return vo;
    }

    private List<StatusStatVO> buildStatusStats(long total, long pending, long processing,
                                                 long verifying, long completed, long rejected) {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("待处理", pending);
        map.put("处理中", processing);
        map.put("待验收", verifying);
        map.put("已完成", completed);
        map.put("已驳回", rejected);

        List<StatusStatVO> stats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            StatusStatVO stat = new StatusStatVO();
            stat.setStatus(entry.getKey());
            stat.setCount(entry.getValue());
            stat.setPercentage(total > 0 ? Math.round(entry.getValue() * 10000.0 / total) / 100.0 : 0);
            stats.add(stat);
        }
        return stats;
    }
}