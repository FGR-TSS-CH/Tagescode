# Tagescode für fēnix 8 AMOLED (47/51 mm)

Diese Connect-IQ-Erweiterung stellt den aktuellen Tagescode als öffentliche
**Complication „Tagescode“** bereit. Ein kompatibles Zifferblatt kann diesen
Wert in einem seiner Datenfelder anzeigen. Das bestehende Zifferblatt wird
nicht verändert oder ersetzt. Optional zeigt die Erweiterung denselben Wert
beim direkten Öffnen und als Glance an.

## Status

Gerätebuild mit SDK 9.2.0 für `fenix847mm` erfolgreich. Paketübernahme,
führende Nullen, ungültige Pakete und fehlende Tagescodes im Simulator geprüft.
Die Integration mit dem installierten Zifferblatt
„Fenix7 Pro Analog MB“ und die Bluetooth-Übertragung müssen auf der Uhr
getestet werden. Die Herstellerseite nennt Unterstützung für eigene
Complications, garantiert jedoch nicht jede installierte Version oder
Formatierung. Insbesondere sechsstellige Werte und führende Nullen prüfen.

## Daten und Bedienung

- Android: unten auf die Versionsnummer → **Garmin-Uhr → Uhr auswählen**.
- Garmin Connect muss auf dem Handy installiert und mit der Uhr gekoppelt sein.
- Die Garmin-Erweiterung muss separat auf der Uhr installiert sein.
- Nach der Installation einmal auf der Uhr öffnen, damit die
  Hintergrundübernahme und die Complication aktiviert werden.
- Nach der Dateiprüfung in der geöffneten Android-App werden die gespeicherten
  Codes automatisch zur ausgewählten, verbundenen Uhr übertragen.
- **Codes jetzt übertragen** wiederholt die Übertragung bei Bedarf.
- Ein Paket enthält gestern, heute und vorhandene Codes für die nächsten
  31 Tage. Historische Referenzcodes werden nicht übertragen.
- Die Uhr speichert die Liste lokal; sie wählt anhand ihres eigenen lokalen
  Datums den heutigen Code. Fehlende Tage ergeben „Kein Code“.
- Android 1.5.1 imports the selected TXT and sends codes in the background.
  Open the updated phone app once; a selected watch, persisted file access and
  Garmin Connect running in the background are required.
- First attempt: after at least 15 minutes. After success: 24 hours. After
  import/connection failure: retry after at least 30 minutes. Android power
  saving may delay jobs. Reopen the app after force-stop.
- Watch app 1.0.1 closes the full-screen code view automatically after 10 seconds.
  Configure a button shortcut on the watch if Garmin offers this app as a target.
  No double-tap binding is added to third-party watch faces.
- Manual transfer was confirmed on the user's watch. Background delivery and
  timed return still require physical device acceptance testing.
- Die Garmin-Hintergrundaufgabe aktualisiert die Complication im beantragten
  Fünf-Minuten-Intervall. Garmin steuert den tatsächlichen Ausführungszeitpunkt;
  insbesondere Mitternacht, Neustart, Energiesparen und Zeitzonenwechsel können
  eine Verzögerung verursachen. Das Zifferblatt zeigt bis dahin den zuletzt
  veröffentlichten Wert. Keine Zusage einer sekundengenauen Umschaltung.
- Es werden keine Benachrichtigungen, Vibrationen oder Startaufforderungen erzeugt.

## Zifferblatt einrichten (nach erfolgreicher Installation)

In den Einstellungen des bestehenden Zifferblatts ein Datenfeld auf
**Complication** stellen und, sofern die Auswahl angeboten wird, **Tagescode**
als Quelle auswählen. Farbe, Grösse und Position legt das Zifferblatt fest.
Die genaue Menüfolge auf der installierten Version ist noch zu verifizieren.

## Bauen

Connect IQ SDK 9.2.0 und Geräteprofil `fenix847mm` im Garmin SDK Manager
installieren. Einen privaten Entwicklerschlüssel erstellen und ausserhalb
des Repositorys sichern. Den Schlüssel nicht committen.

```text
monkeyc -f monkey.jungle -d fenix847mm -y <developer_key.der> -o bin/Tagescode.prg -w
```

Nur eine für `fenix847mm` gebaute PRG-Datei ist zur Geräteinstallation gedacht.
Ein generischer Compilerlauf ohne Geräteprofil reicht nicht als Geräteprüfung.
Die App-ID ist `38e977f4f09e45ffab433147c238fed4` und muss mit
`GarminCodePacket.APP_ID` in der Android-App übereinstimmen.

## Abnahme auf dem Gerät

1. Paket übertragen, App öffnen, sechsstelligen Code mit dem Handy vergleichen.
2. Complication im bestehenden Zifferblatt auswählen und führende Null prüfen.
3. Nach Trennen des Handys bleibt der heutige gespeicherte Code verfügbar.
4. Tageswechsel mit vorhandenem und fehlendem Folgetag prüfen.
5. Neustart und Energiesparmodus prüfen; Aktualisierungsverzögerung erfassen.
6. Uhr nicht erreichbar: Android bleibt bedienbar; manuelle Übertragung meldet
   einen Fehler und kann wiederholt werden.

## Grundlagen

- https://developer.garmin.com/connect-iq/api-docs/Toybox/Complications.html
- https://github.com/garmin/connectiq-android-sdk
- https://apps.bliemel.net/watch-face-features/

Die Tagescodes und privaten Signierschlüssel sind nicht im Repository enthalten.
