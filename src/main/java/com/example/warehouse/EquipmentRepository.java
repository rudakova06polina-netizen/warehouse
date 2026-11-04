package com.example.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Сортировка по двум полям: дата ввоза, затем вид техники
    @Query("select e from Equipment e order by e.dateIn asc, e.вид_техники asc")
    List<Equipment> findAllSorted();

    @Query("""
           select e from Equipment e
           where lower(e.вид_техники) like lower(concat('%', :q, '%'))
              or lower(e.techGroup) like lower(concat('%', :q, '%'))
              or lower(e.driverName) like lower(concat('%', :q, '%'))
           order by e.dateIn asc, e.вид_техники asc
           """)
    List<Equipment> search(@Param("q") String q);

    List<Equipment> findByDateOutBetween(LocalDate from, LocalDate to);
}
