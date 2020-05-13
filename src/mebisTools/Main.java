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
        JButton klassenStundenplanVerteiler = new JButton("Klassenstundenplanverteiler");
        schuelerZuordnungButton.setMargin(new Insets(5, 5, 5, 5));
        klassenStundenplanVerteiler.setMargin(new Insets(5, 5, 5, 5));
        schuelerZuordnungButton.setSize(50, 20);
        klassenStundenplanVerteiler.setSize(50, 20);


        schuelerZuordnungButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == schuelerZuordnungButton){
                    new FilenameConverter().start();
                }
            }
        });

        klassenStundenplanVerteiler.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == klassenStundenplanVerteiler){
                    new ClassTimetableDistribution().start();
                }
            }
        });

//        schuelerZuordnungButton.setSize(100 , 30);
//        klassenStundenplanVerteiler.setSize(100 , 30);


        JTextArea jTextArea = new JTextArea();
        jTextArea.setText("Bitte w\u00e4hlen, " +
                "welcher Modus gew\u00fcnscht wird.\n\n" +
                "Sollen nach Sch\u00fclern benannte Dateien distribuiert werden oder sollen " +
                "Klassenstundenpl\u00e4ne verteilt werden?");


        JFrame jFrame = new JFrame("Mebis-Tools");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(3,1, 20, 20);
        jPanel.setLayout(gridLayout);
        jPanel.setBorder(new EmptyBorder(5,5,5,5));

        jPanel.add(jTextArea);
        jPanel.add(schuelerZuordnungButton);
        jPanel.add(klassenStundenplanVerteiler);

        jFrame.getContentPane().setLayout(new BorderLayout());
        jFrame.getContentPane().add(jPanel);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        }
}
