package gr.ihu.hospital.notification;

import gr.ihu.hospital.domain.record.ExamOrder;

/** Γεγονός εξέτασης: παραγγέλθηκε (UC-20) ή βγήκε αποτέλεσμα (UC-21). */
public final class ExamEvent {

    /** Τι συνέβη στην εξέταση. */
    public enum Type {
        ORDERED, RESULT_READY
    }

    private final ExamOrder order;
    private final Type type;

    public ExamEvent(ExamOrder order, Type type) {
        this.order = order;
        this.type = type;
    }

    public ExamOrder getOrder() {
        return order;
    }

    public Type getType() {
        return type;
    }
}
