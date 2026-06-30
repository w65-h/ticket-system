package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.Department;
import com.ticket.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/dept")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @GetMapping("/tree")
    public Result<List<Department>> getDeptTree() {
        return Result.ok(departmentService.getDeptTreeFromCache());
    }

    @GetMapping
    public Result<List<Department>> list() {
        return Result.ok(departmentService.list());
    }

    @PostMapping
    public Result<?> save(@RequestBody Department department) {
        departmentService.saveOrUpdate(department);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        departmentService.removeById(id);
        return Result.ok();
    }
}