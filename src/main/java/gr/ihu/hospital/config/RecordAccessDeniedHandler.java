package gr.ihu.hospital.config;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gr.ihu.hospital.domain.record.AccessAction;
import gr.ihu.hospital.domain.user.Patient;
import gr.ihu.hospital.domain.user.User;
import gr.ihu.hospital.repository.ExamOrderRepository;
import gr.ihu.hospital.repository.UserRepository;
import gr.ihu.hospital.service.AccessLogger;
import gr.ihu.hospital.service.RecordAccessDeniedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * NFR-02: και οι απόπειρες που κόβονται από τον ρόλο (URL ή @PreAuthorize), πριν φτάσουν στο RecordAccessPolicy,
 * γράφονται στο AccessLog. Μετά δείχνει τη συνηθισμένη σελίδα 403.
 */
@Component
public class RecordAccessDeniedHandler implements AccessDeniedHandler {

    private static final Pattern RECORD = Pattern.compile("^/(?:records|export/record)/(\\d+)(?:/.*)?$");
    private static final Pattern EXAM = Pattern.compile("^/export/exam/(\\d+)$");

    private final AccessDeniedHandler delegate = new AccessDeniedHandlerImpl();
    private final AccessLogger accessLogger;
    private final UserRepository users;
    private final ExamOrderRepository exams;

    public RecordAccessDeniedHandler(AccessLogger accessLogger, UserRepository users, ExamOrderRepository exams) {
        this.accessLogger = accessLogger;
        this.users = users;
        this.exams = exams;
    }

    @Override
    @Transactional(readOnly = true)
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException denied)
            throws IOException, ServletException {
        if (!(denied instanceof RecordAccessDeniedException)) {
            logAttempt(request.getRequestURI().substring(request.getContextPath().length()));
        }
        delegate.handle(request, response, denied);
    }

    private void logAttempt(String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Patient patient = patientOf(path);
        if (authentication == null || patient == null) {
            return;
        }
        users.findByUsername(authentication.getName()).ifPresent(
            (User user) -> accessLogger.log(user, patient, path.startsWith("/export") ? AccessAction.EXPORT_PDF
                                                                                       : AccessAction.VIEW, false));
    }

    private Patient patientOf(String path) {
        Matcher record = RECORD.matcher(path);
        if (record.matches()) {
            return users.findById(Long.valueOf(record.group(1))).filter(Patient.class::isInstance)
                .map(Patient.class::cast).orElse(null);
        }
        Matcher exam = EXAM.matcher(path);
        return exam.matches() ? exams.findById(Long.valueOf(exam.group(1))).map(e -> e.getPatient()).orElse(null) : null;
    }
}
