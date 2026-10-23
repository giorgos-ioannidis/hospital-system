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

import gr.ihu.hospital.domain.user.Role;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.service.AccountService;
import gr.ihu.hospital.service.StaffAccountService;
import gr.ihu.hospital.web.dto.ContactForm;
import gr.ihu.hospital.web.dto.PasswordForm;
import gr.ihu.hospital.web.dto.PatientRegistrationForm;
import gr.ihu.hospital.web.dto.StaffAccountForm;
import gr.ihu.hospital.web.dto.StaffEditForm;
import jakarta.validation.Valid;

/** UC-02 Εγγραφή Ασθενούς (UI-02), UC-03 CRUD Λογαριασμού Προσωπικού (UI-03), UC-04 Διαχείριση Προφίλ (UI-04). */
@Controller
public class AccountController {

    private final AccountService accounts;
    private final StaffAccountService staffAccounts;

    public AccountController(AccountService accounts, StaffAccountService staffAccounts) {
        this.accounts = accounts;
        this.staffAccounts = staffAccounts;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new PatientRegistrationForm());
        return "account/ui-02-register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") PatientRegistrationForm form, BindingResult result,
                           RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                accounts.registerPatient(form);
                flash.addFlashAttribute("success", "Ο λογαριασμός δημιουργήθηκε. Συνδεθείτε.");
                return "redirect:/login";
            } catch (IllegalArgumentException e) {
                result.reject("register", e.getMessage());
            }
        }
        return "account/ui-02-register";
    }

    @GetMapping("/staff")
    public String staffList(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new StaffAccountForm());
        }
        model.addAttribute("accounts", staffAccounts.staffAccounts());
        model.addAttribute("roles", new Role[] {Role.DOCTOR, Role.NURSE, Role.SUPERVISOR});
        model.addAttribute("departments", staffAccounts.departments());
        model.addAttribute("specialties", staffAccounts.specialties());
        return "account/ui-03a-staff";
    }

    @PostMapping("/staff")
    public String createStaff(@Valid @ModelAttribute("form") StaffAccountForm form, BindingResult result,
                              Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                staffAccounts.createStaff(form);
                flash.addFlashAttribute("success", "Ο λογαριασμός " + form.getUsername() + " δημιουργήθηκε.");
                return "redirect:/staff";
            } catch (IllegalArgumentException e) {
                result.reject("staff", e.getMessage());
            }
        }
        return staffList(model);
    }

    @GetMapping("/staff/{id}")
    public String editStaff(@PathVariable Long id, Model model) {
        User user = staffAccounts.staffAccount(id);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", StaffEditForm.of(user));
        }
        model.addAttribute("account", user);
        model.addAttribute("departments", staffAccounts.departments());
        model.addAttribute("specialties", staffAccounts.specialties());
        return "account/ui-03b-staff-edit";
    }

    @PostMapping("/staff/{id}")
    public String updateStaff(@PathVariable Long id, @Valid @ModelAttribute("form") StaffEditForm form,
                              BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                staffAccounts.updateStaff(id, form);
                flash.addFlashAttribute("success", "Τα στοιχεία αποθηκεύτηκαν.");
                return "redirect:/staff";
            } catch (IllegalArgumentException e) {
                result.reject("staff", e.getMessage());
            }
        }
        return editStaff(id, model);
    }

    @PostMapping("/staff/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam boolean active, Principal principal,
                            RedirectAttributes flash) {
        staffAccounts.setActive(id, active, principal.getName());
        flash.addFlashAttribute("success", active ? "Ο λογαριασμός ενεργοποιήθηκε." : "Ο λογαριασμός απενεργοποιήθηκε.");
        return "redirect:/staff";
    }

    @PostMapping("/staff/{id}/unlock")
    public String unlock(@PathVariable Long id, RedirectAttributes flash) {
        staffAccounts.unlock(id);
        flash.addFlashAttribute("success", "Ο λογαριασμός ξεκλειδώθηκε.");
        return "redirect:/staff";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = accounts.profile(principal.getName());
        ContactForm contact = new ContactForm();
        contact.setEmail(user.getEmail());
        contact.setPhone(user.getPhone());
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", contact);
        }
        if (!model.containsAttribute("password")) {
            model.addAttribute("password", new PasswordForm());
        }
        model.addAttribute("user", user);
        return "account/ui-04-profile";
    }

    @PostMapping("/profile/contact")
    public String updateContact(@Valid @ModelAttribute("contact") ContactForm form, BindingResult result,
                                Principal principal, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                accounts.updateContact(principal.getName(), form);
                flash.addFlashAttribute("success", "Τα στοιχεία επικοινωνίας αποθηκεύτηκαν.");
                return "redirect:/profile";
            } catch (IllegalArgumentException e) {
                result.reject("contact", e.getMessage());
            }
        }
        return profile(principal, model);
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("password") PasswordForm form, BindingResult result,
                                 Principal principal, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                accounts.changePassword(principal.getName(), form);
                flash.addFlashAttribute("success", "Ο κωδικός άλλαξε.");
                return "redirect:/profile";
            } catch (IllegalArgumentException e) {
                result.reject("password", e.getMessage());
            }
        }
        return profile(principal, model);
    }
}
