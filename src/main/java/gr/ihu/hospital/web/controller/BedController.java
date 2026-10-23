package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.domain.organization.Admission;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.service.BedService;
import gr.ihu.hospital.service.ScheduleService;

/** UC-17 Κατανομή Κλίνης (UI-24) και UC-18 Πληρότητα Θαλάμων (UI-25). */
@Controller
public class BedController {

    private final BedService beds;
    private final ScheduleService schedules;

    public BedController(BedService beds, ScheduleService schedules) {
        this.beds = beds;
        this.schedules = schedules;
    }

    /** Με ΑΜΚΑ, τμήμα, βαρύτητα και απομόνωση εμφανίζει την καλύτερη κλίνη και 2 εναλλακτικές. */
    @GetMapping("/beds")
    public String assign(@RequestParam(required = false) String amka, @RequestParam(required = false) Long departmentId,
                         @RequestParam(defaultValue = "2") int severity, @RequestParam(defaultValue = "false") boolean isolation,
                         Model model) {
        model.addAttribute("departments", schedules.departmentList());
        model.addAttribute("amka", amka);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("severity", severity);
        model.addAttribute("isolation", isolation);
        model.addAttribute("admissions", beds.activeAdmissions());
        if (amka != null && !amka.isBlank() && departmentId != null) {
            try {
                Patient patient = beds.patientByAmka(amka);
                model.addAttribute("patient", patient);
                model.addAttribute("suggestions", beds.suggest(patient, departmentId, severity, isolation));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "beds/ui-24-assign";
    }

    @PostMapping("/beds/admit")
    public String admit(@RequestParam String amka, @RequestParam Long bedId, @RequestParam int severity,
                        @RequestParam(defaultValue = "false") boolean isolation, Principal principal,
                        RedirectAttributes flash) {
        Admission admission = beds.admit(principal.getName(), amka, bedId, severity, isolation);
        flash.addFlashAttribute("success", admission.getPatient().getFullName() + " εισήχθη στην κλίνη "
            + admission.getBed().getName() + ".");
        return "redirect:/beds";
    }

    @PostMapping("/beds/{id}/discharge")
    public String discharge(@PathVariable Long id, RedirectAttributes flash) {
        beds.discharge(id);
        flash.addFlashAttribute("success", "Δόθηκε εξιτήριο.");
        return "redirect:/beds";
    }

    @GetMapping("/beds/occupancy")
    public String occupancy(Model model) {
        model.addAttribute("occupancy", beds.occupancy());
        return "beds/ui-25-occupancy";
    }
}
