package gr.ihu.hospital.service;

import org.springframework.security.access.AccessDeniedException;

/** Άρνηση από το RecordAccessPolicy. Έχει ήδη καταγραφεί στο AccessLog, άρα ο handler δεν την ξαναγράφει. */
public class RecordAccessDeniedException extends AccessDeniedException {

    private static final long serialVersionUID = 1L;

    public RecordAccessDeniedException(String message) {
        super(message);
    }
}
