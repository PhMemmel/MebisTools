package mebisTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private List<String> fileList = null;
    private List<File> excludedFiles = null;
    private String outputZipFileString = null;
    private String dirToZipString = null;

    public ZipUtils(String dirToZipString, List<File> excludedFiles, String outputZipFileString) {
        this.excludedFiles = excludedFiles;
        if (excludedFiles == null) {
            this.excludedFiles = new LinkedList<>();
        }
        fileList = new ArrayList<String>();
        this.dirToZipString = dirToZipString;
        this.outputZipFileString = outputZipFileString;

    }

    public void zipIt() {
        byte[] buffer = new byte[1024];
        String source = new File(outputZipFileString).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(outputZipFileString);
            zos = new ZipOutputStream(fos);

//            System.out.println("Output to Zip : " + outputZipFileString);
            FileInputStream in = null;

            for (String file : this.fileList) {
//                System.out.println("File Added : " + file);
                //ZipEntry ze = new ZipEntry(source + File.separator + file);
                // das wuerde noch einmal einen Unterordner im Zip-Archiv machen -> nicht gewollt
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(dirToZipString + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();
//            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFileList() {
        for (File userDir : new File(dirToZipString).listFiles()) {
            generateFileList(userDir);
        }

    }

    private void generateFileList(File node) {
        // add file only
        if (node.isFile()) {
            if (!excludedFiles.contains(node)) {
                fileList.add(generateZipEntry(node.toString()));
            }
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(dirToZipString.length() + 1, file.length());
    }
}