package com.nadunkawishika.helloshoesapplicationserver.api;

import com.nadunkawishika.helloshoesapplicationserver.dto.EmployeeDTO;
import com.nadunkawishika.helloshoesapplicationserver.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class Employee {
    private final Logger LOGGER = Logger.getLogger(Employee.class.getName());
    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDTO> getEmployees(@RequestParam(required = false) String pattern) {
        if (pattern != null) {
            LOGGER.info("Filter Employee Request: {}" + pattern);
            return employeeService.filterEmployees(pattern);
        } else {
            LOGGER.info("Get All Employee Request");
            return employeeService.getEmployees();
        }
    }

    @GetMapping("/{id}")
    public EmployeeDTO getEmployee(@PathVariable String id) {
        LOGGER.info("Get Employee Request: {}" + id);
        return employeeService.getEmployee(id);
    }

    // Authenticated users can access this endpoint
    @Secured("ADMIN")
    @PostMapping
    public void saveEmployee(@RequestParam("image") MultipartFile image, @RequestPart(name = "dto") String dto) throws IOException {
        LOGGER.info("Save Employee Request");
        employeeService.saveEmployee(dto, image);
    }

    @Secured("ADMIN")
    @PutMapping("/{id}")
    public void updateEmployee(@PathVariable String id, @RequestPart(name = "dto") String dto, @RequestParam("image") MultipartFile image) throws IOException {
        LOGGER.info("Get Employee Request: {}" + id);
        employeeService.getEmployee(id);
    }

    @Secured("ADMIN")
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable String id) {
        LOGGER.info("Delete Employee Request: {}" + id);
        employeeService.deleteEmployee(id);
    }
}
