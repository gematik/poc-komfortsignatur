# Release 0.2.1
Im Testfall "Komfortsignatur aktivieren" gab es einen Bug, der dazu führte, dass der Testfall auf einen Fehler lief, wenn der erste Request ActivateComfortSignature nicht mit dem SOAP-Fault 4018 beantwortet wurde. Dieser Bug wurde gefixt.

# Release 0.2.0
* Es wurde ein Testfall zur Aktivierung der Komfortsignatur hinzugefügt, der den Ablauf verdeutlich, wenn der Sicherheitszustand des HBAs bereits erhöht ist
* Der existierende PoC wurde an Veränderungen im Testsystem angepasst

# Release 0.1.0
* Komfortsignatur aktivieren / deaktivieren
* Signatur-Modus abfragen
* Verordnungsdatensatz mit Komfort signieren
* Verordnungsdatensatz nach Wechsel des Arbeitsplatzes mit Komfort signieren

