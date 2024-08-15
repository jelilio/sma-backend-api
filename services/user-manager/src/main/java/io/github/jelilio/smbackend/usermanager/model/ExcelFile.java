package io.github.jelilio.smbackend.usermanager.model;

import com.poiji.annotation.ExcelCellName;
import io.github.jelilio.smbackend.common.entity.enumeration.UserType;

import java.time.LocalDate;
import java.util.Objects;

public class ExcelFile {
  @ExcelCellName("idnumber")
  public String idNumber;
  @ExcelCellName("name")
  public String name;
  @ExcelCellName("email")
  public String email;
  @ExcelCellName("type")
  public UserType type;
  @ExcelCellName("enabled")
  public boolean enabled = true;
  @ExcelCellName("birthdate")
  public LocalDate birthDate;
  @ExcelCellName("course")
  public String course;
  @ExcelCellName("school")
  public String school;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExcelFile excelFile = (ExcelFile) o;
    return Objects.equals(idNumber, excelFile.idNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idNumber);
  }
}
