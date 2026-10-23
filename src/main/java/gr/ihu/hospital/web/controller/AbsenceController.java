package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.service.AbsenceService;
import gr.ihu.hospital.web.dto.AbsenceForm;
import jakarta.validation.Valid;

/** UC-09 Δήλωση Απουσίας (UI-08), UC-10 Εύρεση Αντικαταστάτη (UI-10). */
@Controller
public class AbsenceController {

    private final AbsenceService absences;

    public AbsenceController(AbsenceService absences) {
        this.absences = absences;
    }

    @GetMapping("/absences")
    public String declareForm(Principal principal, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AbsenceForm());
        }
        model.addAttribute("mine", absences.mine(principal.getName()));
        return "absence/ui-08-declare";
    }

    @PostMapping("/absences")
    public String declare(@Valid @ModelAttribute("form") AbsenceForm form, BindingResult result, Principal principal,
                          Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                int released = absences.declare(principal.getName(), form);
                flash.addFlashAttribute("success", released == 0
                    ? "Η απουσία καταχωρήθηκε. Δεν είχατε βάρδιες στο διάστημα, άρα δεν χρειάζεται αναπλήρωση."
                    : "Η απουσία καταχωρήθηκε. " + released + " βάρδιες έμειναν ακάλυπτες και ο Προϊστάμενος ενημερώθηκε για αναπλήρωση.");
                return "redirect:/absences";
            } catch (IllegalArgumentException e) {
                result.reject("absence", e.getMessage());
            }
        }
        return declareForm(principal, model);
    }

    @GetMapping("/absences/replacements")
    public String replacements(Model model) {
        model.addAttribute("uncovered", absences.uncovered());
        return "absence/ui-10-replacements";
    }

    @PostMapping("/absences/replacements")
    public String assign(@RequestParam Long shiftId, @RequestParam Long staffId, RedirectAttributes flash) {
        HealthStaff replacement = absences.assignReplacement(shiftId, staffId);
        flash.addFlashAttribute("success", replacement.getFullName() + " ορίστηκε αναπληρωτής.");
        return "redirect:/absences/replacements";
    }
}
