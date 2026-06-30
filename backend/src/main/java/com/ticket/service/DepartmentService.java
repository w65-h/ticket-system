package com.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticket.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    List<Department> getDeptTree();

    List<Department> getDeptTreeFromCache();

    List<Long> getSubDeptIds(Long deptId);
}