package com.example.restapi.controller;
import com.example.restapi.persistance.Department;
import com.example.restapi.service.DepartmentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    DepartmentService service;

    public DepartmentController(DepartmentService service) {

        this.service = service;
    }

    @GetMapping(value ="/departments")
    public List<Department> getAll(){

        return this.service.getAll();
    }


    @GetMapping(value ="/departments/{id}")
    public Department getById(@PathVariable Long id){

        return  this.service.getById(id);
    }

    @PostMapping(value = "/departments")
    public Department add(@RequestBody Department department){
        return this.service.add(department);
    }

    @PutMapping(value = "/departments/{id}", consumes = "application/json")
    public Department update( @PathVariable Long id,@RequestBody Department department){

        return this.service.update(id,department);
    }
    @DeleteMapping(value = "/departments/{id}")
    public void delete( @PathVariable long id){
        this.service.delete(id);
    }


}

