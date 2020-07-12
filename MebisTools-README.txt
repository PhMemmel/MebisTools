Kurze Anleitung, wie die mebis-Tools zu benutzen sind:

Aktuell enthalten die MebisTools drei Teil-Tools:

Tool 1: "Schülerzuordnung":
Benennt Dateien, die den Nachnamen eines Schülers enthalten, mit Hilfe einer heruntergeladenen Offline-Bewertungstabelle der jeweiligen mebis-Aufgabe so um, dass man sie als zip-Archiv in die Aufgabe hochladen kann. Die Dateien werden dann dem jeweiligen Schüler als Feedbackdatei zugeordnet.

Tool 2: "Klassenplanverteiler":
Verteilt Stundenplandateien. Dazu muss aus dem Infoportal der Schule eine Schülerdatei exportiert werden (csv-Datei, Semikolon-getrennt mit den Feldern Name, Vorname, Klasse). Außerdem benötigt man wiederum die Offline-Bewertungstabelle der jeweiligen mebis-Aufgabe. Das Tool sucht zu jeder Datei aus dem angegebenen Ordner, die mit der Bezeichnung einer Klasse endet (z. B. StundenplanKlasse5a.pdf) aus der Infoportal-Liste alle Schüler der Klasse, dupliziert die Datei für jeden einzelnen Schüler und benennt sie so um, dass man sie wiederum in die entsprechende mebis-Aufgabe hochladen kann.

Tool 3: "Fragensammlungs-Generator":
Dieses Tool konvertiert den csv-Export einer mebis-Datenbank in eine Moodle-XML-Datei, die dann in eine Fragensammlung importiert werden kann.
Idee dahinter: Schüler erstellen selbst einen Multiple-Choice-Test. Hierzu tragen die Schüler im Unterricht/zu Hause ihre Fragen für den Test in eine mebis-Datenbank mitsamt der passenden Antworten dazu ein, die sie jeweils auch als "Richtig" oder "Falsch" deklarieren.
Das Schema der Datenbank sollte lauten: "Fragentext", "Antwortmöglichkeit 1", "[Richtig|Falsch]", "Antwortmöglichkeit 2", "[Richtig|Falsch]", ...
Hierbei muss jeweils das exakte Wort "Richtig" bzw. "Falsch" angegeben sein (Auswahlbutton im jeweiligen Datenbank-Feld).
Die XML-Datei wird im selben Verzeichnis wie die Quelldatei unter dem Namen "FragensammlungsOutput.xml" abgelegt. Sie folgt folgenden Vorgaben:
https://docs.moodle.org/39/de/Moodle_XML-Format


Etwas konkreter für das Tool "Schülerzuordnung":

So geht's:

1. Die Bewertungstabelle muss heruntergeladen werden: Hierzu mebis-Aufgabe anlegen, bei Feedback-Optionen "Offline-Bewertungstabelle" auswählen und dann im DropDown-Menü auf der Bewertungsübersicht "Bewertungstabelle herunterladen". Aus dieser Tabelle extrahiert das Programm die jeweilige Abgabe-ID des Users sowie dessen vollen Namen.

2. Die Feedbackdateien, die umbenannt werden sollen, müssen in einem gemeinsamen Ordner liegen. Die Dateinamen der Feedbackdateien müssen den Nachnamen des jeweiligen Schülers enthalten und zwar so, dass dieser das letztes "Wort" vor der Dateiendung ist, also beispielsweise "Feedbackdatei fuer Hans Mueller.pdf". Auch "Mueller.pdf" funktioniert, es muss lediglich sichergestellt sein, dass das letzte durch ein Leerzeichen getrennte Wort der Nachname ist.

3. Ausführen des Programms: Doppelklick auf die jar-Datei: MebisToolsFilenameConverter.jar
Der Reihe nach wird nun abgefragt:
- Tool 1 oder Tool 2
- Das Verzeichnis mit den Feedbackdateien
- Die Bewertungstabellendatei
- Der gewünschte Dateiname der jeweiligen Feedback-Datei, so wie sie dann später bei den Schülern im Moodle angezeigt werden soll.
- Sollen schülerspezifische Verzeichnisse erstellt werden (dann könnte man in die entstehende Verzeichnisstruktur noch weitere Dateien exportieren, die mit verteilt werden) oder die Dateien nur entsprechend umbenannt werden.
- Soll das zip-Archiv direkt mit erstellt werden? Falls ja, wird im Feedbackdateien-Ordner ein moodle-kompatibles zip-Archiv gleich mit erstellt.

FERTIG!


Etwas konkreter für das Tool "Klassenplanverteiler":

Vorgehensweise analog zur "Schülerzuordnung", nach der Bewertungstabelle wird jedoch noch die aus dem Infoportal heruntergeladene Datei abgefragt (csv-Datei mit Schema "Name;Vorname;Klasse" ohne Hochkommata zur Zellentrennung).


VORSICHT!
Dieses Programm wurde bisher nicht ausführlich getestet. Bei Problemen bitte kurze Nachricht an unten genannte Mail.

Ein Sonderfall bei der Schülerzuordnung konnte bisher noch nicht berücksichtigt werden: Sind zwei Schüler mit identischem Nachnamen dabei, werden beide ignoriert, da dann keine eindeutige Zuordnung stattfinden kann. Es wird eine entsprechende Fehlermeldung ausgegeben. Hier muss man manuell eingreifen. Die ID erhält man aus der ersten Spalte der Bewertungstabelle.

Übrigens: Das Namensschema der von Untis exportierten Studentenpläne passt "zufällig" genau mit den Anforderungen des Programms an die umzubenennenden Feedbackdateien zusammen ;-)

Ich wünsche viel Erfolg.

Philipp Memmel
philipp.memmel@thg.muenchen.musin.de