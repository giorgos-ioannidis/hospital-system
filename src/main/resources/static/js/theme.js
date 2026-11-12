/*
 * Φωτεινό/σκούρο θέμα. Φορτώνεται στο <head> πριν από τη σχεδίαση της σελίδας, ώστε να μην «αναβοσβήνει».
 * Προτεραιότητα: η επιλογή του χρήστη (localStorage), αλλιώς η ρύθμιση του συστήματος.
 * Κάθε κουμπί με [data-theme-toggle] εναλλάσσει το θέμα.
 */
(function () {
    var KEY = 'hs-theme';
    var root = document.documentElement;
    var media = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null;

    function stored() {
        try { return localStorage.getItem(KEY); } catch (e) { return null; }
    }

    function apply(theme) {
        root.setAttribute('data-bs-theme', theme);
        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) { meta.setAttribute('content', theme === 'dark' ? '#051d1c' : '#0a4744'); }
        var buttons = document.querySelectorAll('[data-theme-toggle]');
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
        }
    }

    apply(stored() || (media && media.matches ? 'dark' : 'light'));

    if (media && media.addEventListener) {
        media.addEventListener('change', function (e) {
            if (!stored()) { apply(e.matches ? 'dark' : 'light'); }
        });
    }

    document.addEventListener('click', function (e) {
        var button = e.target.closest ? e.target.closest('[data-theme-toggle]') : null;
        if (!button) { return; }
        var next = root.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
        try { localStorage.setItem(KEY, next); } catch (err) { /* ιδιωτική περιήγηση: ισχύει μόνο για αυτή τη σελίδα */ }
        apply(next);
    });

    document.addEventListener('DOMContentLoaded', function () {
        apply(root.getAttribute('data-bs-theme'));
    });
})();
