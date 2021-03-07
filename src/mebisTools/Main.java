package mebisTools;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });


        return;
    }

    private static void createAndShowGUI() {
        JButton schuelerZuordnungButton = new JButton("Sch\u00fclerzuordnung");
        schuelerZuordnungButton.setMargin(new Insets(5, 5, 5, 5));
        schuelerZuordnungButton.setSize(50, 20);
        schuelerZuordnungButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == schuelerZuordnungButton){
                    new FilenameConverter().start();
                }
            }
        });

        JButton klassenStundenplanVerteiler = new JButton("Klassenstundenplanverteiler");
        klassenStundenplanVerteiler.setMargin(new Insets(5, 5, 5, 5));
        klassenStundenplanVerteiler.setSize(50, 20);
        klassenStundenplanVerteiler.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == klassenStundenplanVerteiler){
                    new ClassTimetableDistribution().start();
                }
            }
        });

        JButton assignStudentClassCsvGenerator = new JButton("CSV Klassen mit eingeschriebenen Schülern");
        assignStudentClassCsvGenerator.setMargin(new Insets(5, 5, 5, 5));
        assignStudentClassCsvGenerator.setSize(50, 20);
        assignStudentClassCsvGenerator.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == assignStudentClassCsvGenerator){
                    new AssignStudentClassCsvGenerator().start();
                }
            }
        });

        JButton studentenStundenplanVerteiler = new JButton("Studentenstundenplanverteiler");
        studentenStundenplanVerteiler.setMargin(new Insets(5, 5, 5, 5));
        studentenStundenplanVerteiler.setSize(50, 20);
        studentenStundenplanVerteiler.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == studentenStundenplanVerteiler){
                    new StudentTimetableDistribution().start();
                }
            }
        });

        JButton fragensammlungsGenerator = new JButton(("Fragensammlungs-Generator"));
        fragensammlungsGenerator.setMargin(new Insets(5,5,5,5));
        fragensammlungsGenerator.setSize(50,20);
        fragensammlungsGenerator.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == fragensammlungsGenerator){
                    new FragensammlungsGenerator().start();
                }
            }
        });


        JTextArea jTextArea = new JTextArea();
        jTextArea.setText("Bitte w\u00e4hlen, " +
                "welches Tool gew\u00fcnscht wird.");


        JFrame jFrame = new JFrame("Mebis-Tools by Philipp Memmel");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(6,1, 20, 20);
        jPanel.setLayout(gridLayout);
        jPanel.setBorder(new EmptyBorder(5,5,5,5));

        jPanel.add(jTextArea);
        jPanel.add(schuelerZuordnungButton);
        jPanel.add(studentenStundenplanVerteiler);
        jPanel.add(assignStudentClassCsvGenerator);
        jPanel.add(klassenStundenplanVerteiler);
        jPanel.add(fragensammlungsGenerator);

        jFrame.getContentPane().setLayout(new BorderLayout());
        jFrame.getContentPane().add(jPanel);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        }
}
