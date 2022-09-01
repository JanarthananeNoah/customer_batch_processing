package com.javatechie.spring.batch.repository;

import com.javatechie.spring.batch.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
}
