package com.alotra.util;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    public static Customer getCustomer(HttpSession session) {
        return (Customer) session.getAttribute("customer");
    }

    public static Employee getEmployee(HttpSession session) {
        return (Employee) session.getAttribute("employee");
    }
}
