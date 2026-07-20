# Tagescode Android

Die App zeigt auf dem Android-Startbildschirm in einem Widget ausschliesslich den heutigen sechsstelligen Tagescode an.

## Funktionen

- Widget zeigt nur die Zahlen des heutigen Tagescodes.
- Tippen auf das Widget öffnet die App.
- In der App wird zuerst der heutige Code angezeigt.
- Über **Anderes Datum** kann ein Tagescode für ein anderes Datum gesucht werden.
- Die manuelle Auswahl wird nicht gespeichert. Nach dem Verlassen oder erneuten Öffnen der App wird wieder der heutige Code angezeigt.
- Das Widget bleibt immer beim heutigen Tagescode.
- Automatische Aktualisierung bei Datumswechsel, Zeitänderung, Zeitzonenänderung und Neustart.
- Automatischer Hell- und Dunkelmodus.
- Komplett offline.

## APK mit GitHub Actions erstellen

1. Den gesamten Inhalt dieses Ordners in die oberste Ebene eines GitHub-Repositories hochladen.
2. In GitHub oben **Actions** öffnen.
3. Den Workflow **Android APK erstellen** auswählen.
4. Nach erfolgreichem Build unten den Artifact-Download **Tagescode-APK** herunterladen.
5. Die ZIP entpacken und `app-debug.apk` auf dem Android-Handy installieren.

Die GitHub Action verwendet eine online installierte Gradle-Version. Ein lokaler Gradle Wrapper ist deshalb für den Online-Build nicht erforderlich.

## Codeliste aktualisieren

Datei ersetzen:

`app/src/main/assets/tagescodes.txt`

Format pro Eintrag:

`MM/DD/YYYY 123456`
