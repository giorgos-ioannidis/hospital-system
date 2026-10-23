package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.service.OfficeHoursResult;
import gr.ihu.hospital.service.OfficeHoursService;
import gr.ihu.hospital.web.dto.OfficeHoursForm;
import jakarta.validation.Valid;

/** UC-11 Ορισμός Ωρών Υποδοχής (UI-13). */
@Controller
public class OfficeHoursController {

    private final OfficeHoursService officeHours;

    public OfficeHoursController(OfficeHoursService officeHours) {
        this.officeHours = officeHours;
    }

    @GetMapping("/office-hours")
    public String list(Principal principal, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new OfficeHoursForm());
        }
        model.addAttribute("rooms", officeHours.rooms(principal.getName()));
        model.addAttribute("hours", officeHours.mine(principal.getName()));
        return "officehours/ui-13-office-hours";
    }

    @PostMapping("/office-hours")
    public String define(@Valid @ModelAttribute("form") OfficeHoursForm form, BindingResult result, Principal principal,
                         Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                OfficeHoursResult outcome = officeHours.define(principal.getName(), form);
                flash.addFlashAttribute("success", outcome.getCreated().size() == 1 ? "Οι ώρες υποδοχής καταχωρήθηκαν."
                    : "Καταχωρήθηκαν ώρες υποδοχής για " + outcome.getCreated().size() + " ημέρες.");
                if (!outcome.getRejected().isEmpty()) {
                    flash.addFlashAttribute("rejected", outcome.getRejected());
                }
                return "redirect:/office-hours";
            } catch (IllegalArgumentException e) {
                result.reject("hours", e.getMessage());
            }
        }
        return list(principal, model);
    }

    /** Με κλεισμένα ραντεβού ζητείται πρώτα επιβεβαίωση (§7.5). */
    @PostMapping("/office-hours/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm,
                         Principal principal, RedirectAttributes flash) {
        int booked = officeHours.bookedCount(principal.getName(), id);
        if (booked > 0 && !confirm) {
            flash.addFlashAttribute("confirmDelete", id);
            flash.addFlashAttribute("confirmCount", booked);
            return "redirect:/office-hours";
        }
        int cancelled = officeHours.remove(principal.getName(), id);
        flash.addFlashAttribute("success", cancelled == 0 ? "Οι ώρες διαγράφηκαν."
            : "Οι ώρες διαγράφηκαν. Ακυρώθηκαν " + cancelled + " ραντεβού και οι ασθενείς ειδοποιήθηκαν.");
        return "redirect:/office-hours";
    }
}
