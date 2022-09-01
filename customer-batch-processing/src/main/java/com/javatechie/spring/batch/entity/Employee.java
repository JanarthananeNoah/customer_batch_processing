package com.javatechie.spring.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "EMPLOYEE_INFO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    private int id;

    private String firstName;

    private String lastName;

    private String email;

    private String gender;

    private String jobTitle;

    private String skill;


}
