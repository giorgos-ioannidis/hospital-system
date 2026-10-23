package gr.ihu.hospital.web.controller;

import java.security.Principal;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.ihu.hospital.service.ExportService;

/** UC-26 Εξαγωγή PDF. Ο έλεγχος πρόσβασης γίνεται στο ExportService. */
@RestController
public class ExportController {

    private final ExportService export;

    public ExportController(ExportService export) {
        this.export = export;
    }

    @GetMapping("/export/schedule")
    public ResponseEntity<byte[]> schedule(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                                           Principal principal) {
        YearMonth shown = month == null ? YearMonth.now() : month;
        return pdf("programma-" + shown + ".pdf", export.mySchedule(principal.getName(), shown));
    }

    @GetMapping("/export/appointments")
    public ResponseEntity<byte[]> appointments(Principal principal) {
        return pdf("rantevou.pdf", export.myAppointments(principal.getName()));
    }

    @GetMapping("/export/record/{patientId}")
    public ResponseEntity<byte[]> record(@PathVariable Long patientId, Principal principal) {
        return pdf("istoriko.pdf", export.record(principal.getName(), patientId));
    }

    @GetMapping("/export/exam/{id}")
    public ResponseEntity<byte[]> exam(@PathVariable Long id, Principal principal) {
        return pdf("apotelesma-" + id + ".pdf", export.examResult(principal.getName(), id));
    }

    private static ResponseEntity<byte[]> pdf(String filename, byte[] content) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
            .body(content);
    }
}
