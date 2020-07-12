package mebisTools;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;

public class FragensammlungsGenerator {

    private File moodleDatabaseExportCsv;

    public void start() {
        JOptionPane.showMessageDialog(null, "Import muss im Moodle-XML-Format erfolgen.\n\n"
                + "Weitere Einstellungen: Kategorie und Kontext aus Datei holen sollte nicht aktiviert sein,\nbei " +
                        "\"Bewertungen abgleichen\" soll \"N\u00e4chstliegende Bewertung ausw\u00e4hlen.\" ausgew\u00e4hlt sein.\n\n" +
                        "Der Export aus der mebis-Datenbank muss im CSV-Format stattfinden, komma-separiert.");

        /*
         * configure FileChooser dialog to choose grading table file
         */
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("W\u00e4hle den Moodle-Datenbank-Export (csv-Datei) aus.");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("csv-Dateien", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);


        if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
            showMessageDialog(null, "Auswahl notwendig! Programm wird beendet.");
            return;
        } else {
            moodleDatabaseExportCsv = fileChooser.getSelectedFile();
            if (moodleDatabaseExportCsv != null) {
                if (!moodleDatabaseExportCsv.canRead()) {
                    showMessageDialog(null, "Moodle-Datenbank-Export nicht existent oder lesbar," +
                            " Programm wird beendet.");
                    return;
                }
            }
        }


        List<Question> questionList = new LinkedList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(moodleDatabaseExportCsv),
                    StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String line = reader.readLine();
            String resultFile;
            line = reader.readLine(); // skip first line
            int i=0;
            while (line != null && !line.isEmpty()) {
                String[] lineArray = line.trim().split(",");
                // replaceAll removes leading and trailing "
                Question question = new Question("Frage " + (i+1), lineArray[0].replaceAll("^\"|\"$", ""));


                int countCorrectAnswers = 0;
                int countAnswers = 0;
                for (String column : lineArray) {
                    if (column.equals("Richtig")) {
                        countCorrectAnswers++;
                        countAnswers++;
                    } else if (column.equals("Falsch")) {
                        countAnswers++;
                    }

                }
                double correctPercentage = 100.0/countCorrectAnswers;
                String correctPercentageString = new DecimalFormat("##.#####").format(correctPercentage).replace(",",".");
                double wrongPercentage = 100.0/countAnswers;
                String wrongPercentageString = "-" + new DecimalFormat("##.#####").format(wrongPercentage).replace(",",".");

                for (int j=0; j<lineArray.length; j++) {
                    String answerString = lineArray[j-1].replaceAll("^\"|\"$", "");

                    if (lineArray[j].equals("Richtig")) {
                        question.addAnswer(answerString, correctPercentageString);
                    }
                    if (lineArray[j].equals("Falsch")) {
                        question.addAnswer(answerString, wrongPercentageString);
                    }
                }
                questionList.add(question);

                line = reader.readLine();
                i++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // generate XML file
        generateXML(questionList);

    }

