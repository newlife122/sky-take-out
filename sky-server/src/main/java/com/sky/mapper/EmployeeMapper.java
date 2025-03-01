package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    Employee getByUsername(String username);

    @AutoFIll(OperationType.INSERT)
    void insert(Employee employee);

    Page<Employee> selectAll(EmployeePageQueryDTO employeePageQueryDTO);

    Employee getById(Long id);

    /**
     * 根据id来update
     * @param employee
     */
    @AutoFIll(OperationType.UPDATE)
    void update(Employee employee);
}
