package com.example.EmployeeManager.controller;

import com.example.EmployeeManager.model.Employee;
import com.example.EmployeeManager.service.EmployeeService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Display employee form and employee list
    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute(
                "employees",
                employeeService.getAllEmployees()
        );
        return "employees";
    }

    // Save a new employee
    @PostMapping("/employees")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute(
                    "employees",
                    employeeService.getAllEmployees()
            );
            return "employees";
        }

        if (employeeService.emailExists(employee.getEmail())) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered"
            );

            model.addAttribute(
                    "employees",
                    employeeService.getAllEmployees()
            );
            return "employees";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "success",
                "Employee saved successfully!"
        );

        return "redirect:/employees";
    }

    // Show edit form
    @GetMapping("/employees/{id}/edit")
    public String editEmployee(
            @PathVariable Long id,
            Model model) {

        Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid employee ID: " + id));

        model.addAttribute("employee", employee);

        return "edit-employee";
    }

    // Update employee
    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        employeeService.getEmployeeById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid employee ID: " + id));

        employee.setId(id);

        if (result.hasErrors()) {
            return "edit-employee";
        }

        if (employeeService.emailExistsForOtherEmployee(
                employee.getEmail(), id)) {

            result.rejectValue(
                    "email",
                    "duplicate",
                    "This email is already registered"
            );

            return "edit-employee";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "success",
                "Employee updated successfully!"
        );

        return "redirect:/employees";
    }

    // Delete employee
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        employeeService.deleteEmployee(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Employee deleted successfully!"
        );

        return "redirect:/employees";
    }
}
