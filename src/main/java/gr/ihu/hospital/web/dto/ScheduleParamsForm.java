package gr.ihu.hospital.web.dto;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** UC-05: παράμετροι παραγωγής προγράμματος (UI-05a). */
public class ScheduleParamsForm {

    @NotNull
    private Long departmentId;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM")
    private YearMonth month;

    @Min(8)
    @Max(80)
    private int maxWeeklyHours = 48;

    private boolean fair = true;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(int maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public boolean isFair() {
        return fair;
    }

    public void setFair(boolean fair) {
        this.fair = fair;
    }
}
