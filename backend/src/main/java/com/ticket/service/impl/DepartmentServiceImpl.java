package com.ticket.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticket.entity.Department;
import com.ticket.mapper.DepartmentMapper;
import com.ticket.service.DepartmentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private static final String DEPT_TREE_KEY = "dept:tree";
    private static final String DEPT_CHILDREN_KEY = "dept:children:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Department> getDeptTree() {
        List<Department> allDepts = list(new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSortOrder));

        Map<Long, List<Department>> parentMap = allDepts.stream()
                .filter(d -> d.getParentId() != null && d.getParentId() != 0)
                .collect(Collectors.groupingBy(Department::getParentId));

        List<Department> roots = allDepts.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());

        for (Department root : roots) {
            buildChildren(root, parentMap);
        }

        return roots;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Department> getDeptTreeFromCache() {
        Object cached = redisTemplate.opsForValue().get(DEPT_TREE_KEY);
        if (cached != null) {
            return (List<Department>) cached;
        }

        List<Department> tree = getDeptTree();
        redisTemplate.opsForValue().set(DEPT_TREE_KEY, tree, 30, TimeUnit.MINUTES);
        return tree;
    }

    @Override
    public List<Long> getSubDeptIds(Long deptId) {
        String cacheKey = DEPT_CHILDREN_KEY + deptId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (List<Long>) cached;
        }

        List<Long> result = new ArrayList<>();
        result.add(deptId);
        collectSubDeptIds(deptId, result);

        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        return result;
    }

    private void buildChildren(Department parent, Map<Long, List<Department>> parentMap) {
        List<Department> children = parentMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (Department child : children) {
            buildChildren(child, parentMap);
        }
    }

    private void collectSubDeptIds(Long deptId, List<Long> result) {
        List<Department> allDepts = list(new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .select(Department::getId, Department::getParentId));
        collectSubDeptIdsFromList(deptId, allDepts, result);
    }

    private void collectSubDeptIdsFromList(Long deptId, List<Department> allDepts, List<Long> result) {
        for (Department child : allDepts) {
            if (child.getParentId() != null && child.getParentId().equals(deptId)) {
                result.add(child.getId());
                collectSubDeptIdsFromList(child.getId(), allDepts, result);
            }
        }
    }
}