# Tagescode Android

Die App zeigt den heutigen sechsstelligen Tagescode an und stellt ihn zusÃ¤tzlich direkt auf dem Android-Startbildschirm in einem Widget dar.

## Funktionen

* Widget zeigt den heutigen sechsstelligen Tagescode.
* Widget zeigt zusÃ¤tzlich das aktuelle Datum.
* Tippen auf das Widget Ã¶ffnet die App.
* In der App wird zuerst der heutige Code angezeigt.
* Ãœber **Anderes Datum** kann ein Tagescode fÃ¼r ein anderes Datum gesucht werden.
* Ãœber **Heute** kann wieder direkt zum aktuellen Tagescode gewechselt werden.
* Die manuelle Auswahl wird nicht gespeichert. Nach dem Verlassen oder erneuten Ã–ffnen der App wird wieder der heutige Code angezeigt.
* Das Widget bleibt immer beim heutigen Tagescode.
* Automatische Aktualisierung des Widgets beim Tageswechsel.
* ZusÃ¤tzliche Aktualisierung bei ZeitÃ¤nderung, ZeitzonenÃ¤nderung und Neustart.
* Automatischer Hell- und Dunkelmodus.
* Unterschiedliches Videojet-Logo fÃ¼r Hell- und Dunkelmodus.
* Eigenes Tagescode-App-Icon.
* Bereits gespeicherte Codes funktionieren offline; neue OneDrive-Codes werden bei verfÃ¼gbarem Dateizugriff importiert.

## Weitere Tagescodes

In der App werden zusÃ¤tzlich folgende Tagescodes angezeigt:

* Code von gestern
* 01.01.2000
* 01.01.2001
* 01.01.2006

## APK mit GitHub Actions erstellen

1. Den gesamten Inhalt dieses Ordners in die oberste Ebene eines GitHub-Repositories hochladen.
2. In GitHub oben **Actions** Ã¶ffnen.
3. Den Workflow **Android APK erstellen** auswÃ¤hlen.
4. Nach erfolgreichem Build unten den Artifact-Download **Tagescode-APK** herunterladen.
5. Die ZIP entpacken und die APK mit dem Namen `Tagescode_V<Versionsnummer>.apk` auf dem Android-Handy installieren (zum Beispiel `Tagescode_V1.3.106.apk`).

Die GitHub Action verwendet eine online installierte Gradle-Version. Ein lokaler Gradle Wrapper ist deshalb fÃ¼r den Online-Build nicht erforderlich.

## Eine zentrale Tagescodes.txt

Beim ersten Start die von Power Automate befÃ¼llte `Tagescodes.txt` im Android-Dateiauswahldialog auswÃ¤hlen. Dazu muss OneDrive als Dateianbieter verfÃ¼gbar und angemeldet sein. Die App speichert die Leseberechtigung dauerhaft.

Beim Ã–ffnen der App und beim tÃ¤glichen Widget-Update wird die Datei im Hintergrund eingelesen. Neue Datumswerte werden im privaten App-Speicher gesichert. Vorhandene Codes werden nicht Ã¼berschrieben, bei Duplikaten gewinnt der erste gÃ¼ltige Eintrag. FÃ¼hrende Nullen bleiben erhalten. Grusszeilen und ungÃ¼ltige Daten werden ignoriert. Bei einem abgebrochenen Lesevorgang wird kein Teilimport gespeichert. Die lokale Datei wird atomar ersetzt.

UnterstÃ¼tzte Formate sind `MM/DD/YYYY`, `DD.MM.YYYY`, `YYYY-MM-DD` und `DD-MM-YYYY`, jeweils gefolgt von sechs Ziffern. SchrÃ¤gstriche bedeuten immer Monat/Tag/Jahr, passend zur Power-Automate-Datei. Beispiel: `10/02/2026 000416` ist der 2. Oktober.

