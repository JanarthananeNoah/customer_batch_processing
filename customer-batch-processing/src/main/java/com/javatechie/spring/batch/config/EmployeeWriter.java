package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.Employee;
import com.javatechie.spring.batch.repository.EmployeeRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeWriter implements ItemWriter<Employee> {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void write(List<? extends Employee> list) throws Exception {
        System.out.println("Thread Name Employee : " + Thread.currentThread().getName());
        employeeRepository.saveAll(list);
    }
}
