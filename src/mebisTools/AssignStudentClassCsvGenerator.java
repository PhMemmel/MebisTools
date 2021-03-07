/**
 * @author Philipp Memmel <philipp.memmel@thg.muenchen.musin.de>
 * @version 2020-05-08
 *
 */

package mebisTools;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * This class copies student's class specific files (e. g. timetable) to a user specific file with
 * user specific file name:
 *
 * The target file name scheme is:
 * "FIRSTNAME LASTNAME_1234567_assignsubmission_file_STUDENTFILENAME.pdf"
 *
 * A file having that kind of file name can be put into an zip archive and uploaded into a moodle
 * assignment. The distribution to the students is automatically done considering the user ID (1234567
 * in this example) and the given full name. User ID and full name have to be extracted from separate csv file
 * downloaded in the moodle assignment ("offline grading table"). Unfortunately, this file does not
 * contain any information about the student's class, so this has to be joined from a table downloaded
 * from the often used online platform "Infoportal".
 *
 * The export settings can be found in the screenshot file "ScreenshotInfoportalSchuelerlisteExport.png"
 *
 */

public class AssignStudentClassCsvGenerator {

    File gradingTable = null;
    File studentClassTable = null;
    LinkedList<File> excludedFiles = new LinkedList<>(); // list which contains files to exclude from zipping

    public void start() {

          /*
         * configure FileChooser dialog to choose grading table file
         */
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("W\u00e4hlen Sie die aus Moodle heruntergeladene " +
                "Offline-Bewertungstabelle im csv-Format.");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV-Datei", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
            showMessageDialog(null, "Auswahl notwendig! Programm wird beendet.");
            return;
        } else {
            gradingTable = fileChooser.getSelectedFile();
            if (gradingTable != null) {
                if (!gradingTable.canRead()) {
                    showMessageDialog(null, "Bewertungstabelle nicht existent oder lesbar," +
                            " Programm wird beendet.");
                    return;
                }
            }
        }




        /*
         * configure FileChooser dialog to choose table with student class mapping,
         * Infoportal->Klassen->Widersprüche Datenfreigabe anzeigen (Schule)
         */
        fileChooser.setCurrentDirectory(gradingTable.getAbsoluteFile());
        fileChooser.setDialogTitle("W\u00e4hlen Sie die aus dem Infoportal heruntergeladene " +
                "Sch\u00fcler-Datei im csv-Format.");

        if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
            showMessageDialog(null, "Auswahl notwendig! Programm wird beendet.");
            return;
        } else {
            studentClassTable = fileChooser.getSelectedFile();
            if (studentClassTable != null) {
                if (!studentClassTable.canRead()) {
                    showMessageDialog(null, "Datei \"Widerspr\u00fcche Datenfreigabe\" " +
                            "nicht existent oder lesbar, Programm wird beendet.");
                    return;
                }
            }
        }



        File outputFile = new File(gradingTable.getParent() + File.separator + "output.csv");

        // ignore files for zipping later on
        excludedFiles.add(gradingTable);
        excludedFiles.add(studentClassTable);
        excludedFiles.add(outputFile);

        /*
         * try for each file in the given directory which is supposed to be distributed:
         * - extract class name out of given file name,
         * - generate a list of students in this class out of the studentClassTable file
         * - having the full name of the student extract id from grading table
         * - copy the file to match the student specific file name
         */
        HashSet<String> classes = new HashSet<>();

        try {
            Scanner scanner = new Scanner(studentClassTable);
            do {
                String line = scanner.nextLine();
                classes.add(line.split(";")[2]);
            } while (scanner.hasNextLine());
            scanner.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            classes.stream().sorted().forEach(
                    (className) -> {
                        List<String[]> fullNameList = getFullNameOutOfStudentClassTable(className);
                        for (String[] name : fullNameList) {
                            if (getID(name[0], gradingTable) != "FAIL") {
                                try {
                                    writer.write(name[1] + ";" + className);
                                    writer.newLine();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    }

            );
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO copied from ClassTimetableDistribution, move it to utility class
    /**
     * This method generates a list of students in a specified class
     *
     * @param studentsClass
     * @return list of students in this class
     */
    private List<String[]> getFullNameOutOfStudentClassTable(String studentsClass) {
        LinkedList<String[]> fullNames = new LinkedList<>();

        BufferedReader reader = null;
        try {
            // only possible since java 11
            //reader = new BufferedReader(new FileReader(gradingTable, StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(studentClassTable),
                    StandardCharsets.UTF_8));
            String line = reader.readLine();
            line = reader.readLine(); // skip first row

            while (line != null && !line.isEmpty()) {
                String[] splitLine = line.split(";");

                if(splitLine[2].equals(studentsClass)) {
                    // in case of last names containing multiple words use only words starting with
                    // capital letters (as moodle does)
                    String[] lastNameParts = splitLine[0].split(" ");
                    String lastName = "";

                    /*// remove all trailing and leading spaces in the split up last name:
                    for (int i=0; i<lastNameParts.length;i++) {
                        lastNameParts[i]=lastNameParts[i].trim();
                    }*/

                    int i = lastNameParts.length-1;
                    // starting with last word in last name, go to first word with small letter.
                    // As soon as small letter is detected, ignore it, just use the words with
                    // starting upper letter

                    while (i>=0) {
                        if (lastNameParts[i].substring(0,1).matches("[A-ZÄÖÜ]")) {
                            i--;
                        } else {
                            break;
                        }
                    }
                    // rest has to be last name like used in moodle (all words with capital letters)
                    for (int j=i+1;j<lastNameParts.length;j++) {
                        if (lastName.isEmpty()) {
                            lastName += lastNameParts[j];
                        } else {
                            lastName += " " + lastNameParts[j];
                        }

                    }

                    String fullName = splitLine[1] + " " // first name
                            + lastName; // last part of last name

                    fullNames.add(new String[]{fullName, splitLine[0] + ";" + splitLine[1]});
                }


                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fullNames;
    }

    /**
     * extracts the ID of a student out of the given grading file
     *
     * @param fullName first and last name of the student (search key for ID)
     * @param gradingTable grading table file object to extract the student's id from
     * @return string containing the assignment user id
     */
    private String getID(String fullName, File gradingTable) {
        String returnString = null;
        BufferedReader reader;
        try {
            // only possible since java 11
            //reader = new BufferedReader(new FileReader(gradingTable, StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(gradingTable),
                    StandardCharsets.UTF_8));
            String line = reader.readLine();
            line = reader.readLine(); // skip first row

            while (line != null && !line.isEmpty()) {

                /*
                 * hol dir den Nachnamen aus der aktuell gelesenen Zeile
                 */
                String nameInGradingTable = getFullNameOutOfLine(line);

                if (nameInGradingTable.equals(fullName)) {
                    /*
                     * Erst wird die erste Spalte herausgesplittet, der fuehrende String ist genau
                     * 13 Zeichen lang, naemlich "Teilnehmer/in", die werden abgeschnitten
                     */
                    reader.close();
                    return line.split(",")[0].substring(13);

                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Nutzer " + fullName + " nicht in gradingTable gefunden");
        return "FAIL";
    }


    /**
     * extracts the complete name out of a grading table line
     *
     * @param line the grading table line
     * @return the full name in the form "FIRSTNAME LASTNAME" (without quotes)
     */
    private String getFullNameOutOfLine(String line) {
        String fullName = line.split(",")[1];
        fullName = fullName.substring(1,fullName.length()-1);
        return fullName;
    }
}
