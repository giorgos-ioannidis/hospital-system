package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.domain.leave.LeaveType;
import gr.ihu.hospital.service.LeaveService;
import gr.ihu.hospital.web.dto.LeaveForm;
import jakarta.validation.Valid;

/** UC-06 Υποβολή Αίτησης Άδειας (UI-06), UC-07 Έγκριση Αίτησης Άδειας με προειδοποίηση στελέχωσης (UI-07). */
@Controller
public class LeaveController {

    private final LeaveService leaves;

    public LeaveController(LeaveService leaves) {
        this.leaves = leaves;
    }

    @GetMapping("/leave")
    public String requestForm(Principal principal, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new LeaveForm());
        }
        model.addAttribute("types", LeaveType.values());
        model.addAttribute("mine", leaves.mine(principal.getName()));
        return "leave/ui-06-request";
    }

    @PostMapping("/leave")
    public String request(@Valid @ModelAttribute("form") LeaveForm form, BindingResult result, Principal principal,
                          Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                leaves.request(principal.getName(), form);
                flash.addFlashAttribute("success", "Η αίτηση στάλθηκε στον Προϊστάμενο.");
                return "redirect:/leave";
            } catch (IllegalArgumentException e) {
                result.reject("leave", e.getMessage());
            }
        }
        return requestForm(principal, model);
    }

    @GetMapping("/leave/pending")
    public String pending(Model model) {
        model.addAttribute("pending", leaves.pending());
        return "leave/ui-07-pending";
    }

    @PostMapping("/leave/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        int released = leaves.approve(id, principal.getName());
        String note = released > 0 ? " " + released + " βάρδιες έγιναν ακάλυπτες (δείτε τις Αναπληρώσεις)." : "";
        flash.addFlashAttribute("success", "Η άδεια εγκρίθηκε." + note);
        return "redirect:/leave/pending";
    }

    @PostMapping("/leave/{id}/reject")
    public String reject(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        leaves.reject(id, principal.getName());
        flash.addFlashAttribute("success", "Η άδεια απορρίφθηκε.");
        return "redirect:/leave/pending";
    }
}
