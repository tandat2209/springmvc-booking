package com.mgmtp.controller;

import com.mgmtp.model.Employee;
import com.mgmtp.model.Request;
import com.mgmtp.repository.VacationTypeRepository;
import com.mgmtp.service.EmployeeService;
import com.mgmtp.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/my-request")
public class MyRequestController {
    private final static Logger LOGGER = LoggerFactory.getLogger(MyRequestController.class);
    private static final int MAX_PAGES_IN_PAGINATION = 9;

    private final RequestService requestService;

    private final EmployeeService employeeService;

    private final VacationTypeRepository vacationTypeRepository;

    @Autowired
    public MyRequestController(RequestService requestService, EmployeeService employeeService, VacationTypeRepository vacationTypeRepository) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.vacationTypeRepository = vacationTypeRepository;
    }


    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public String history(@RequestParam(value = "page", defaultValue = "1", required = false) Integer pageNumber,
                          @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit,
                          Model model){
        if (pageNumber < 1) return "redirect:/my-request/history";

        Page<Request> requests = requestService.findALl(pageNumber, limit);
        model.addAttribute("requests", requests);
        setPaginationInfo(model, requests);

        return "myrequest/history";
    }

    @RequestMapping(value = "/booking", method = RequestMethod.GET)
    public String booking(Model model){
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        Employee currentEmployee = employeeService.findByEmail(email);
        model.addAttribute("approvers", currentEmployee.getApprovers());
        model.addAttribute("request", new Request());
        model.addAttribute("vacationTypes", vacationTypeRepository.findAll());
        return "myrequest/booking";
    }

    @RequestMapping(value = "/booking", method = RequestMethod.POST)
    public String processBookingForm(Request request){

        requestService.save(request);
        return "redirect:/my-request/booking";
    }

    private void setPaginationInfo(Model model, Page page){
        int currentIndex = page.getNumber() + 1;
        int beginIndex = 1, endIndex = page.getTotalPages();
        if(page.getTotalPages() > MAX_PAGES_IN_PAGINATION){
            beginIndex = Math.max(1, currentIndex - MAX_PAGES_IN_PAGINATION/2);
            endIndex = Math.min(beginIndex + MAX_PAGES_IN_PAGINATION - 1, page.getTotalPages());
            if(endIndex - beginIndex + 1 < MAX_PAGES_IN_PAGINATION){
                beginIndex -= MAX_PAGES_IN_PAGINATION - (endIndex-beginIndex + 1);
            }
        }

        model.addAttribute("beginIndex", beginIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("currentIndex", currentIndex);
    }
}
