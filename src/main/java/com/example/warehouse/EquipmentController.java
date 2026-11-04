package com.example.warehouse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private static final int HISTOGRAM_DAYS = 30; // вариант Н–Т

    private final EquipmentRepository repo;

    public EquipmentController(EquipmentRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Equipment> list(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return repo.findAllSorted();
        }
        return repo.search(q.trim());
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", repo.count());
    }

    @PostMapping
    public Equipment create(@Valid @RequestBody Equipment e) {
        checkDates(e);
        e.setId(null);
        return repo.save(e);
    }

    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @Valid @RequestBody Equipment e) {
        checkDates(e);
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена");
        }
        e.setId(id);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена");
        }
        repo.deleteById(id);
    }

    // Гистограмма: сколько единиц техники вывезено по дням (по дате вывоза), последние 30 дней
    @GetMapping("/histogram")
    public List<Map<String, Object>> histogram() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(HISTOGRAM_DAYS - 1L);

        Map<LocalDate, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < HISTOGRAM_DAYS; i++) {
            counts.put(start.plusDays(i), 0);
        }
        for (Equipment e : repo.findByDateOutBetween(start, today)) {
            counts.merge(e.getDateOut(), 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((d, c) -> result.add(Map.of("date", d.toString(), "count", c)));
        return result;
    }

    private void checkDates(Equipment e) {
        if (e.getDateOut() != null && e.getDateOut().isBefore(e.getDateIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата вывоза не может быть раньше даты ввоза");
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> onValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("Некорректные данные");
        return Map.of("message", msg);
    }
}
