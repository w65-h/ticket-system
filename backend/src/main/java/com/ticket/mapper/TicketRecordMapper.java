package com.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticket.entity.TicketRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketRecordMapper extends BaseMapper<TicketRecord> {
}