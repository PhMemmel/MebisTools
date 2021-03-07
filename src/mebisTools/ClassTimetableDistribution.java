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
import java.util.LinkedList;
import java.util.List;

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

public class ClassTimetableDistribution {

    File dir = null;
    File gradingTable = null;
    File studentClassTable = null;
    LinkedList<File> excludedFiles = new LinkedList<>(); // list which contains files to exclude from zipping

    public void start() {

        /*
         * configure FileChooser dialog for choosing directory which contains feedback files
         */
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File("."));
        directoryChooser.setDialogTitle("W\u00e4hlen Sie den Ordner, der die Stundenpl\u00e4ne enth\u00e4lt.");
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        if (directoryChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
            showMessageDialog(null, "Auswahl notwendig! Programm wird beendet.");
            return;
        } else {
            dir = directoryChooser.getSelectedFile();
            if (dir != null) {
                if (!dir.canWrite()) {
                    showMessageDialog(null, "Verzeichnis nicht existent oder schreibbar," +
                            " Programm wird beendet.");
                    return;
                }
            }
        }


        /*
         * configure FileChooser dialog to choose grading table file
         */
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(dir);
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
        fileChooser.setCurrentDirectory(dir);
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


        /*
         * open text input dialog to get filename which is shown to the student in the moodle assignment
         */
        String studentFileName = JOptionPane.showInputDialog(null, "Geben Sie den Dateinamen " +
                        "(ohne Endung) der Feedback ein,\n so wie ihn der Sch\u00fcler sp\u00e4ter in seinem moodle-System " +
                        "sehen soll.",
                "Festlegung Feedbackdatei-Anzeigename", JOptionPane.PLAIN_MESSAGE);

        if (studentFileName.isEmpty()) {
            showMessageDialog(null, "Kein Dateiname ausgew\u00e4hlt. Programm wird beendet.");
            return;
        }


        /*
         * open dialog if files should be just renamed or moved to specially named student specific directories
         */
        String[] dirOrSingleFiles = {"Verzeichnis", "Einzeldateien"};
        int dirOrSingleFilesIndex = JOptionPane.showOptionDialog(null, "Bitte w\u00e4hlen, " +
                        "ob f\u00fcr jeden Sch\u00fcler ein separates Verzeichnis\n erstellt werden soll oder die " +
                        "Dateien lediglich umbenannt werden sollen:","Verzeichnis oder Datei?",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                dirOrSingleFiles, dirOrSingleFiles[0]);


        File outputDir = new File(dir.getAbsolutePath() + File.separator + "output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // ignore files for zipping later on
        excludedFiles.add(gradingTable);
        excludedFiles.add(studentClassTable);
        excludedFiles.add(outputDir);

        /*
         * try for each file in the given directory which is supposed to be distributed:
         * - extract class name out of given file name,
         * - generate a list of students in this class out of the studentClassTable file
         * - having the full name of the student extract id from grading table
         * - copy the file to match the student specific file name
         */
        for (File file : dir.listFiles()) {
            if (!excludedFiles.contains(file) && file.isFile()) {
                // ignores original file for zipping later on
                excludedFiles.add(file);
                String[] fileNameParts = file.getName().split("\\."); // split off suffix
                // splits file name by whitespace, assuming last "word" separated by whitespace is last name of student
                String[] fileNameWithoutSuffix = fileNameParts[0].split("_");
                String classOutOfFilename = fileNameWithoutSuffix[fileNameWithoutSuffix.length-1];
                File userDir = null;

                System.out.println("Jetzt kommt Klasse: " + classOutOfFilename);

                List<String> fullNameList = getFullNameOutOfStudentClassTable(classOutOfFilename);

                for (String fullName : fullNameList) {
                    String newFileName = studentFileName + "_" + classOutOfFilename + "." + fileNameParts[1];

                    String id = getID(fullName, gradingTable);
                    if (id.equals("FAIL")) {
                        System.out.println("ID f\u00fcr Sch\u00fcler " + fullName + " konnte " +
                                "nicht ermittelt werden!");
                        break;
                    }


                    switch (dirOrSingleFilesIndex) {
                        case 0 :
                                userDir = new File(outputDir.getAbsolutePath() + File.separator +
                                        fullName + "_" + getID(fullName, gradingTable) +
                                        "_assignsubmission_file_");
                                if (!userDir.exists()) {
                                    userDir.mkdir();
                                }
                                File copiedFile = new File(userDir.getAbsolutePath() + File.separator +
                                        newFileName);
                                try {
                                    Files.copy(file.toPath(), copiedFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    showMessageDialog(null, "Beim Kopieren der Datei " +
                                            copiedFile.getAbsolutePath() + " ist ein Fehler aufgetreten!");
                                    e.printStackTrace();
                                }

                            break;
                        case 1:
                            try {
                                    Files.copy(file.toPath(), new File(outputDir.getAbsolutePath() + File.separator // directory
                                            + fullName + "_" + id +
                                            "_assignsubmission_file_" +
                                            newFileName).toPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            showMessageDialog(null, "Es wurde keine Ausgabemethode ausgew\u00e4hlt.\n" +
                                    "Programm wird beendet.");
                            return;
                    }
                }
            }
        }

        /*
         * create dialog to decide if zip file should be created
         */
        String[] zipOrNot = {"Ja, gerne.", "Nein, mache ich selber."};
        int zipOrNotIndex = JOptionPane.showOptionDialog(null, "Wollen Sie gleich ein " +
                        "moodle-kompatibles zip-Archiv erstellen?","Zip-Datei erstellen?",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                zipOrNot, zipOrNot[0]);

        // if zip file creation is chosen, create zip file in feedback file directory, named "output.zip"
        if (zipOrNotIndex == 0) {
            File zipFile = new File(dir.getAbsolutePath() + File.separator + "output.zip");
            excludedFiles.add(zipFile);
            ZipUtils zipUtils = new ZipUtils(outputDir.getAbsolutePath(), excludedFiles, zipFile.getAbsolutePath());
            zipUtils.generateFileList();
            zipUtils.zipIt();
        }


        showMessageDialog(null, "Alle Dateien abgearbeitet und umbenannt." +
                " Programm wird beendet.");


    }

    /**
     * This method generates a list of students in a specified class
     *
     * @param studentsClass
     * @return list of students in this class
     */
    private List<String> getFullNameOutOfStudentClassTable(String studentsClass) {
        LinkedList<String> fullNames = new LinkedList<>();

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
                    fullNames.add(fullName);
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
        System.out.println("Nutzer " + fullName + "nicht in gradingTable gefunden");
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
