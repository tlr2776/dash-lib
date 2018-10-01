/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author tara
 */
abstract public class DashParser {

//    private String fn; //filename
    private static ArrayList<Job> jobList;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy h:m a");
    //this might break b/c all dates from Dash have times as well, but I want to disregard those if possible

    //these ints are the column #s where each value is located; doing this so that
    //I can make the parser method dynamic
    //they default to -1 so that if the column is not present, I have a way to know to ignore it (because
    //0 is generally going to be the JobID column)
    private int jobID = -1;
    private int customerName = -1;
    private int jobStatus = -1;
    private int lossCat = -1;
    private int lossType = -1;
    private int lossTypeSec = -1;
    private int referredBy = -1;
    private int referralType = -1;
    private int division = -1;

    private int totalEstimates = -1;
    private int totalInvoiced = -1;
    private int totalJobCost = -1;
    private int totalCollected = -1;

    private int paidDate = -1;
    private int startedDate = -1;
    private int invoicedDate = -1;
    private int receivedDate = -1;
    private int closedDate = -1;
    private int workAuthDate = -1;

    public DashParser() {
        jobList = new ArrayList();
    }

    public void openFile(String fileName) {

        try {
            File inputFile = new File(fileName);
            XSSFWorkbook wb;
            try (InputStream inp = new FileInputStream(inputFile)) {
                wb = new XSSFWorkbook(inp);
                Sheet sheet = wb.getSheetAt(0);
                //call setColumns to define all of the column variables
                setColumns(sheet.getRow(0));
                //starting at int i = 1 to skip the first row (which is just labels)
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    parseFile(sheet.getRow(i)); //this might need to be i+1
                }
                //finally
            }
            wb.close();

        } catch (IOException ex) {
            Logger.getLogger(DashParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method sets the column number for each field, so that the parseFile
     * method can get its values dynamically, rather than relying on the report
     * never being updated.
     *
     * @param r
     */
    protected void setColumns(Row r) {
        //should this be <= instead of < ?
        for (int i = 0; i < r.getLastCellNum(); i++) {

            switch (r.getCell(i).getStringCellValue()) {
                case "Job Number":
                    jobID = i;
                    break;
                case "Customer":
                    customerName = i;
                    break;
                case "Loss Category":
                    lossCat = i;
                    break;
                case "Job Status":
                    jobStatus = i;
                    break;
                case "Type of Loss":
                    lossType = i;
                    break;
                case "Secondary Loss Type":
                    lossTypeSec = i;
                    break;
                case "Referred By":
                    referredBy = i;
                    break;
                case "Referral Type":
                    referralType = i;
                    break;
                case "Total Estimates":
                    totalEstimates = i;
                    break;
                case "Total Invoiced":
                    totalInvoiced = i;
                    break;
                case "Total Job Cost":
                    totalJobCost = i;
                    break;
                case "Collected Subtotal":
                    totalCollected = i;
                    break;
                case "Date Paid":
                    paidDate = i;
                    break;
                case "Date Invoiced":
                    invoicedDate = i;
                    break;
                case "Date Received":
                    receivedDate = i;
                    break;
                case "Date Closed":
                    closedDate = i;
                    break;
                case "Date of Work Authorization":
                    workAuthDate = i;
                    break;
                case "Division":
                    division = i;
                    break;
                case "Date Started":
                    startedDate = i;
                    break;
                default:
                    System.out.println("Column name " + r.getCell(i).getStringCellValue() + " not found!");
            }
        }
    }

    protected void parseFile(Row curRow) {

        if (curRow != null) {
//            if (curRow.getRowNum() == 164) {
//                System.out.println("Found Brandi");
//            }
            Job job = new Job(getCellVal(curRow, 0), getCellVal(curRow, 1));

            if (lossCat > -1) {
                job.setLossCat(getCellVal(curRow, lossCat));
            }
            if (jobStatus > -1) {
                job.setJobStatus(getCellVal(curRow, jobStatus));
            }

            //if there is something in the Secondary Loss Type field, use that, otherwise just get the loss type
            if (lossType > -1 && lossTypeSec > -1) {
                if (getCellVal(curRow, lossTypeSec) != null) {
                    job.setLossType(getCellVal(curRow, lossTypeSec));
                } else {
                    job.setLossType(getCellVal(curRow, lossType));
                }
            }

            if (referredBy > -1) {
                job.setReferredBy(getCellVal(curRow, referredBy));
            }
            if (referralType > -1) {
                job.setReferralType(getCellVal(curRow, referralType));
            }
            if (division > -1) {
                job.setDivision(getCellVal(curRow, division));
            }

            if (totalEstimates > -1) {
                if (curRow.getCell(totalEstimates).getCellTypeEnum() == CellType.NUMERIC) {
                    job.setTotalEstimates(curRow.getCell(totalEstimates).getNumericCellValue());
                } else {
                    if (curRow.getCell(totalEstimates).getStringCellValue().length() > 1) {
                        job.setTotalEstimates(Double.valueOf(curRow.getCell(totalEstimates).getStringCellValue()));
                    }
                }
            }

            if (totalInvoiced > -1) {
                if (curRow.getCell(totalInvoiced).getCellTypeEnum() == CellType.NUMERIC) {
                    job.setTotalInvoiced(curRow.getCell(totalInvoiced).getNumericCellValue());
                } else {
                    if (curRow.getCell(totalInvoiced).getStringCellValue().length() > 1) {
                        job.setTotalInvoiced(Double.valueOf(curRow.getCell(totalInvoiced).getStringCellValue()));
                    }
                }
            }

            if (totalJobCost > -1) {
                if (curRow.getCell(totalJobCost).getCellTypeEnum() == CellType.NUMERIC) {
                    job.setTotalJobCost(curRow.getCell(totalJobCost).getNumericCellValue());
                } else {
                    if (curRow.getCell(totalJobCost).getStringCellValue().length() > 1) {
                        job.setTotalJobCost(Double.valueOf(curRow.getCell(totalJobCost).getStringCellValue()));
                    }
                }
            }

            if (totalCollected > -1) {
                if (curRow.getCell(totalCollected).getCellTypeEnum() == CellType.NUMERIC) {
                    job.setTotalCollected(curRow.getCell(totalCollected).getNumericCellValue());
                } else {
                    if (curRow.getCell(totalCollected).getStringCellValue().length() > 1) {
                        job.setTotalCollected(Double.valueOf(curRow.getCell(totalCollected).getStringCellValue()));
                    }
                }
            }

            if (paidDate > -1) {
                if (handleDates(curRow.getCell(paidDate)) != null) {
                    job.setPaidDate(handleDates(curRow.getCell(paidDate)));
                }
            }

            if (receivedDate > -1) {
                if (handleDates(curRow.getCell(receivedDate)) != null) {
                    job.setReceivedDate(handleDates(curRow.getCell(receivedDate)));
                }
            }

            if (invoicedDate > -1) {
                if (handleDates(curRow.getCell(invoicedDate)) != null) {
                    job.setInvoicedDate(handleDates(curRow.getCell(invoicedDate)));
                }
            }

            if (closedDate > -1) {
                if (handleDates(curRow.getCell(closedDate)) != null) {
                    job.setClosedDate(handleDates(curRow.getCell(closedDate)));
                }
            }

            if (workAuthDate > -1) {
                if (handleDates(curRow.getCell(workAuthDate)) != null) {
                    job.setWorkAuthDate(handleDates(curRow.getCell(workAuthDate)));
                }
            }

            if (startedDate > -1) {
                if (handleDates(curRow.getCell(startedDate)) != null) {
                    job.setStartedDate(handleDates(curRow.getCell(startedDate)));
                }
            }

            jobList.add(job);
        } else {
            System.out.println("A row passed to the parse method is null");
        }

    }

    protected LocalDate handleDates(Cell cell) {
        DataFormatter d = new DataFormatter();

        String v = d.formatCellValue(cell);
        if (v.length() > 1) {
            return LocalDate.parse(v, formatter);
        } else {
            return null;
        }

    }

    protected String getCellVal(Row row, int index) {
        if (row.getCell(index).getCellTypeEnum() == row.getCell(index).getCellTypeEnum().STRING && row.getCell(index).getStringCellValue().length() > 1) {
            return row.getCell(index).getStringCellValue();
        }
        return null;
    }

    public static ArrayList<Job> getJobList() {
        return jobList;
    }

    public static void setJobList(ArrayList<Job> jobList) {
        DashParser.jobList = jobList;
    }

}
