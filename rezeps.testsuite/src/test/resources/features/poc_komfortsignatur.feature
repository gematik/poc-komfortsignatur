@E2EManual
Feature: Komfortsignatur
  Um die Komfortsignatur nutzen zu können, sind folgende Konnektoreinstellungen vorab notwendig:
  1. TLS-Verbindung in der Stufe 3 zwischen Konnektor und PS konfiguriert
  2. SAK_COMFORT_SIGNATURE = Enabled

  Background:
    Given Aufrufkontext "Mandant1", "ClientID1" und "Workplace1" ist gesetzt
    And Das Handle eines signierenden HBAs liegt vor


  Scenario: Testfall: Komfort-Signatur aktivieren

    When PS erstellt neue UserID
    When PS aktiviert Komfortsignatur
    Then Konnektor schickt ActivateComfortSignature-Response mit Status OK


  Scenario: Testfall: Qualifiziert signieren mit Komfort-Signatur
    #überprüft mit ERP_IOP_AF2x1_001 GF E-Rezept erzeugen: ein Verordnungsdatensatz

    Given Verordnungsdatensatz für Coverage wurde erstellt: "109500969", "Test GKV-SV", "1"
    And Verordnungsdatensatz für Patient wurde erstellt: "Angermänn", "Günther", "X1104655770", "Straße", "Hausnummer", "10905", "Berlin", "01.04.1998",
    And Verordnungsdatensatz für Medication wurde erstellt: "1527732", "Trimipramin-neuraxpharm"
    And Eine Job-ID zum Signieren eines Dokumentes ist verfügbar
    And UserId liegt vor

    When E-Rezept mittels HBA signieren
    Then Konnektor schickt SignDocument-Response mit Status OK



  Scenario: Testfall: Qualifiziert signieren mit Komfort-Signatur nach Wechsel des Arbeitsplatzes

    Given Eine Job-ID zum Signieren eines Dokumentes ist verfügbar
    And Aufrufkontext "Mandant1", "ClientID1" und "Workplace2" ist gesetzt
    And Verordnungsdatensatz für Coverage wurde erstellt: "109500969", "Test GKV-SV", "1"
    And Verordnungsdatensatz für Patient wurde erstellt: "Angermänn", "Günther", "X1104655770", "Straße", "Hausnummer", "10905", "Berlin", "01.04.1998",
    And Verordnungsdatensatz für Medication wurde erstellt: "1527732", "Trimipramin-neuraxpharm"
    And UserId liegt vor

    When E-Rezept mittels HBA signieren
    Then Konnektor schickt SignDocument-Response mit Status OK


  Scenario: Testfall: Signature Mode für HBA nach Aktivieren der Komfort-Signatur prüfen (COMFORT erwartet)

    Given UserId liegt vor

    When PS fragt Signature Mode für HBA ab
    Then Konnektor antwortet mit Signatur-Mode COMFORT

  Scenario: Testfall: Komfort-Signatur deaktivieren

    When PS deaktiviert Komfortsignatur
    Then Konnektor schickt DeactivateComfortSignature-Response mit Status OK


  Scenario: Testfall: Signature Mode für HBA nach Deaktivieren der Komfort-Signatur prüfen (keine SessionInfo in Response erwartet)

    Given UserId liegt vor

    When PS fragt Signature Mode für HBA ab
    Then Konnektor-Response enthält keine SessionInfo

