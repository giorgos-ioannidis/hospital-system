package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.service.ExamService;
import gr.ihu.hospital.service.MedicalRecordService;

/** UC-20 Παραγγελία Εξέτασης και καταχώριση αποτελέσματος (UI-19), UC-21 Αποτελέσματα του ασθενούς (UI-20). */
@Controller
public class ExamController {

    private final ExamService exams;
    private final MedicalRecordService records;

    public ExamController(ExamService exams, MedicalRecordService records) {
        this.exams = exams;
        this.records = records;
    }

    @GetMapping("/exams")
    public String doctorExams(Principal principal, Model model) {
        model.addAttribute("patients", records.myPatients(principal.getName()));
        model.addAttribute("catalog", exams.catalog());
        model.addAttribute("orders", exams.ordered(principal.getName()));
        return "exam/ui-19-doctor-exams";
    }

    @PostMapping("/exams")
    public String order(@RequestParam Long patientId, @RequestParam Long serviceId,
                        @RequestParam(required = false) String notes, Principal principal, RedirectAttributes flash) {
        try {
            exams.order(principal.getName(), patientId, serviceId, notes);
            flash.addFlashAttribute("success", "Η εξέταση παραγγέλθηκε και ο ασθενής ειδοποιήθηκε.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/exams";
    }

    @PostMapping("/exams/{id}/result")
    public String result(@PathVariable Long id, @RequestParam String text, Principal principal, RedirectAttributes flash) {
        exams.recordResult(principal.getName(), id, text);
        flash.addFlashAttribute("success", "Το αποτέλεσμα καταχωρήθηκε και ο ασθενής ειδοποιήθηκε.");
        return "redirect:/exams";
    }

    @PostMapping("/exams/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        exams.cancel(principal.getName(), id);
        flash.addFlashAttribute("success", "Η εξέταση ακυρώθηκε.");
        return "redirect:/exams";
    }

    @GetMapping("/exams/mine")
    public String mine(Principal principal, Model model) {
        model.addAttribute("orders", exams.mine(principal.getName()));
        return "exam/ui-20-results";
    }
}
