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

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * This class renames all given files in the given directory
 *
 * Renaming is neccessary in order to get the following file name scheme:
 * "FIRSTNAME LASTNAME_1234567_assignsubmission_file_STUDENTFILENAME.pdf"
 *
 * A file having that kind of file name can be put into an zip archive and uploaded into a moodle
 * assignment. The distribution to the students is automatically done considering the user ID (1234567)
 * in this example and the given full name. User ID and full name have to be extracted from separate csv file
 * downloaded in the moodle assignment ("offline grading table").
 *
 */

public class FilenameConverter {

    public static void main(String[] args) {

        File dir = null;
        LinkedList<File> excludedFiles = new LinkedList<>(); // list which contains files to exclude from zipping

        /*
         * configure FileChooser dialog for choosing directory which contains feedback files
         */
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File("."));
        directoryChooser.setDialogTitle("W\u00e4hlen Sie den Ordner, der die Feedbackdateien enth\u00e4lt.");
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
        File gradingTable = null;
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

        /*
         * try for each file in the given directory:
         * extract last name of student out of given file name, join that name with the ID and the full name
         * in the matching line of grading file
         * then rename the file or create directory and move file into it
         */
        for (File file : dir.listFiles()) {
            if (!file.getName().equals(gradingTable.getName()) && file.isFile()) {
                String[] fileNameParts = file.getName().split("\\."); // split off suffix
                // splits file name by whitespace, assuming last "word" separated by whitespace is last name of student
                String[] fileNameWithoutSuffix = fileNameParts[0].split(" ");
                String surname = fileNameWithoutSuffix[fileNameWithoutSuffix.length-1];
                File userDir = null;
                if (getIdAndFullName(surname, gradingTable).equals("FAIL")) { // checks if there is doubled last name
                    excludedFiles.add(file);
                } else {
                    switch (dirOrSingleFilesIndex) {
                        case 0 :
                            userDir = new File(dir.getAbsolutePath() + File.separator +
                                    getIdAndFullName(surname, gradingTable) +
                                    "_assignsubmission_file_");
                            if (!userDir.exists()) {
                                userDir.mkdir();
                            }
                            File movedFile = new File(userDir.getAbsolutePath() + File.separator +
                                    studentFileName + "." + fileNameParts[1]);
                            try {
                                Files.move(file.toPath(), movedFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                showMessageDialog(null, "Beim Verschieben der Datei " +
                                        movedFile.getAbsolutePath() + " ist ein Fehler aufgetreten!");
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                            file.renameTo(new File(dir.getAbsolutePath() + File.separator // directory
                                + getIdAndFullName(surname, gradingTable) +
                                "_assignsubmission_file_" +
                                studentFileName + // file name shown to student after uploading into moodle
                                "."
                                + fileNameParts[1])); // add previously split off suffix again
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
            excludedFiles.add(gradingTable);
            excludedFiles.add(zipFile);
            ZipUtils zipUtils = new ZipUtils(dir.getAbsolutePath(), excludedFiles, zipFile.getAbsolutePath());
            zipUtils.generateFileList();
            zipUtils.zipIt();
        }




        showMessageDialog(null, "Alle Dateien abgearbeitet und umbenannt." +
                " Programm wird beendet.");


    }

    /**
     * extracts the ID and the full name of a student out of the given grading file
     *
     * @param lastName extracted last name of the student out of the given file name
     * @param gradingTable grading table file object to look for matching student (id and full name)
     * @return string containing the assignment user id as well as the student's full name
     */
    private static String getIdAndFullName(String lastName, File gradingTable) {
        String returnString = null;
        BufferedReader reader;
        try {
            // only possible since java 11
            //reader = new BufferedReader(new FileReader(gradingTable, StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(gradingTable),
                    StandardCharsets.UTF_8));
            String line = reader.readLine();
            line = reader.readLine(); // skip first row

            // check if there is duplicate surname
            int count=0;
            while (line != null && !line.isEmpty()) {


                if (getNameOutOfLineString(line).equals(lastName)) {
                    count++;
                }

                line = reader.readLine();
            }


            if (count>1) {
                showMessageDialog(null, "Vorsicht: Doppelter Nachname \"" + lastName
                        + "\"!\n\nBitte in Dateinamen und Offline-Tabelle eindeutigen Nachnamen verwenden!\n" +
                        "Datei zu dem Nachnamen wird ignoriert.");
                reader.close();
                return "FAIL";
            }

            reader.close();
            // reinitialize reader
            // only possible since java 11
            //reader = new BufferedReader(new FileReader(gradingTable, StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(gradingTable),
                    StandardCharsets.UTF_8));


            line = reader.readLine();
            line = reader.readLine(); // skip first line
            while (line != null && !line.isEmpty()) {

                /*
                 * hol dir den Nachnamen aus der aktuell gelesenen Zeile
                 */
                String nameInGradingTable = getNameOutOfLineString(line);


                if (nameInGradingTable.equals(lastName)) {
                    /*
                     * Erst wird die erste Spalte herausgesplittet, der fuehrende String ist genau
                     * 13 Zeichen lang, naemlich "Teilnehmer/in", die werden abgeschnitten
                     */

                    String id = line.split(",")[0].substring(13);
                    returnString = getFullNameOutOfLine(line) + "_" + id;
                    break;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    /**
     * extracts the last name out of a single line of the given grading file
     * @param line single line of the grading file
     * @return the last name of the student
     */
    private static String getNameOutOfLineString(String line) {
        String nameInGradingTable = getFullNameOutOfLine(line);
        String[] singleNames = nameInGradingTable.split(" ");
        nameInGradingTable = singleNames[singleNames.length-1];
        return nameInGradingTable;
    }

    /**
     * extracts the complete name out of a grading table line
     *
     * @param line the grading table line
     * @return the full name in the form "FIRSTNAME LASTNAME" (without quotes)
     */
    private static String getFullNameOutOfLine(String line) {
        String fullName = line.split(",")[1];
        fullName = fullName.substring(1,fullName.length()-1);
        return fullName;
    }
}
