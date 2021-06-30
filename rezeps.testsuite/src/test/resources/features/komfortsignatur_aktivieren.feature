@E2EManual
Feature: Komfortsignatur aktivieren
  Im Feature wird zunächst ein ExternalAuthenticate durchgeführt. Dazu wird eine schwache User-ID
  verwendet und die PIN manuell eingegeben. Anschließend wird versucht die Komfortsignatur zu
  aktivieren, dies schlägt fehl. Danach wird die Karte ausgeworfen, erneut gesteckt und die
  Komfortsignatur nochmals aktiviert, dieses mal erfolgreich. Im Anschluss wird ein Dokument mit
  Komfort signiert. Der Ablauf endet mit dem Freischalten der PIN.CH mit der starken UserID.

  Background:
    Given EE Testfalldaten entfernen
    And Aufrufkontext "Mandant1", "ClientID1", "Workplace1" und "User" ist gesetzt
    And Das Handle eines signierenden HBAs liegt vor
    And Verordnungsdatensatz für Coverage wurde erstellt: "109500969", "Test GKV-SV", "1"
    And Eine Job-ID zum Signieren eines Dokumentes ist verfügbar

  Scenario: Komfortsignatur aktivieren

    When PS schaltet PIN.CH frei
    When Testsystem generiert zufällige CodeChallenge
    When PS signiert Challenge mittels externalAuthenticate mit HBA
    Then Konnektor schickt externalAuthenticate-Response mit Status OK
    When PS erstellt neue UserID
    And PS aktiviert Komfortsignatur
    Then Konnektor antwortet auf ActivateComfortSignature mit Fault 4018
    Given Aufrufkontext "Mandant1", "ClientID1", "Workplace1" und "User" ist gesetzt
    When PS fordert Auswurf des HBAs an
    Then Konnektor antwortet auf EjectCard mit Fault 4203
    When PS fordert das Stecken des HBAs in das Terminal mit der ID {string} und in Slot {int} an 
    Then Konnektor sendet RequestCardResponse mit Status OK und einem CardHandle
    Given Das Handle eines signierenden HBAs liegt vor
    When PS erstellt neue UserID
    And PS aktiviert Komfortsignatur
    Then Konnektor schickt ActivateComfortSignature-Response mit Status OK
    When E-Rezept mittels HBA signieren
    Then Konnektor schickt SignDocument-Response mit Status OK
    When PS schaltet PIN.CH frei
    Then Konnektor antwortet mit Result OK und PinResult OK
