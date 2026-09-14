## 1. Einfache Company-Navigation (wesentlich)

- [x] 1.1 Company-Sidebar und Artikel-Seitenüberschrift auf „Artikelverwaltung“ umbenennen, separaten Sidebarpunkt „Artikelkategorien“ entfernen und per `node --check` prüfen; Partnernavigation bleibt „Artikel“.
- [x] 1.2 In Artikelverwaltung „Kategorien verwalten“ und auf Kategorienseite „Zur Artikelverwaltung“ mit bestehenden Hash-Routen ergänzen; keine API-, CRUD- oder Zustandslogik duplizieren.

## 2. Bedien- und Regressionsprüfung (wesentlich)

- [x] 2.1 Company-Flow bei 1440 px und 390 px prüfen: Navigation, beide Richtungen, Reload/Direktlink, Tastaturbedienung, sichtbarer Fokus und mobile Aktionsanordnung; Screenshots nur bei echtem Dokumentationsnutzen speichern.
- [x] 2.2 Partnernavigation sowie bestehendes Kategorien-CRUD und Artikel-Kategorieauswahl regressionsprüfen, `openspec validate integrate-category-management-into-article-management --strict` und `git diff --check` erfolgreich ausführen.