Zum Wechseln der Datei oder erneuten Erteilen der Berechtigung unten auf die Versionsanzeige tippen und **OneDrive-Datei auswÃ¤hlen** wÃ¤hlen. Dort lÃ¤sst sich auch weiterhin ein **PwD-Ordner auswÃ¤hlen**. Eine bestehende Ordnerfreigabe bleibt erhalten; neue EintrÃ¤ge aus dessen `PwD.txt` werden ebenfalls lokal gespeichert. Die integrierte Codeliste bleibt verfÃ¼gbar.

Bei fehlendem Internet, abgemeldetem OneDrive oder entzogenem Zugriff bleiben lokal gespeicherte Codes erhalten. In der App erscheint bei fehlgeschlagenem Cloud-Import ein Hinweis. Android und der Dateianbieter bestimmen, wann aktuelle Cloud-Inhalte verfÃ¼gbar sind; die App kann keine sofortige OneDrive-Synchronisation erzwingen.

## Widget-Aktualisierung

Das Widget zeigt beim Tageswechsel sofort den lokal bekannten Code. Der Alarm kurz nach Mitternacht plant zusÃ¤tzlich einen Android-Hintergrundjob zum Import ein. Nach dem Import wird das Widget erneut aktualisiert. Fehlgeschlagene Cloud-Zugriffe werden mit zeitlichem Abstand erneut versucht. Android darf Alarm und Hintergrundjob im Energiesparmodus verzÃ¶gern.

## Tests

`gradle :app:testDebugUnitTest :app:assembleDebug`

Die Importtests prÃ¼fen Datumsformate, fÃ¼hrende Nullen, Duplikate, ungÃ¼ltige EintrÃ¤ge und Lesefehler. Auf einem Android-GerÃ¤t zusÃ¤tzlich Dateiauswahl, dauerhafte Freigabe nach Neustart, Offline-Anzeige und tÃ¤glichen Widget-Import mit dem verwendeten OneDrive-Anbieter prÃ¼fen.

## Version

Die App zeigt im unteren Bereich die aktuelle Versionsnummer sowie Monat und Jahr des Builds an.

Beispiel:

`Version 1.3.86 Â· August 2026`

## Kalender und Sperrbildschirm

Im Kalender sind ausschliesslich Daten mit gespeichertem Tagescode auswÃ¤hlbar. Fehlende Tage bleiben ausgegraut; die Monatsnavigation Ã¼berspringt Monate ohne Codes. Ãœber die MonatsÃ¼berschrift lassen sich Monat und Jahr direkt wÃ¤hlen. Neue Importe sind beim nÃ¤chsten Ã–ffnen des Kalenders verfÃ¼gbar.

Das Widget deklariert UnterstÃ¼tzung fÃ¼r Start- und Sperrbildschirm. Auf unterstÃ¼tzten GerÃ¤ten: Sperrbildschirm lange drÃ¼cken, Widgets Ã¶ffnen und Tagescode auswÃ¤hlen. Ob ein Drittanbieter-Widget angeboten wird, bestimmt der jeweilige Android-/Samsung-Widget-Host; die App kann diese Auswahl nicht erzwingen. Die Sperrbildschirm-Anzeige muss auf dem ZielgerÃ¤t geprÃ¼ft werden.

## Interaktive Browser-Vorschau

Mit `python preview/build_preview.py preview/index.html "PFAD/ZU/Tagescodes.txt"` wird `preview/index.html` aus der zentralen Datei erzeugt und kann im Browser geÃ¶ffnet werden. Die Vorschau bettet alle gültigen Codes der angegebenen TXT-Datei ein und simuliert Kalender, Anzeige, Hell-/Dunkelmodus und TXT-Import. Importe bleiben nur im Arbeitsspeicher des Browsers. Sie ersetzt keinen Android-Test fÃ¼r OneDrive-Berechtigungen, Hintergrundjobs oder echte Widgets. Nach Ã„nderungen an Layout oder Logik die Vorschau entsprechend mitpflegen.
