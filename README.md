# Tagescode Android

Die App zeigt den heutigen sechsstelligen Tagescode an und stellt ihn zusätzlich direkt auf dem Android-Startbildschirm in einem Widget dar.

## Funktionen

* Widget zeigt den heutigen sechsstelligen Tagescode.
* Widget zeigt zusätzlich das aktuelle Datum.
* Tippen auf das Widget öffnet die App.
* In der App wird zuerst der heutige Code angezeigt.
* Über **Anderes Datum** kann ein Tagescode für ein anderes Datum gesucht werden.
* Über **Heute** kann wieder direkt zum aktuellen Tagescode gewechselt werden.
* Die manuelle Auswahl wird nicht gespeichert. Nach dem Verlassen oder erneuten Öffnen der App wird wieder der heutige Code angezeigt.
* Das Widget bleibt immer beim heutigen Tagescode.
* Automatische Aktualisierung des Widgets beim Tageswechsel.
* Zusätzliche Aktualisierung bei Zeitänderung, Zeitzonenänderung und Neustart.
* Automatischer Hell- und Dunkelmodus.
* Unterschiedliches Videojet-Logo für Hell- und Dunkelmodus.
* Eigenes Tagescode-App-Icon.
* Bereits gespeicherte Codes funktionieren offline; neue OneDrive-Codes werden bei verfügbarem Dateizugriff importiert.
* Unten stehen vier Direktbuttons für **Uhr**, **Prüfen**, **TXT** und **Info**.
* Eine Statusanzeige zeigt sofort, ob alles i.O. ist. Grün bedeutet: Datei erfolgreich geprüft, neue Codes falls nötig importiert und heutiger Code vorhanden.

## Weitere Tagescodes

In der App werden zusätzlich folgende Tagescodes angezeigt:

* Code von gestern
* 01.01.2000
* 01.01.2001
* 01.01.2006

## APK mit GitHub Actions erstellen

1. Den gesamten Inhalt dieses Ordners in die oberste Ebene eines GitHub-Repositories hochladen.
2. In GitHub oben **Actions** öffnen.
3. Den Workflow **Android APK erstellen** auswählen.
4. Nach erfolgreichem Build unten den Artifact-Download **Tagescode-APK** herunterladen.
5. Die ZIP entpacken und die APK mit dem Namen `Tagescode_V<Versionsnummer>.apk` auf dem Android-Handy installieren (zum Beispiel `Tagescode_V1.3.106.apk`).

Die GitHub Action verwendet eine online installierte Gradle-Version. Ein lokaler Gradle Wrapper ist deshalb für den Online-Build nicht erforderlich.

## Eine zentrale Tagescodes.txt

Beim ersten Start die von Power Automate befüllte `Tagescodes.txt` im Android-Dateiauswahldialog auswählen. Dazu muss OneDrive als Dateianbieter verfügbar und angemeldet sein. Die App speichert die Leseberechtigung dauerhaft.

Beim Öffnen der App und beim täglichen Widget-Update wird die Datei im Hintergrund eingelesen. Neue Datumswerte werden im privaten App-Speicher gesichert. Vorhandene Codes werden nicht überschrieben, bei Duplikaten gewinnt der erste gültige Eintrag. Führende Nullen bleiben erhalten. Grusszeilen und ungültige Daten werden ignoriert. Bei einem abgebrochenen Lesevorgang wird kein Teilimport gespeichert. Die lokale Datei wird atomar ersetzt.

Unterstützte Formate sind `MM/DD/YYYY`, `DD.MM.YYYY`, `YYYY-MM-DD` und `DD-MM-YYYY`, jeweils gefolgt von sechs Ziffern. Schrägstriche bedeuten immer Monat/Tag/Jahr, passend zur Power-Automate-Datei. Beispiel: `10/02/2026 000416` ist der 2. Oktober.

Zum Wechseln der Datei oder erneuten Erteilen der Berechtigung unten auf **TXT** tippen. Die App importiert ausschliesslich die gewählte `Tagescodes.txt`. Der frühere PwD-Ordner wird nicht mehr gelesen. Bereits gespeicherte Codes bleiben erhalten. Nach einer Neuinstallation muss die zentrale Datei einmal ausgewählt und erfolgreich importiert werden.

