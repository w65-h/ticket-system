package com.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticket.dto.TicketQueryDTO;
import com.ticket.entity.Ticket;
import com.ticket.vo.DayTrendVO;
import com.ticket.vo.DeptStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    Page<Ticket> selectTicketPage(Page<Ticket> page, @Param("query") TicketQueryDTO query,
                                   @Param("deptIds") List<Long> deptIds);

    @Select("SELECT COUNT(*) FROM ticket WHERE deleted = 0")
    long selectTotalCount();

    @Select("SELECT COUNT(*) FROM ticket WHERE status = #{status} AND deleted = 0")
    long selectCountByStatus(@Param("status") Integer status);

    @Select("SELECT AVG(TIMESTAMPDIFF(HOUR, create_time, finish_time)) " +
            "FROM ticket WHERE status = 4 AND deleted = 0")
    Double selectAvgProcessHours();

    List<DeptStatVO> selectDeptStats();

    List<DayTrendVO> selectDayTrend(@Param("days") int days);
}