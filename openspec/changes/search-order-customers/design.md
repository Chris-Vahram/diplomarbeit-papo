## Context

Der Partnerwizard nutzt bisher `kundePickerHtml/wireKundePicker`, blendet deren Inline-Neuanlage aber aus. Die Company-Bestellung benötigt diese Helfer weiterhin. Endkunden-Neuanlage im Wizard wird als `newKunde` im abschließenden Order-POST transaktional gespeichert.

## Goals / Non-Goals

Suchbarer erster Partnerschritt; keine Änderung an Company-Bestellung, API oder Speicherzeitpunkt.

## Decisions

- Den ersten Schritt lokal in `partner-order-new-page.js` rendern: beschriftetes `input type=search`, native Select-Liste mit leerer Auswahloption, „Nächster Schritt“ und eigener `type=button` „Neuen Kunden anlegen“. Keine neue Comboboxbibliothek oder duplizierte Companyform. Unbenötigte Pickerimporte nur auf der Partnerseite entfernen; gemeinsame Helfer erhalten.
- Suche lokal mit `trim().toLocaleLowerCase('de-AT')` über Vorname, Nachname und formatierten Adresstext. Keine Begrenzung auf die ersten 20 Treffer. Native Select-Optionen über DOM-`textContent`/escaped Markup; Werte sind Kunden-IDs. Leere Suche zeigt alle eigenen geladenen Kunden; kein Treffer zeigt „Keine Endkunden gefunden“ und lässt Neuanlage verfügbar.
- Keine automatische Erstauswahl. Wird die aktuelle Auswahl durch Filterwechsel unsichtbar, lokale Auswahl auf leer setzen und Weiter sperren. Bestätigte Wizardauswahl bleibt bis einer neuen bewussten Auswahl/Neuanlage im State; vorwärts über Checkpoints ist während unbestätigtem Wechsel gesperrt. Eingaben nur im Seitenzustand.
- Weiter prüft, dass die ausgewählte ID in `this.customers` enthalten ist, setzt bestehenden Kunden und geht direkt zu Artikeln. Neuanlagebutton setzt New-Modus, löscht die aktive bestehende ID und öffnet den vorhandenen Neuanlageschritt. Ein vorhandener neuer Kundenentwurf bleibt beim Zurückgehen erhalten und kann ausdrücklich mit „Kundenentwurf fortsetzen“ erneut geöffnet werden; kein stiller Verlust durch Suchnavigation.
- Wechsel zwischen Kunden erhält den Warenkorb, aber aktualisiert Kundenkontext und Checkpoints. Bestehendes goToStep prüft bislang nur maxStepReached und reicht nicht: einen lokalen gemeinsamen Kunden-Weitercheck in goToStep und den Weiter-/Checkpointzuständen ergänzen. Jede unbestätigte Änderung der lokalen Auswahl sperrt Vorwärtssprünge über Kundenauswahl; im Neuentwurf sperren geänderte oder ungültige Felder Artikel/Übersicht bis zum erneuten validen Formularsubmit. Dafür input/change des neuen Kundenformulars als unbestätigt markieren, auch wenn state.newKunde noch alte valide Werte enthält. Zurückgehen bleibt möglich; direkte Sprünge zu bereits besuchter Übersicht müssen dieselben Gates erfüllen.
- Bei erfolgreichem Neustart nach Bestellung: Suche, Auswahl, Entwurf und Warenkorb leer. GET-Fehler bietet keine vermeintlich vollständige leere Kundenliste an; bestehende Fehleranzeige bleibt.

## Risks / Trade-offs

Native Suche plus Select braucht zwei Controls, bietet aber echte Tastaturauswahl ohne neue Accessibility-Logik. Nur eigenes GET-Ergebnis durchsuchen; keine globale Kundenabfrage.

## Migration Plan

Keine Daten-/API-Migration. Unabhängig von der Übersichtsanlage; nach deren Umsetzung Wiederverwendung neu angelegter Kunden mitprüfen. `clarify-order-customer-context` kann vorher oder danach angewendet werden; gleiche Datei gezielt integrieren.
