package com.alotra.controller.admin;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.entity.enums.EmployeeRole;
import com.alotra.entity.enums.EmployeeStatus;
import com.alotra.factory.UserFactory;
import com.alotra.service.CustomerService;
import com.alotra.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UsersAdminController {
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final UserFactory userFactory;

    public UsersAdminController(CustomerService customerService, EmployeeService employeeService, UserFactory userFactory) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.userFactory = userFactory;
    }

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "tab", required = false, defaultValue = "customers") String tab,
                        @RequestParam(value = "kwC", required = false) String kwC,
                        @RequestParam(value = "statusC", required = false) Integer statusC,
                        @RequestParam(value = "kwE", required = false) String kwE,
                        @RequestParam(value = "roleE", required = false) Integer roleE,
                        @RequestParam(value = "statusE", required = false) Integer statusE) {
        List<Customer> customers = customerService.search(kwC, CustomerStatus.fromValue(statusC));
        List<Employee> employees = employeeService.search(kwE, EmployeeRole.fromValue(roleE), EmployeeStatus.fromValue(statusE)); 
        model.addAttribute("pageTitle", "Người dùng");
        model.addAttribute("currentPage", "users");
        model.addAttribute("tab", tab);
        model.addAttribute("customers", customers);
        model.addAttribute("employees", employees);
        model.addAttribute("kwC", kwC);
        model.addAttribute("statusC", statusC);
        model.addAttribute("kwE", kwE);
        model.addAttribute("roleE", roleE);
        model.addAttribute("statusE", statusE);
        return "admin/users/index";
    }

    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable Integer id, Model model){
        Customer customer = customerService.findById(id);
        if (customer == null) throw new IllegalArgumentException("Không tìm thấy khách hàng");
        model.addAttribute("pageTitle", "Chi tiết khách hàng");
        model.addAttribute("currentPage", "users");
        model.addAttribute("kh", customer);
        return "admin/users/customer-detail";
    }

    @PostMapping("/customers/{id}/lock")
    public String lockCustomer(@PathVariable Integer id, RedirectAttributes ra) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        } else if ("boss".equalsIgnoreCase(String.valueOf(customer.getUsername())) ||
                   "boss@alotra.com".equalsIgnoreCase(String.valueOf(customer.getEmail()))) {
            ra.addFlashAttribute("error", "Không thể khóa tài khoản quản trị hệ thống.");
        } else {
            customer.setStatus(CustomerStatus.INACTIVE);
            customerService.save(customer);
            ra.addFlashAttribute("msg", "Đã khóa tài khoản khách hàng.");
        }
        return "redirect:/admin/users?tab=customers";
    }

    @PostMapping("/customers/{id}/unlock")
    public String unlockCustomer(@PathVariable Integer id, RedirectAttributes ra) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        } else {
            customer.setStatus(CustomerStatus.ACTIVE);
            customerService.save(customer);
            ra.addFlashAttribute("msg", "Đã mở khóa tài khoản khách hàng.");
        }
        return "redirect:/admin/users?tab=customers";
    }

    @PostMapping("/customers/{id}/delete")
    public String deleteCustomerPost(@PathVariable Integer id, RedirectAttributes ra) {
        return deleteCustomerInternal(id, ra);
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomerGet(@PathVariable Integer id, RedirectAttributes ra) {
        return deleteCustomerInternal(id, ra);
    }

    private String deleteCustomerInternal(Integer id, RedirectAttributes ra) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
            return "redirect:/admin/users?tab=customers";
        }
        if ("boss".equalsIgnoreCase(String.valueOf(customer.getUsername())) ||
            "boss@alotra.com".equalsIgnoreCase(String.valueOf(customer.getEmail()))) {
            ra.addFlashAttribute("error", "Không thể xóa tài khoản quản trị hệ thống.");
            return "redirect:/admin/users?tab=customers";
        }
        try {
            customerService.deleteById(id);
            ra.addFlashAttribute("msg", "Đã xóa tài khoản khách hàng.");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Không thể xóa vì tài khoản đã phát sinh dữ liệu. Vui lòng khóa thay vì xóa.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể xóa: " + ex.getMessage());
        }
        return "redirect:/admin/users?tab=customers";
    }

    @GetMapping("/employees/new")
    public String newEmployee(Model model){
        Employee employee = new Employee();
        employee.setRole(EmployeeRole.STAFF);
        employee.setStatus(EmployeeStatus.ACTIVE);
        model.addAttribute("pageTitle", "Thêm nhân viên");
        model.addAttribute("currentPage", "users");
        model.addAttribute("nv", employee);
        return "admin/users/employee-form";
    }

    @GetMapping("/employees/edit/{id}")
    public String editEmployee(@PathVariable Integer id, Model model){
        Employee employee = employeeService.findById(id).orElseThrow();
        model.addAttribute("pageTitle", "Sửa nhân viên");
        model.addAttribute("currentPage", "users");
        model.addAttribute("nv", employee);
        return "admin/users/employee-form";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("nv") @Valid Employee employee, BindingResult result, Model model, RedirectAttributes ra){
        Employee byU = employee.getUsername()!=null ? employeeService.findByUsername(employee.getUsername()) : null;
        if (byU != null && (employee.getId()==null || !byU.getId().equals(employee.getId()))) {
            result.rejectValue("username","dup","Tên đăng nhập đã tồn tại");
        }
        Employee byE = employee.getEmail()!=null ? employeeService.findByEmail(employee.getEmail()) : null;
        if (byE != null && (employee.getId()==null || !byE.getId().equals(employee.getId()))) {
            result.rejectValue("email","dup","Email đã tồn tại");
        }
        if (employee.getPhone()!=null && !employee.getPhone().isBlank()){
            Employee byP = employeeService.findByPhone(employee.getPhone());
            if (byP != null && (employee.getId()==null || !byP.getId().equals(employee.getId()))) {
                result.rejectValue("phone","dup","Số điện thoại đã tồn tại");
            }
        }
        boolean isNew = employee.getId()==null;
        String pw = employee.getPlainPassword();
        String cpw = employee.getConfirmPassword();
        if (isNew && (pw==null || pw.isBlank())) {
            result.rejectValue("plainPassword","empty","Vui lòng nhập mật khẩu");
        }
        if (pw!=null && !pw.isBlank() && !pw.equals(cpw)){
            result.rejectValue("confirmPassword","mismatch","Mật khẩu xác nhận không khớp");
        }
        if (result.hasErrors()){
            model.addAttribute("pageTitle", isNew ? "Thêm nhân viên" : "Sửa nhân viên");
            model.addAttribute("currentPage", "users");
            return "admin/users/employee-form";
        }
        if (isNew) {
            Employee created = userFactory.createEmployee(
                    employee.getUsername(),
                    employee.getEmail(),
                    employee.getFullName(),
                    employee.getPhone(),
                    employee.getRole(),
                    employee.getStatus(),
                    employee.getPlainPassword()
            );
            employeeService.save(created);
        } else {
            employeeService.saveHandlingPassword(employee);
        }
        ra.addFlashAttribute("msg", isNew ? "Đã thêm nhân viên thành công." : "Đã cập nhật nhân viên thành công.");
        return "redirect:/admin/users?tab=employees";
    }

    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Integer id, RedirectAttributes ra){
        employeeService.softDeleteToTrash(id);
        ra.addFlashAttribute("msg", "Đã chuyển nhân viên vào thùng rác.");
        return "redirect:/admin/users?tab=employees";
    }

    @PostMapping("/employees/{id}/reset-password")
    public String resetPassword(@PathVariable Integer id, RedirectAttributes ra) {
        String temp = employeeService.resetPassword(id);
        ra.addFlashAttribute("msg", "Đã đặt lại mật khẩu. Mật khẩu tạm thời: " + temp);
        return "redirect:/admin/users?tab=employees";
    }
}