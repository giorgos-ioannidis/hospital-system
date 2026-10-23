package gr.ihu.hospital.web.controller;

import java.security.Principal;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.service.ChangeResult;
import gr.ihu.hospital.service.ScheduleChangeService;
import gr.ihu.hospital.service.ScheduleService;
import gr.ihu.hospital.web.dto.ManualChangeForm;
import gr.ihu.hospital.web.dto.ScheduleCalendarView;
import gr.ihu.hospital.web.dto.ScheduleParamsForm;
import jakarta.validation.Valid;

/** UC-05 Παραγωγή Προγράμματος: παράμετροι (UI-05a), ημερολόγιο (UI-05b). UC-08 Προβολή Προγράμματος (UI-09). */
@Controller
public class ScheduleController {

    private final ScheduleService schedules;
    private final ScheduleChangeService changes;

    public ScheduleController(ScheduleService schedules, ScheduleChangeService changes) {
        this.schedules = schedules;
        this.changes = changes;
    }

    @GetMapping("/schedules")
    public String params(Model model) {
        if (!model.containsAttribute("form")) {
            ScheduleParamsForm form = new ScheduleParamsForm();
            form.setMonth(YearMonth.now().plusMonths(1));
            model.addAttribute("form", form);
        }
        model.addAttribute("departments", schedules.departmentList());
        model.addAttribute("schedules", schedules.list());
        return "schedule/ui-05a-params";
    }

    @PostMapping("/schedules")
    public String generate(@Valid @ModelAttribute("form") ScheduleParamsForm form, BindingResult result, Model model,
                           RedirectAttributes flash) {
        if (result.hasErrors()) {
            return params(model);
        }
        Schedule draft = schedules.generate(form);
        flash.addFlashAttribute("success", "Δημιουργήθηκε πρόχειρο πρόγραμμα. Ελέγξτε το και εγκρίνετέ το.");
        return "redirect:/schedules/" + draft.getId();
    }

    @GetMapping("/schedules/{id}")
    public String calendar(@PathVariable Long id, Model model) {
        Schedule schedule = schedules.get(id);
        model.addAttribute("schedule", schedule);
        model.addAttribute("calendar", new ScheduleCalendarView(schedule));
        model.addAttribute("violations", schedules.review(schedule));
        model.addAttribute("staff", schedules.departmentStaff(schedule));
        if (!model.containsAttribute("change")) {
            model.addAttribute("change", new ManualChangeForm());
        }
        return "schedule/ui-05b-calendar";
    }

    @PostMapping("/schedules/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        schedules.approve(id, principal.getName());
        flash.addFlashAttribute("success", "Το πρόγραμμα εγκρίθηκε και είναι πλέον ενεργό.");
        return "redirect:/schedules/" + id;
    }

    @PostMapping("/schedules/{id}/changes")
    public String change(@PathVariable Long id, @Valid @ModelAttribute("change") ManualChangeForm form,
                         BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Συμπληρώστε ημερομηνία, βάρδια και άτομο.");
            return "redirect:/schedules/" + id;
        }
        ChangeResult outcome = changes.change(id, form);
        flash.addFlashAttribute("warnings", outcome.getWarnings());
        if (outcome.isApplied()) {
            flash.addFlashAttribute("success", "Η αλλαγή εφαρμόστηκε.");
        } else {
            form.setConfirm(true);
            flash.addFlashAttribute("pendingChange", form);
        }
        return "redirect:/schedules/" + id;
    }

    @GetMapping("/schedules/mine")
    public String mine(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                       Principal principal, Model model) {
        YearMonth shown = month == null ? YearMonth.now() : month;
        model.addAttribute("month", shown);
        model.addAttribute("assignments", schedules.mySchedule(principal.getName(), shown));
        return "schedule/ui-09-my-schedule";
    }
}
