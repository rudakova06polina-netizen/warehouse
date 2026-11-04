package com.example.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Вариант Н–Т: поле называется кириллицей
    @NotBlank(message = "Укажите вид техники")
    @JsonProperty("вид_техники")
    @Column(name = "вид_техники", nullable = false)
    private String вид_техники;

    @NotBlank(message = "Укажите группу техники")
    @Column(name = "tech_group", nullable = false)
    private String techGroup;

    @NotNull(message = "Укажите дату ввоза")
    @Column(name = "date_in", nullable = false)
    private LocalDate dateIn;

    // null — техника ещё на складе
    @Column(name = "date_out")
    private LocalDate dateOut;

    @NotBlank(message = "Укажите ФИО водителя")
    @Column(name = "driver_name", nullable = false)
    private String driverName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getВид_техники() { return вид_техники; }
    public void setВид_техники(String вид_техники) { this.вид_техники = вид_техники; }
    public String getTechGroup() { return techGroup; }
    public void setTechGroup(String techGroup) { this.techGroup = techGroup; }
    public LocalDate getDateIn() { return dateIn; }
    public void setDateIn(LocalDate dateIn) { this.dateIn = dateIn; }
    public LocalDate getDateOut() { return dateOut; }
    public void setDateOut(LocalDate dateOut) { this.dateOut = dateOut; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
}
