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
* Komplett offline.

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
5. Die ZIP entpacken und `app-debug.apk` auf dem Android-Handy installieren.

Die GitHub Action verwendet eine online installierte Gradle-Version. Ein lokaler Gradle Wrapper ist deshalb für den Online-Build nicht erforderlich.

## Codeliste aktualisieren

Die integrierte Codeliste befindet sich hier:

`app/src/main/assets/tagescodes.txt`

Format pro Eintrag:

`MM/DD/YYYY 123456`

Beispiel:

`08/26/2026 123456`

Der Tagescode muss immer aus sechs Ziffern bestehen.

Auch führende Nullen müssen erhalten bleiben.

Beispiel:

`07/16/2026 000416`

## Externe Codeliste

Zusätzlich kann die App eine externe Datei `PwD.txt` verwenden.

Der Ordner wird beim ersten Start einmalig ausgewählt.

Anschliessend merkt sich die App den Zugriff auf diesen Ordner.

Wird die Datei `PwD.txt` später durch eine neue Version ersetzt, muss der Ordner nicht erneut ausgewählt werden.

## Widget-Aktualisierung

Der Tagescode eines bestimmten Datums ändert sich nie.

Beim Tageswechsel muss das Widget deshalb lediglich den Code des neuen Datums anzeigen.

Das Widget wird automatisch beim Datumswechsel aktualisiert.

Zusätzlich wird kurz nach Mitternacht eine weitere Aktualisierung ausgelöst, damit der neue Tagescode zuverlässig angezeigt wird.

## Version

Die App zeigt im unteren Bereich die aktuelle Versionsnummer sowie Monat und Jahr des Builds an.

Beispiel:

`Version 1.3.86 · August 2026`
