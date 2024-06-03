package com.spring.rewards.services;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.rewards.Repository.EmployeeRepository;
import com.spring.rewards.Repository.ReconciliationRepository;
import com.spring.rewards.entity.Employee;
import com.spring.rewards.entity.Reconciliation;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReconciliationService {

    @Autowired
    private EmployeeRepository empRepo;

    @Autowired
    private ReconciliationRepository reconRepo;

    public List<Reconciliation> getGrandchildrenReconciliationRecords(Long parentEmpId) {
        Optional<Employee> parentOptional = empRepo.findById(parentEmpId);
        if (parentOptional.isPresent()) {
            Employee parentEmployee = parentOptional.get();

            List<Reconciliation> reconciliationRecords = new ArrayList<>();
            collectGrandchildrenReconciliation(parentEmployee, reconciliationRecords);

            return reconciliationRecords;
        } else {
            return Collections.emptyList();
        }
    }

    private void collectGrandchildrenReconciliation(Employee parentEmployee,
            List<Reconciliation> reconciliationRecords) {
        List<Employee> children = parentEmployee.getChildern();
        for (Employee child : children) {
            List<Reconciliation> childReconciliations = reconRepo.findByEmployeeNumber(child.getEmpId());
            reconciliationRecords.addAll(childReconciliations);

            // Recursively collect reconciliation records of grandchildren
            collectGrandchildrenReconciliation(child, reconciliationRecords);
        }
    }

    public void reconcile(InputStream otlInputStream, InputStream timexInputStream, String weekNum, String periodNum,
            String yearNum) throws IOException {
        Map<Long, String> idMap = fetchEmployeesWithBothIds();
        System.out.println(idMap);

        if (idMap.isEmpty()) {
            System.out.println("No employee IDs found in Employee Table");
            return;
        }
        Map<String, Object> resultMap = new TreeMap<>();

        readAndProcessOtlFile(otlInputStream, "Peopleone Number", idMap.keySet(), "Hours", "Project Number",
                "Aa Type Description", "Aa Type", resultMap);
        System.out.println(resultMap);
//      Map<String, Float> hoursMap = (Map<String, Float>) otlHoursMap.get("hoursMap");
//      Map<String, Float> blankHoursMap = (Map<String, Float>) otlHoursMap.get("blankHoursMap");

        Map<String, Float> hoursMap = null;
        Map<String, Float> blankHoursMap = null;

        Object hoursMapObj = resultMap.get("hoursMap");
        Object blankHoursMapObj = resultMap.get("blankHoursMap");

        if (hoursMapObj instanceof Map) {
            hoursMap = (Map<String, Float>) hoursMapObj;
        }

        if (blankHoursMapObj instanceof Map) {
            blankHoursMap = (Map<String, Float>) blankHoursMapObj;
        }

        LinkedHashSet<String> tescoIdsSet = new LinkedHashSet<>(idMap.values());

        Map<String, Float> timexHoursMap = readAndProcessTimexFile(timexInputStream, "Employee_Number", tescoIdsSet,
                "Booked_Hours", "Project_Code");

        System.out.println(idMap.values());
        System.out.println(timexHoursMap);

        for (Map.Entry<Long, String> entry : idMap.entrySet()) {
            Long empId = entry.getKey();
            String timexId = entry.getValue();
            Float timexHours = timexHoursMap.getOrDefault(timexId, null);

            Float otlHours = hoursMap.getOrDefault(String.valueOf(empId), 0.0f);

            Float blankHours = blankHoursMap.getOrDefault(String.valueOf(empId), 0.0f);

            Float difference = (timexHours != null) ? otlHours - timexHours : null;
            insertIntoReconciliationTable(empId, difference, otlHours, timexHours, weekNum, periodNum, yearNum,
                    blankHours);
        }
    }

//  public void reconcile(InputStream otlInputStream, InputStream timexInputStream, String weekNum, String periodNum,
//          String yearNum) throws IOException {
//      Map<Long, String> idMap = fetchEmployeesWithBothIds();
//      System.out.println(idMap);
//
//      if (idMap.isEmpty()) {
//          System.out.println("No employee IDs found in Employee Table");
//          return;
//      }
//
//      Map<String, Object> otlHoursMap = readAndProcessOtlFile(otlInputStream, "Peopleone Number", idMap.keySet(),
//              "Hours", "Project Number", "Aa Type Description", "Aa Type");
//      System.out.println(otlHoursMap);
//      Map<String, Float> hoursMap = (Map<String, Float>) otlHoursMap.get("hoursMap");
//      Map<String, Float> blankHoursMap = (Map<String, Float>) otlHoursMap.get("blankHoursMap");
//      for (Map.Entry<String, Float> entry : hoursMap.entrySet()) {
//          String empId = entry.getKey();
//          Float hours = entry.getValue();
//      }
//
//      // Iterate over blankHoursMap to get empId and blank hours
//      for (Map.Entry<String, Float> entry : blankHoursMap.entrySet()) {
//          String empId = entry.getKey();
//          Float blankHours = entry.getValue();
//      }
//
//      LinkedHashSet<String> tescoIdsSet = new LinkedHashSet<>(idMap.values());
//
//      Map<String, Float> timexHoursMap = readAndProcessTimexFile(timexInputStream, "Employee_Number", tescoIdsSet,
//              "Booked_Hours", "Project_Code");
//
//      System.out.println(idMap.values());
//      System.out.println(timexHoursMap);
//
//      for (Map.Entry<Long, String> entry : idMap.entrySet()) {
//          Long empId = entry.getKey();
//          String timexId = entry.getValue();
//          Float timexHours = timexHoursMap.getOrDefault(timexId, null);
//
//          if (otlHoursMap.containsKey(String.valueOf(empId))) {
//              Float otlHours = hoursMap.get(String.valueOf(empId));
//              Float blankHours = blankHoursMap.get(String.valueOf(empId));
//
//              Float difference = (timexHours != null) ? otlHours - timexHours : null;
//              insertIntoReconciliationTable(empId, difference, otlHours, timexHours, weekNum, periodNum, yearNum,
//                      blankHours);
//          } else {
//              Float otlHours = 0.0f;
//              Float difference = (timexHours != null) ? -timexHours : null;
//              Float blankHours = blankHoursMap.get(String.valueOf(empId));
//
//              insertIntoReconciliationTable(empId, difference, otlHours, timexHours, weekNum, periodNum, yearNum,
//                      blankHours);
//          }
//      }
//  }

    private Map<Long, String> fetchEmployeesWithBothIds() {
        Map<Long, String> idMap = new HashMap<>();

        Iterable<Employee> employees = empRepo.findAll();
        for (Employee employee : employees) {
//          if (employee.getEmpId() != null && employee.getTescoId() != null) {
            idMap.put(employee.getEmpId(), employee.getTescoId());
            // }
        }
        return idMap;
    }

//  private Map<String, Float> readAndProcessOtlFile(InputStream inputStream, String employeeNumberColumnName,
//          Set<Long> empIds, String... hourColumnNames) {
//      Map<String, Float> hoursMap = new HashMap<>();
//
//      try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//          Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
//
//          int employeeNumberColumnIndex = -1;
//          int[] hourColumnIndices = new int[hourColumnNames.length];
//
//          Row headerRow = sheet.getRow(0);
//          if (headerRow != null) {
//              for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//                  Cell cell = headerRow.getCell(i);
//                  if (cell != null && cell.getCellType() == CellType.STRING) {
//                      String columnName = cell.getStringCellValue().trim();
//                      if (columnName.equalsIgnoreCase(employeeNumberColumnName)) {
//                          employeeNumberColumnIndex = i;
//                      } else {
//                          for (int j = 0; j < hourColumnNames.length; j++) {
//                              if (columnName.equalsIgnoreCase(hourColumnNames[j])) {
//                                  hourColumnIndices[j] = i;
//                                  break;
//                              }
//                          }
//                      }
//                  }
//              }
//          }
//
//          for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//              Row row = sheet.getRow(i);
//              if (row != null) {
//                  Cell empIdCell = row.getCell(employeeNumberColumnIndex);
//                  if (empIdCell != null && empIdCell.getCellType() == CellType.NUMERIC) {
//                      long empId = (long) empIdCell.getNumericCellValue();
//                      if (empIds.contains(empId)) {
//                          float totalHours = 0.0f;
//                          for (int hourColumnIndex : hourColumnIndices) {
//                              Cell cell = row.getCell(hourColumnIndex);
//                              if (cell != null && cell.getCellType() == CellType.NUMERIC) {
//                                  totalHours += (float) cell.getNumericCellValue();
//                              }
//                          }
//                          hoursMap.put(String.valueOf(empId), totalHours);
//                      }
//                  }
//              }
//          }
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//
//      return hoursMap;
//  }

//  private Map<String, Object> readAndProcessOtlFile(InputStream inputStream, String employeeNumberColumnName,
//          Set<Long> empIds, String hoursColumnName, String projectNumberColumnName, String aaTypeColumnName,
//          String aaColumnName) {
//      Map<String, Float> hoursMap = new HashMap<>();
//      Map<String, Float> blankHoursMap = new HashMap<>();
//
//
//      try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//          Sheet sheet = workbook.getSheetAt(0);
//          int employeeNumberColumnIndex = -1;
//          int hoursColumnIndex = -1;
//          int projectNumberColumnIndex = -1;
//          int aaTypeColumnIndex = -1;
//          int aaColumnIndex = -1;
//
//          Row headerRow = sheet.getRow(0);
//          if (headerRow != null) {
//              for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//                  Cell cell = headerRow.getCell(i);
//                  if (cell != null && cell.getCellType() == CellType.STRING) {
//                      String columnName = cell.getStringCellValue().trim();
//                      if (columnName.equalsIgnoreCase(employeeNumberColumnName)) {
//                          employeeNumberColumnIndex = i;
//                      } else if (columnName.equalsIgnoreCase(hoursColumnName)) {
//                          hoursColumnIndex = i;
//                      } else if (columnName.equalsIgnoreCase(projectNumberColumnName)) {
//                          projectNumberColumnIndex = i;
//                      } else if (columnName.equals(aaTypeColumnName)) {
//                          aaTypeColumnIndex = i;
//                      } else if (columnName.equals(aaColumnName)) {
//                          aaColumnIndex = i;
//                      }
//                  }
//              }
//          }
//
//          for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//              Row row = sheet.getRow(i);
//              if (row != null) {
//                  Cell empIdCell = row.getCell(employeeNumberColumnIndex);
//                  Cell projectNumberCell = row.getCell(projectNumberColumnIndex);
//                  Cell aaTypeDescriptionCell = row.getCell(aaTypeColumnIndex);
//                  Cell aaTypeCell = row.getCell(aaColumnIndex);
//
//                  if (empIdCell != null && empIdCell.getCellType() == CellType.NUMERIC) {
//                      long empId = (long) empIdCell.getNumericCellValue();
//                      String projectNumber = null;
//                      if (projectNumberCell != null) {
//                          if (projectNumberCell.getCellType() == CellType.NUMERIC) {
//                          } else if (projectNumberCell.getCellType() == CellType.STRING) {
//                              projectNumber = projectNumberCell.getStringCellValue().trim();
//                          }
//                      }
//                      String aaTypeDescription = null;
//
//                      if (aaTypeDescriptionCell != null) {
//                          if (aaTypeDescriptionCell.getCellType() == CellType.NUMERIC) {
//
//                          } else if (aaTypeDescriptionCell.getCellType() == CellType.STRING) {
//                              aaTypeDescription = aaTypeDescriptionCell.getStringCellValue().trim();
//                          }
//                      }
//
//                      String aaType = null;
//
//                      if (aaTypeCell != null) {
//                          if (aaTypeCell.getCellType() == CellType.BLANK) {
//                              
//
//                              Cell hoursCell1 = row.getCell(hoursColumnIndex);
//                              if (hoursCell1 != null && hoursCell1.getCellType() == CellType.NUMERIC) {
//                                  float hours = (float) hoursCell1.getNumericCellValue();
//                                  if (blankHoursMap.containsKey(String.valueOf(empId))) {
//                                      hours += blankHoursMap.get(String.valueOf(empId));
//                                  }
//                                  blankHoursMap.put(String.valueOf(empId), hours);
//                              }
//
//
//                          } 
//                          
//                          
//                      }
//                      
//                      if (empIds.contains(empId) && !isExcludedProjectNumber(projectNumber)
//                              && !isExcludeAaTypeDescription(aaTypeDescription)) {
//
//                          Cell hoursCell = row.getCell(hoursColumnIndex);
//                          if (hoursCell != null && hoursCell.getCellType() == CellType.NUMERIC) {
//                              float hours = (float) hoursCell.getNumericCellValue();
//                              if (hoursMap.containsKey(String.valueOf(empId))) {
//                                  hours += hoursMap.get(String.valueOf(empId));
//                              }
//                              hoursMap.put(String.valueOf(empId), hours);
//                          }
//                      }
//                  }
//              }
//          }
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//      Map<String,Object>resultMap= new HashMap<>();
//      resultMap.put("hoursMap",hoursMap);
//      resultMap.put("blankHoursMap", blankHoursMap);
//      
//
//      return resultMap;
//  }

    private void readAndProcessOtlFile(InputStream inputStream, String employeeNumberColumnName, Set<Long> empIds,
            String hoursColumnName, String projectNumberColumnName, String aaTypeColumnName, String aaColumnName,
            Map<String, Object> resultMap2) {
        Map<String, Float> hoursMap = new HashMap<>();
        Map<String, Float> blankHoursMap = new HashMap<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int employeeNumberColumnIndex = -1;
            int hoursColumnIndex = -1;
            int projectNumberColumnIndex = -1;
            int aaTypeColumnIndex = -1;
            int aaColumnIndex = -1;

            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String columnName = cell.getStringCellValue().trim();
                        if (columnName.equalsIgnoreCase(employeeNumberColumnName)) {
                            employeeNumberColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(hoursColumnName)) {
                            hoursColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(projectNumberColumnName)) {
                            projectNumberColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(aaTypeColumnName)) {
                            aaTypeColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(aaColumnName)) {
                            aaColumnIndex = i;
                        }
                    }
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell empIdCell = row.getCell(employeeNumberColumnIndex);
                    Cell projectNumberCell = row.getCell(projectNumberColumnIndex);
                    Cell aaTypeDescriptionCell = row.getCell(aaTypeColumnIndex);
                    Cell aaTypeCell = row.getCell(aaColumnIndex);

                    if (empIdCell != null && empIdCell.getCellType() == CellType.NUMERIC) {
                        long empId = (long) empIdCell.getNumericCellValue();
                        String projectNumber = null;
                        if (projectNumberCell != null) {
                            if (projectNumberCell.getCellType() == CellType.NUMERIC) {
                            } else if (projectNumberCell.getCellType() == CellType.STRING) {
                                projectNumber = projectNumberCell.getStringCellValue().trim();
                            }
                        }
                        String aaTypeDescription = null;

                        if (aaTypeDescriptionCell != null) {
                            if (aaTypeDescriptionCell.getCellType() == CellType.NUMERIC) {

                            } else if (aaTypeDescriptionCell.getCellType() == CellType.STRING) {
                                aaTypeDescription = aaTypeDescriptionCell.getStringCellValue().trim();
                            }
                        }

//                      String aaType = null;
//
//                      if (aaTypeCell != null) {
//                          if (aaTypeCell.getCellType() == CellType.BLANK) {
//
//                          } 
//                          
//                      }
//                      
//                      if (!isExcludedCheckbox(aaTypeCell)) {
//                          Cell hoursCell1 = row.getCell(hoursColumnIndex);
//                          if (hoursCell1 != null && hoursCell1.getCellType() == CellType.NUMERIC) {
//                              float hours = (float) hoursCell1.getNumericCellValue();
//                              if (blankHoursMap.containsKey(String.valueOf(empId))) {
//                                  hours += blankHoursMap.get(String.valueOf(empId));
//                              }
//                              blankHoursMap.put(String.valueOf(empId), hours);
//                          }
//                      }

//                      String aaType = null;
//
//                      if (aaTypeCell != null && !isExcludedCheckbox(aaTypeCell)) {
//                          Cell hoursCell1 = row.getCell(hoursColumnIndex);
//                          if (hoursCell1 != null && hoursCell1.getCellType() == CellType.NUMERIC) {
//                              float hours = (float) hoursCell1.getNumericCellValue();
//                              if (blankHoursMap.containsKey(String.valueOf(empId))) {
//                                  hours += blankHoursMap.get(String.valueOf(empId));
//                              }
//                              blankHoursMap.put(String.valueOf(empId), hours);
//                          }
//                      }

//                        String aaType = null;
//
//                          if (aaTypeCell != null && aaTypeCell.getCellType() == CellType.BLANK) {
//                              Cell hoursCell1 = row.getCell(hoursColumnIndex);
//                              if (hoursCell1 != null && hoursCell1.getCellType() == CellType.NUMERIC) {
//                                  float hours = (float) hoursCell1.getNumericCellValue();
//                                  if (blankHoursMap.containsKey(String.valueOf(empId))) {
//                                      hours += blankHoursMap.get(String.valueOf(empId));
//                                  }
//                                  blankHoursMap.put(String.valueOf(empId), hours);
//                              }
//                          }

                        String aaType = null;

                        if (aaTypeCell != null) {
                             if (aaTypeCell.getCellType() == CellType.STRING) {
                                aaType = "";
                            }else if(aaTypeCell.getCellType()==CellType.BLANK) {
                                aaType="";
                            }else if(aaTypeCell.getCellType()==CellType.NUMERIC) {
                                aaType="";
                            }
                        }
            
                        
                        
                        if (empIds.contains(empId) && aaType == null && !isExcludedProjectNumber(projectNumber) && !isExcludeAaTypeDescription(aaTypeDescription)) {

                            Cell hoursCell = row.getCell(hoursColumnIndex);
                            if (hoursCell != null && hoursCell.getCellType() == CellType.NUMERIC) {
                                float hours = (float) hoursCell.getNumericCellValue();
                                if (blankHoursMap.containsKey(String.valueOf(empId))) {
                                    hours += blankHoursMap.get(String.valueOf(empId));
                                }
                                blankHoursMap.put(String.valueOf(empId), hours);
                            }
                        }
                        
                        

                        if (empIds.contains(empId) && !isExcludedProjectNumber(projectNumber)
                                && !isExcludeAaTypeDescription(aaTypeDescription)) {

                            Cell hoursCell = row.getCell(hoursColumnIndex);
                            if (hoursCell != null && hoursCell.getCellType() == CellType.NUMERIC) {
                                float hours = (float) hoursCell.getNumericCellValue();
                                if (hoursMap.containsKey(String.valueOf(empId))) {
                                    hours += hoursMap.get(String.valueOf(empId));
                                }
                                hoursMap.put(String.valueOf(empId), hours);
                            }

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultMap2.put("hoursMap", hoursMap);
        resultMap2.put("blankHoursMap", blankHoursMap);

    }

    private boolean isExcludedCheckbox(String aaType) {
        
        return new HashSet<>(List.of("COPH", "COWD", "COWE", "SOPH", "SOWD", "SOWE")).contains(aaType);
    }

//  private Map<String, Float> readAndProcessTimexFile(InputStream inputStream, String employeeNumberColumnName,
//          Set<String> tescoIds, String bookedHoursColumnName, String projectCodeColumnName) {
//      Map<String, Float> hoursMap = new HashMap<>();
//
//      try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//          Sheet sheet = workbook.getSheetAt(0);
//          int employeeNumberColumnIndex = -1;
//          int bookedHoursColumnIndex = -1;
//          int projectCodeColumnIndex = -1;
//
//          Row headerRow = sheet.getRow(0);
//          if (headerRow != null) {
//              for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//                  Cell cell = headerRow.getCell(i);
//                  if (cell != null && cell.getCellType() == CellType.STRING) {
//                      String columnName = cell.getStringCellValue().trim();
//                      if (columnName.equalsIgnoreCase(employeeNumberColumnName)) {
//                          employeeNumberColumnIndex = i;
//                      } else if (columnName.equalsIgnoreCase(bookedHoursColumnName)) {
//                          bookedHoursColumnIndex = i;
//                      } else if (columnName.equalsIgnoreCase(projectCodeColumnName)) {
//                          projectCodeColumnIndex = i;
//                      }
//                  }
//              }
//          }
//
//          for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//              Row row = sheet.getRow(i);
//              if (row != null) {
//                  Cell tescoIdCell = row.getCell(employeeNumberColumnIndex);
//                  Cell projectCodeCell = row.getCell(projectCodeColumnIndex);
//                  if (tescoIdCell != null && tescoIdCell.getCellType() == CellType.STRING) {
//                      String tescoId = tescoIdCell.getStringCellValue();
//                      String projectCode = projectCodeCell != null ? projectCodeCell.getStringCellValue() : "";
//                      if (tescoIds.contains(tescoId) && !isExcludedProjectCode(projectCode)) {
//                          Cell bookedHoursCell = row.getCell(bookedHoursColumnIndex);
//                          if (bookedHoursCell != null && bookedHoursCell.getCellType() == CellType.NUMERIC) {
//                              float bookedHours = (float) bookedHoursCell.getNumericCellValue();
//                              if (hoursMap.containsKey(tescoId)) {
//                                  bookedHours += hoursMap.get(tescoId);
//                              }
//                              hoursMap.put(tescoId, bookedHours);
//                          }
//                      }
//                  }
//              }
//          }
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//
//      return hoursMap;
//  }

    private Map<String, Float> readAndProcessTimexFile(InputStream inputStream, String employeeNumberColumnName,
            Set<String> tescoIds, String bookedHoursColumnName, String projectCodeColumnName) {
        Map<String, Float> hoursMap = new HashMap<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int employeeNumberColumnIndex = -1;
            int bookedHoursColumnIndex = -1;
            int projectCodeColumnIndex = -1;

            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String columnName = cell.getStringCellValue().trim();
                        if (columnName.equalsIgnoreCase(employeeNumberColumnName)) {
                            employeeNumberColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(bookedHoursColumnName)) {
                            bookedHoursColumnIndex = i;
                        } else if (columnName.equalsIgnoreCase(projectCodeColumnName)) {
                            projectCodeColumnIndex = i;
                        }
                    }
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell tescoIdCell = row.getCell(employeeNumberColumnIndex);
                    Cell bookedHoursCell = row.getCell(bookedHoursColumnIndex);
                    Cell projectCodeCell = row.getCell(projectCodeColumnIndex);
                    if (tescoIdCell != null && tescoIdCell.getCellType() == CellType.STRING) {
                        String tescoId = tescoIdCell.getStringCellValue();
                        String projectCode = null;

                        if (projectCodeCell != null)

                            if (projectCodeCell.getCellType() == CellType.NUMERIC) {
                            } else if (projectCodeCell.getCellType() == CellType.STRING) {
                                projectCode = projectCodeCell.getStringCellValue().trim();
                            }

                        if (tescoIds.contains(tescoId) && !isExcludedProjectCode(projectCode)) {
                            if (bookedHoursCell != null && bookedHoursCell.getCellType() == CellType.NUMERIC) {
                                float bookedHours = (float) bookedHoursCell.getNumericCellValue();
                                if (hoursMap.containsKey(tescoId)) {
                                    float totalHours = hoursMap.get(tescoId);
                                    totalHours += bookedHours;
                                    hoursMap.put(tescoId, totalHours);
                                } else {
                                    hoursMap.put(tescoId, bookedHours);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hoursMap;
    }

    private boolean isExcludedProjectNumber(String projectNumber) {
        Set<String> excludedProjectNumbers = new HashSet<>(Arrays.asList("40-B797", "01-B797", "02-B797", "03-B797",
                "04-B797", "08-B797", "09-B797", "102-B797", "104-B797", "14-B797"));
        return excludedProjectNumbers.contains(projectNumber);
    }

    private boolean isExcludeAaTypeDescription(String aaTypeDescription) {
        Set<String> excludeAaTypeDes = new HashSet<>(Arrays.asList("Month End Adjustment"));
        return excludeAaTypeDes.contains(aaTypeDescription);
    }

    private boolean isExcludedProjectCode(String projectNumber) {
        Set<String> excludedProjectNumbers = new HashSet<>(Arrays.asList("W11000"));
        return excludedProjectNumbers.contains(projectNumber);
    }

    private void insertIntoReconciliationTable(Long empId, Float difference, Float otlHours, Float timexHours,
            String weekNum, String periodNum, String yearNum, Float blankHours) {

        final float DELTA = 0.0001f;

        Reconciliation reconciliation = new Reconciliation();
        reconciliation.setEmployeeNumber(empId);
        reconciliation.setDifferenceInHours(difference);
        reconciliation.setOtlBookedHours(otlHours);
        reconciliation.setTimexBookedHours(timexHours);
        reconciliation.setWeekNumber(weekNum);
        reconciliation.setPeriodName(periodNum);
        reconciliation.setYearNumber(yearNum);
        reconciliation.setBlankHours(blankHours);

        if (timexHours != null && Math.abs(timexHours - otlHours) < DELTA) {
            reconciliation.setStatus("Matched");
        } else if (timexHours == null) {
            reconciliation.setStatus("Timex doesn't exist for the employee");
        } else {
            reconciliation.setStatus("Not Matched");
        }

        Optional<Employee> optionalEmployee = empRepo.findById(empId);
        ;
        if (optionalEmployee.isEmpty()) {
            throw new EntityNotFoundException("employee with id" + empId + "does not exist.");

        }
        Employee employee = optionalEmployee.get();
        String empName = employee.getEmpName();
        reconciliation.setEmpName(empName);
        reconRepo.save(reconciliation);
    }

}