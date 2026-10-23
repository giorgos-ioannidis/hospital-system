package gr.ihu.hospital.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gr.ihu.hospital.domain.organization.Room;
import gr.ihu.hospital.service.HospitalInfoService;

/** UC-25 Χάρτης Αιθουσών & Τμημάτων (UI-22), UC-12 Πληροφορίες Νοσοκομείου (UI-23). */
@Controller
public class InfoController {

    private final HospitalInfoService info;

    public InfoController(HospitalInfoService info) {
        this.info = info;
    }

    @GetMapping("/info/map")
    public String map(@RequestParam(required = false) Long room, Model model) {
        model.addAttribute("floors", info.floors());
        if (room != null) {
            Room selected = info.room(room);
            model.addAttribute("selected", selected);
            model.addAttribute("todayHours", info.todayIn(selected));
        }
        return "info/ui-22-map";
    }

    @GetMapping("/info/directory")
    public String directory(Model model) {
        model.addAttribute("departments", info.directory());
        return "info/ui-23-directory";
    }
}
