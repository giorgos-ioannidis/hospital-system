package gr.ihu.hospital.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gr.ihu.hospital.domain.record.EntryType;
import gr.ihu.hospital.service.MedicalRecordService;

/** UC-19 Ιατρικό Ιστορικό: αναζήτηση / οι ασθενείς μου (UI-18a), φάκελος (UI-18b). */
@Controller
public class RecordController {

    private final MedicalRecordService records;

    public RecordController(MedicalRecordService records) {
        this.records = records;
    }

    @GetMapping("/records")
    public String search(@RequestParam(required = false) String q, Principal principal, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("results", records.search(q));
        model.addAttribute("mine", records.myPatients(principal.getName()));
        return "record/ui-18a-search";
    }

    @GetMapping("/records/mine")
    public String mine(Principal principal) {
        return "redirect:/records/" + records.ownPatientId(principal.getName());
    }

    @GetMapping("/records/{patientId}")
    public String record(@PathVariable Long patientId, Principal principal, Model model) {
        model.addAttribute("view", records.view(principal.getName(), patientId));
        model.addAttribute("ownRecord", false);
        return "record/ui-18b-record";
    }

    @PostMapping("/records/{patientId}/entries")
    public String addEntry(@PathVariable Long patientId, @RequestParam EntryType type, @RequestParam String text,
                           Principal principal, RedirectAttributes flash) {
        records.addEntry(principal.getName(), patientId, type, text);
        flash.addFlashAttribute("success", "Η εγγραφή προστέθηκε στο ιστορικό.");
        return "redirect:/records/" + patientId;
    }

    /** Για τον ασθενή: ποιος άνοιξε τον φάκελό του. */
    @GetMapping("/records/mine/access-log")
    public String accessLog(Principal principal, Model model) {
        model.addAttribute("logs", records.accessLogOfMine(principal.getName()));
        return "record/ui-18c-access-log";
    }
}
