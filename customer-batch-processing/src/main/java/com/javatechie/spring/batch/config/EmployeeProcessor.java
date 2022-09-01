package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.Employee;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee,Employee> {
    @Override
    public Employee process(Employee employee) throws Exception {
        return employee ;
    }
}
