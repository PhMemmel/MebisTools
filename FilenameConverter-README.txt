Kurze Anleitung, wie der FilenameConverter zu benutzen ist:

So geht's:

1. Die Bewertungstabelle muss heruntergeladen werden: Hierzu mebis-Aufgabe anlegen, bei Feedback-Optionen "Offline-Bewertungstabelle" auswählen und dann im DropDown-Menü auf der Bewertungsübersicht "Bewertungstabelle herunterladen". Aus dieser Tabelle extrahiert das Programm die jeweilige Abgabe-ID des Users sowie dessen vollen Namen.

2. Die Feedbackdateien, die umbenannt werden sollen, müssen in einem gemeinsamen Ordner liegen. Die Dateinamen der Feedbackdateien müssen den Nachnamen des jeweiligen Schülers enthalten und zwar so, dass dieser das letztes "Wort" vor der Dateiendung ist, also beispielsweise "Feedbackdatei fuer Hans Mueller.pdf". Auch "Mueller.pdf" funktioniert, es muss lediglich sichergestellt sein, dass das letzte durch ein Leerzeichen getrennte Wort der Nachname ist.

3. Ausführen des Programms: Doppelklick auf die jar-Datei: MebisToolsFilenameConverter.jar
Der Reihe nach wird nun abgefragt:
- Das Verzeichnis mit den Feedbackdateien
- Die Bewertungstabellendatei
- Der gewünschte Dateiname der jeweiligen Feedback-Datei, so wie sie dann später bei den Schülern im Moodle angezeigt werden soll.
- Soll das zip-Archiv direkt mit erstellt werden? Falls ja, wird im Feedbackdateien-Ordner ein moodle-kompatibles zip-Archiv gleich mit erstellt.

FERTIG!



VORSICHT!
Dieses Programm wurde bisher nicht ausführlich getestet. Bei Problemen bitte kurze Nachricht an unten genannte Mail.

Ein Sonderfall konnte auch nicht berücksichtigt werden: Sind zwei Schüler mit identischem Nachnamen dabei, werden beide ignoriert, da dann keine eindeutige Zuordnung stattfinden kann. Es wird eine entsprechende Fehlermeldung ausgegeben. Hier muss man manuell eingreifen. Die ID erhält man aus der ersten Spalte der Bewertungstabelle.

Übrigens: Das Namensschema der von Untis exportierten Studentenpläne passt "zufällig" genau mit den Anforderungen des Programms an die umzubenennenden Feedbackdateien zusammen ;-)

Ich wünsche viel Erfolg.

Philipp Memmel
philipp.memmel@thg.muenchen.musin.de