Bei fehlendem Internet, abgemeldetem OneDrive oder entzogenem Zugriff bleiben lokal gespeicherte Codes erhalten. In der App erscheint bei fehlgeschlagenem Cloud-Import ein Hinweis. Android und der Dateianbieter bestimmen, wann aktuelle Cloud-Inhalte verfügbar sind; die App kann keine sofortige OneDrive-Synchronisation erzwingen.

## Widget-Aktualisierung

Das Widget zeigt beim Tageswechsel sofort den lokal bekannten Code. Der Alarm kurz nach Mitternacht plant zusätzlich einen Android-Hintergrundjob zum Import ein. Nach dem Import wird das Widget erneut aktualisiert. Fehlgeschlagene Cloud-Zugriffe werden mit zeitlichem Abstand erneut versucht. Android darf Alarm und Hintergrundjob im Energiesparmodus verzögern.

## Tests

`gradle :app:testDebugUnitTest :app:assembleDebug`

Die Importtests prüfen Datumsformate, führende Nullen, Duplikate, ungültige Einträge und Lesefehler. Auf einem Android-Gerät zusätzlich Dateiauswahl, dauerhafte Freigabe nach Neustart, Offline-Anzeige und täglichen Widget-Import mit dem verwendeten OneDrive-Anbieter prüfen.

## Bedienleiste und Status

Die untere Bedienleiste enthält:

* **Uhr** – Tagescodes direkt an die ausgewählte Garmin-Uhr senden.
* **Prüfen** – die gewählte `Tagescodes.txt` sofort prüfen und nur neue Datumswerte importieren.
* **TXT** – eine andere `Tagescodes.txt` auswählen.
* **Info** – Version, Build, gewählte Datei, letzte Prüfung, letzte erfolgreiche Prüfung, letzten Import neuer Codes, Anzahl neuer Codes, verfügbares Enddatum und Garmin-Übertragungsstatus anzeigen.

Die Statusanzeige verwendet drei Zustände:

* **Grün** – alles i.O.; Datei erfolgreich geprüft und heutiger Tagescode vorhanden.
* **Gelb** – Prüfung läuft, Datei fehlt oder es wurde noch keine erfolgreiche Prüfung durchgeführt.
* **Rot** – Prüfung fehlgeschlagen oder der heutige Tagescode fehlt.

## Version

Aktuelle App-Version: **1.6.0**

Die App zeigt weiterhin Versionsnummer sowie Monat und Jahr des Builds an.

## Kalender und Sperrbildschirm

Im Kalender sind ausschliesslich Daten mit gespeichertem Tagescode auswählbar. Fehlende Tage bleiben ausgegraut; die Monatsnavigation überspringt Monate ohne Codes. Über die Monatsüberschrift lassen sich Monat und Jahr direkt wählen. Neue Importe sind beim nächsten Öffnen des Kalenders verfügbar.

Das Widget deklariert Unterstützung für Start- und Sperrbildschirm. Auf unterstützten Geräten: Sperrbildschirm lange drücken, Widgets öffnen und Tagescode auswählen. Ob ein Drittanbieter-Widget angeboten wird, bestimmt der jeweilige Android-/Samsung-Widget-Host; die App kann diese Auswahl nicht erzwingen. Die Sperrbildschirm-Anzeige muss auf dem Zielgerät geprüft werden.

## Interaktive Browser-Vorschau

Mit `python preview/build_preview.py preview/index.html "PFAD/ZU/Tagescodes.txt"` wird `preview/index.html` aus der zentralen Datei erzeugt und kann im Browser geöffnet werden. Die Vorschau bettet alle gültigen Codes der angegebenen TXT-Datei ein und simuliert Kalender, Anzeige, Hell-/Dunkelmodus und TXT-Import. Importe bleiben nur im Arbeitsspeicher des Browsers. Sie ersetzt keinen Android-Test für OneDrive-Berechtigungen, Hintergrundjobs oder echte Widgets. Nach Änderungen an Layout oder Logik die Vorschau entsprechend mitpflegen.
