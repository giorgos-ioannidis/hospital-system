package gr.ihu.hospital.web.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

import gr.ihu.hospital.domain.appointment.Appointment;
import gr.ihu.hospital.domain.appointment.AppointmentStatus;
import gr.ihu.hospital.domain.appointment.PaymentMethod;
import gr.ihu.hospital.service.AppointmentManagementService;
import gr.ihu.hospital.service.AppointmentService;
import gr.ihu.hospital.service.BookingCheck;
import gr.ihu.hospital.web.dto.BookingForm;
import jakarta.validation.Valid;

/**
 * UC-14 Κράτηση Ραντεβού: φόρμα (UI-11) → έλεγχος UC-13 → προεπισκόπηση (UI-12) → [πληρωμή UC-16 (UI-16)] → καταχώριση.
 * UC-15 RUD Ραντεβού: του ασθενούς (UI-14) και του ιατρού (UI-15).
 */
@Controller
public class AppointmentController {

    private final AppointmentService appointments;
    private final AppointmentManagementService manage;

    public AppointmentController(AppointmentService appointments, AppointmentManagementService manage) {
        this.appointments = appointments;
        this.manage = manage;
    }

    /**
     * Βήματα 2–5: τμήμα → υπηρεσία και ιατρός (με τις ώρες υποδοχής τους) → πλέγμα ελεύθερων ωρών 14 ημερών.
     * Κάθε ελεύθερη ώρα είναι κουμπί που οδηγεί στον έλεγχο του UC-13 (βήμα 6).
     */
    @GetMapping("/appointments/new")
    public String bookForm(@ModelAttribute("form") BookingForm form, Model model) {
        model.addAttribute("departments", appointments.departments());
        if (form.getDepartmentId() != null) {
            model.addAttribute("services", appointments.services(form.getDepartmentId()));
            model.addAttribute("doctors", appointments.doctorsWithHours(form.getDepartmentId()));
        }
        if (form.getDoctorId() != null && form.getServiceId() != null) {
            model.addAttribute("days", appointments.availableSlots(form.getDoctorId(), form.getServiceId()));
        }
        return "appointment/ui-11-book";
    }

    /** Βήμα 6: Call UC-13. Διαθέσιμη → UI-12 (βήμα 7). Μη διαθέσιμη → 3 εναλλακτικές (7a1). */
    @PostMapping("/appointments/check")
    public String check(@Valid @ModelAttribute("form") BookingForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return bookForm(form, model);
        }
        BookingCheck check = appointments.check(form);
        model.addAttribute("check", check);
        return check.isAvailable() ? "appointment/ui-12-preview" : bookForm(form, model);
    }

    /** Ροή 8a1: «Πληρωμή τώρα» → UC-16. */
    @PostMapping("/appointments/payment")
    public String payment(@ModelAttribute("form") BookingForm form, Model model) {
        model.addAttribute("check", appointments.check(form));
        model.addAttribute("methods", PaymentMethod.values());
        return "appointment/ui-16-payment";
    }

    /** Βήματα 9–10: καταχώριση και μήνυμα με τον κωδικό. Αν η ώρα πιάστηκε στο μεταξύ, πίσω στο UI-11. */
    @PostMapping("/appointments")
    public String book(@Valid @ModelAttribute("form") BookingForm form, BindingResult result, Principal principal,
                       Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return bookForm(form, model);
        }
        try {
            Appointment appointment = appointments.book(principal.getName(), form);
            flash.addFlashAttribute("success", "Το ραντεβού καταχωρήθηκε. Κωδικός ραντεβού: " + appointment.getCode());
            return "redirect:/appointments/mine";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return check(form, result, model);
        }
    }

    @GetMapping("/appointments/mine")
    public String mine(Principal principal, Model model) {
        model.addAttribute("appointments", manage.mine(principal.getName()));
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("deadline", LocalDateTime.now().plusHours(Appointment.CHANGE_DEADLINE_HOURS));
        return "appointment/ui-14-mine";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        manage.cancel(principal.getName(), id);
        flash.addFlashAttribute("success", "Το ραντεβού ακυρώθηκε.");
        return "redirect:/appointments/mine";
    }

    /** UI-14b: αλλαγή ραντεβού με το ίδιο πλέγμα ωρών (ίδιος ιατρός και υπηρεσία). */
    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("appointment", manage.appointment(principal.getName(), id));
        model.addAttribute("days", manage.rescheduleOptions(principal.getName(), id));
        return "appointment/ui-14b-reschedule";
    }

    @PostMapping("/appointments/{id}/reschedule")
    public String reschedule(@PathVariable Long id, @RequestParam("slot") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                             LocalDateTime slot, Principal principal, RedirectAttributes flash) {
        BookingCheck check = manage.reschedule(principal.getName(), id, slot.toLocalDate(), slot.toLocalTime());
        if (check.isAvailable()) {
            flash.addFlashAttribute("success", "Το ραντεβού μεταφέρθηκε.");
        } else {
            flash.addFlashAttribute("error", String.join(" ", check.getConflicts())
                + (check.getAlternatives().isEmpty() ? "" : " Ελεύθερες ώρες: " + check.getAlternatives()));
        }
        return "redirect:/appointments/mine";
    }

    /** UC-28: αξιολόγηση ολοκληρωμένης επίσκεψης (φόρμα μέσα στο UI-14). */
    @PostMapping("/appointments/{id}/rating")
    public String rate(@PathVariable Long id, @RequestParam int stars, @RequestParam(required = false) String comment,
                       Principal principal, RedirectAttributes flash) {
        manage.rate(principal.getName(), id, stars, comment);
        flash.addFlashAttribute("success", "Ευχαριστούμε για την αξιολόγηση.");
        return "redirect:/appointments/mine";
    }

    @GetMapping("/appointments/doctor")
    public String doctorAppointments(Principal principal, Model model) {
        model.addAttribute("agenda", manage.doctorAgenda(principal.getName()));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("statuses", new AppointmentStatus[] {
            AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW});
        return "appointment/ui-15-doctor";
    }

    @PostMapping("/appointments/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam AppointmentStatus status, Principal principal,
                               RedirectAttributes flash) {
        manage.changeStatus(principal.getName(), id, status);
        flash.addFlashAttribute("success", "Η κατάσταση άλλαξε σε «" + status.getLabel() + "».");
        return "redirect:/appointments/doctor";
    }
}