    private void generateXML(List<Question> questions) {
        try {
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // root element
            Element rootElement = doc.createElement("quiz");
            doc.appendChild(rootElement);

            for (Question question : questions) {
                // question
                Element questionElement = doc.createElement("question");
                rootElement.appendChild(questionElement);

                // setting attribute to element
                Attr attr = doc.createAttribute("type");
                attr.setValue("multichoice");
                questionElement.setAttributeNode(attr);

                Element questionName = doc.createElement("name");
                questionElement.appendChild(questionName);

                Element questionNameText = doc.createElement("text");
                questionName.appendChild(questionNameText);
                questionNameText.appendChild(doc.createTextNode(question.getQuestionName()));

                Element questionTextElement = doc.createElement("questiontext");
                questionElement.appendChild(questionTextElement);
                Attr questiontextAttr = doc.createAttribute("format");
                questiontextAttr.setValue("html");
                questionTextElement.setAttributeNode(questiontextAttr);

                Element questionTextText = doc.createElement("text");
                // here the question text is inserted
                questionTextText.appendChild(doc.createCDATASection(question.getQuestionText()));
                questionTextElement.appendChild(questionTextText);

                // general feedback
                Element generalfeedback = doc.createElement("generalfeedback");
                questionElement.appendChild(generalfeedback);
                Attr generalfeedbackAttr = doc.createAttribute("format");
                generalfeedbackAttr.setValue("html");
                generalfeedback.setAttributeNode(generalfeedbackAttr);
                Element generalfeedbackText = doc.createElement("text");
                // enter general feedback here
                generalfeedbackText.appendChild(doc.createTextNode(""));
                generalfeedback.appendChild(generalfeedbackText);

                Element defaultgrade = doc.createElement("defaultgrade");
                // set max points of question to count of possible answers
                defaultgrade.appendChild(doc.createTextNode(Integer.toString(question.getAnswers().size())));
                questionElement.appendChild(defaultgrade);

                // penalty when trying test for the second, third etc. time
                Element penalty = doc.createElement("penalty");
                penalty.appendChild(doc.createTextNode("0.3333333"));
                questionElement.appendChild(penalty);

                // element hidden?
                Element hidden = doc.createElement("hidden");
                hidden.appendChild(doc.createTextNode("0"));
                questionElement.appendChild(hidden);

                // singlechoice or multichoice?
                Element single = doc.createElement("single");
                single.appendChild(doc.createTextNode("false"));
                questionElement.appendChild(single);

                // answer shuffeling
                Element shuffleanswers = doc.createElement("shuffleanswers");
                shuffleanswers.appendChild(doc.createTextNode("true"));
                questionElement.appendChild(shuffleanswers);

                // answer numbering
                Element answernumbering = doc.createElement("answernumbering");
                answernumbering.appendChild(doc.createTextNode("abc"));
                questionElement.appendChild(answernumbering);

                // feedback, if correct
                Element correctfeedback = doc.createElement("correctfeedback");
                questionElement.appendChild(correctfeedback);
                Attr correctfeedbackAttr = doc.createAttribute("format");
                correctfeedbackAttr.setValue("html");
                correctfeedback.setAttributeNode(correctfeedbackAttr);
                Element correctfeedbackText = doc.createElement("text");
                // hier generelles Feedback eintragen
                correctfeedbackText.appendChild(doc.createTextNode(""));
                correctfeedback.appendChild(correctfeedbackText);

                // feedback, if partially correct
                Element partiallycorrectfeedback = doc.createElement("partiallycorrectfeedback");
                questionElement.appendChild(partiallycorrectfeedback);
                Attr partiallycorrectfeedbackAttr = doc.createAttribute("format");
                partiallycorrectfeedbackAttr.setValue("html");
                partiallycorrectfeedback.setAttributeNode(partiallycorrectfeedbackAttr);
                Element partiallycorrectfeedbackText = doc.createElement("text");
                // hier generelles Feedback eintragen
                partiallycorrectfeedbackText.appendChild(doc.createTextNode(""));
                partiallycorrectfeedback.appendChild(partiallycorrectfeedbackText);

                // feedback, if incorrect
                Element incorrectfeedback = doc.createElement("incorrectfeedback");
                questionElement.appendChild(incorrectfeedback);
                Attr incorrectfeedbackAttr = doc.createAttribute("format");
                incorrectfeedbackAttr.setValue("html");
                incorrectfeedback.setAttributeNode(incorrectfeedbackAttr);
                Element incorrectfeedbackText = doc.createElement("text");
                // hier generelles Feedback eintragen
                incorrectfeedbackText.appendChild(doc.createTextNode(""));
                incorrectfeedback.appendChild(incorrectfeedbackText);

                // answer
                for (String answerString : question.getAnswers().keySet()) {
                    Element answer = doc.createElement("answer");
                    Attr answerFractionAttr = doc.createAttribute("fraction");
                    // here the fraction of the answer is set
                    answerFractionAttr.setValue(question.getAnswers().get(answerString));
                    Attr answerFormatAttr = doc.createAttribute("format");
                    answerFormatAttr.setValue("html");
                    answer.setAttributeNode(answerFractionAttr);
                    answer.setAttributeNode(answerFormatAttr);

                    Element answerTextElement = doc.createElement("text");
                    answer.appendChild(answerTextElement);
                    // add the real answer string
                    answerTextElement.appendChild(doc.createTextNode(answerString));

                    // feedback (at the moment not considered at all)
                    Element answerFeedback = doc.createElement("feedback");
                    Attr answerFeedbackFormatAttr = doc.createAttribute("format");
                    answerFeedbackFormatAttr.setValue("html");
                    answerFeedback.setAttributeNode(answerFeedbackFormatAttr);
                    answer.appendChild(answerFeedback);
                    Element answerFeedbackText = doc.createElement("text");
                    answerFeedbackText.appendChild(doc.createTextNode(""));
                    answerFeedback.appendChild(answerFeedbackText);


                    questionElement.appendChild(answer);
                }


            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(moodleDatabaseExportCsv.getParent() + File.separator
                    + "FragensammlungsOutput.xml"));

            transformer.transform(source, result);

            // Output to console for testing
//            StreamResult consoleResult = new StreamResult(System.out);
//            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
