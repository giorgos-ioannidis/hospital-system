package gr.ihu.hospital.web.controller;

import java.security.Principal;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.service.AdminService;
import gr.ihu.hospital.service.ScheduleService;

/** UC-23 Έγκριση Υπερωριών (UI-21). */
@Controller
public class AdminController {

    private final AdminService admin;
    private final ScheduleService schedules;

    public AdminController(AdminService admin, ScheduleService schedules) {
        this.admin = admin;
        this.schedules = schedules;
    }

    @GetMapping("/overtime")
    public String overtime(@RequestParam(defaultValue = "1") Long departmentId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                           Model model) {
        YearMonth shown = month == null ? YearMonth.now() : month;
        model.addAttribute("departments", schedules.departmentList());
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("month", shown);
        model.addAttribute("rows", admin.overtime(departmentId, shown));
        return "admin/ui-21-overtime";
    }

    @PostMapping("/overtime/decide")
    public String decide(@RequestParam Long staffId, @RequestParam Long departmentId,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month, @RequestParam boolean approve,
                         Principal principal, RedirectAttributes flash) {
        admin.decide(staffId, month, approve, principal.getName());
        flash.addFlashAttribute("success", approve ? "Οι υπερωρίες εγκρίθηκαν." : "Οι υπερωρίες απορρίφθηκαν.");
        return "redirect:/overtime?departmentId=" + departmentId + "&month=" + month;
    }
}
