package com.ticket.util;

import com.ticket.service.DepartmentService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("perm")
public class PermissionUtil {

    @Resource
    private DepartmentService departmentService;

    public boolean hasDeptPermission(Long targetDeptId, Long currentUserDeptId) {
        if (currentUserDeptId == null) {
            return false;
        }
        List<Long> subDeptIds = departmentService.getSubDeptIds(currentUserDeptId);
        return subDeptIds.contains(targetDeptId);
    }
}