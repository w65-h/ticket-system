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

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private TicketMapper ticketMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        vo.setTotalTickets(ticketMapper.selectTotalCount());
        vo.setPendingTickets(ticketMapper.selectCountByStatus(1));
        vo.setProcessingTickets(ticketMapper.selectCountByStatus(2));
        vo.setCompletedTickets(ticketMapper.selectCountByStatus(4));
        vo.setRejectedTickets(ticketMapper.selectCountByStatus(5));

        Double avg = ticketMapper.selectAvgProcessHours();
        vo.setAvgProcessHours(avg != null ? Math.round(avg * 100.0) / 100.0 : 0);

        vo.setStatusStats(buildStatusStats(vo));
        vo.setDeptStats(ticketMapper.selectDeptStats());
        vo.setDayTrend(ticketMapper.selectDayTrend(30));

        return vo;
    }

    private List<StatusStatVO> buildStatusStats(DashboardVO vo) {
        long total = vo.getTotalTickets();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("待处理", vo.getPendingTickets());
        map.put("处理中", vo.getProcessingTickets());
        map.put("待验收", ticketMapper.selectCountByStatus(3));
        map.put("已完成", vo.getCompletedTickets());
        map.put("已驳回", vo.getRejectedTickets());

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