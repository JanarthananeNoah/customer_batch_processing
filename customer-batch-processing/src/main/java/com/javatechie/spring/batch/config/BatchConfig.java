package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.Customer;
import com.javatechie.spring.batch.entity.Employee;
import com.javatechie.spring.batch.partition.CustomerPartitioner;

import com.javatechie.spring.batch.partition.EmployeePartitioner;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private CustomerWriter customerWriter;

    private EmployeeWriter employeeWriter;


    @Bean
    public FlatFileItemReader<Customer> customerReader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csv-Reader-Customer");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    //Employee - reader
    @Bean
    public FlatFileItemReader<Employee> employeeReader() {
        FlatFileItemReader<Employee> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/employee.csv"));
        itemReader.setName("csv-Reader-Employee");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapperEmployee());
        return itemReader;
    }

    private LineMapper<Employee> lineMapperEmployee() {
        DefaultLineMapper<Employee> lineMapperEmployee = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "jobTitle", "skill");

        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);

        lineMapperEmployee.setLineTokenizer(lineTokenizer);
        lineMapperEmployee.setFieldSetMapper(fieldSetMapper);
        return lineMapperEmployee;
    }

    @Bean
    public CustomerProcessor customerProcessor() {
        return new CustomerProcessor();
    }

    // Employee Processor
    @Bean
    public EmployeeProcessor employeeProcessor() {
        return new EmployeeProcessor();
    }

    @Bean
    public CustomerPartitioner customerPartitioner() {
        return new CustomerPartitioner();
    }

    /*             Employee Partitioner         */
    @Bean
    public EmployeePartitioner employeePartitioner(){
        return new EmployeePartitioner();
    }

    public PartitionHandler partitionHandlerCustomer() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(2);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStepCustomer());
        return taskExecutorPartitionHandler;
    }

    public PartitionHandler partitionHandleEmployee() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(2);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(SlaveStepEmployee());
        return taskExecutorPartitionHandler;
    }



    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(4);
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setQueueCapacity(4);
        return threadPoolTaskExecutor;
    }

    @Bean
    public Step slaveStepCustomer() {
        return stepBuilderFactory.get("csv-step-Customer").<Customer, Customer>chunk(500)
                .reader(customerReader())
                .processor(customerProcessor())
                .writer(customerWriter)
                .build();

    }
    @Bean
    public Step SlaveStepEmployee(){
        return stepBuilderFactory.get("csv-step-Employee").<Employee, Employee>chunk(500)
                .reader(employeeReader())
                .processor(employeeProcessor())
                .writer(employeeWriter)
                .build();
    }

    @Bean
    public Step partitionCustomerStep() {
        return stepBuilderFactory.get("partition-Customer-Step").
                partitioner(slaveStepCustomer().getName(), customerPartitioner())
                .partitionHandler(partitionHandlerCustomer())
                .build();
    }

    @Bean
    public Step partitionEmployeeStep(){
        return stepBuilderFactory.get("partition-Employee-Step").
                partitioner(SlaveStepEmployee().getName(), employeePartitioner())
                .partitionHandler(partitionHandleEmployee())
                .build();
    }

    @Bean
    public Flow flowCustomer(){
        return new FlowBuilder<SimpleFlow>("customer")
                .start(partitionCustomerStep())
                .build();
    }

    @Bean
    public Flow flowEmployee(){
        return new FlowBuilder<SimpleFlow>("employee")
                .start(partitionEmployeeStep())
                .build();
    }

    @Bean
    public Flow splitFlow(){
        return new FlowBuilder<SimpleFlow>("Split-Flow")
                .split(taskExecutor())
                .add(flowCustomer(),flowEmployee())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers")
                .start(splitFlow())
                .build()
                .build();


    }


}
