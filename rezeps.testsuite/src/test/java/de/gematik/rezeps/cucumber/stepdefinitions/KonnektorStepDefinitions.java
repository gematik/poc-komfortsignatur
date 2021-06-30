/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.rezeps.cucumber.stepdefinitions;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.KonnektorClient;
import de.gematik.rezeps.bundle.Coverage;
import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.gluecode.*;
import de.gematik.rezeps.gluecode.FdClientGlueCode;
import de.gematik.rezeps.gluecode.IdpGlueCode;
import de.gematik.rezeps.gluecode.KonnektorGlueCode;
import de.gematik.rezeps.gluecode.MissingPreconditionException;
import de.gematik.test.logger.TestNGLogManager;
import io.cucumber.java.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.junit.Assert;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class KonnektorStepDefinitions {

  private KonnektorGlueCode konnektorGlueCode;
  private FdClientGlueCode fdClientGlueCode;
  private IdpGlueCode idpGlueCode;
  private static String testDataFilePath;
  private static ConfigurableApplicationContext appContext;

  static {
    TestNGLogManager.useTestNGLogger = false;
  }

  @Before()
  public void set_the_stage() throws IOException {
    if (appContext == null) {
      OnStage.setTheStage(new OnlineCast());
      String[] args = new String[] {};
      appContext = SpringApplication.run(KonnektorClient.class, args);

      konnektorGlueCode = new KonnektorGlueCode(appContext);
      idpGlueCode = new IdpGlueCode(appContext);
      fdClientGlueCode = new FdClientGlueCode();
    }
    testDataFilePath = System.getProperty("java.io.tmpdir") + File.separatorChar + "TCaseData.dat";

    Path testcaseDataFilePath = Paths.get(testDataFilePath);
    File testcaseDataFile = testcaseDataFilePath.toFile();

    if (!testcaseDataFile.exists()) {
      LoggerFactory.getLogger(KonnektorStepDefinitions.class)
          .info("TestCaseData file created: {} ", testcaseDataFile.createNewFile());
    } else {
      TestcaseData.getInstance().deserializeFromFile(testDataFilePath);
    }
  }

  @After
  public void afterScenario(Scenario scenario) {
    if (Status.PASSED.equals(scenario.getStatus())) {
      scenario.log("Speichere TestDaten aus Scenario: \"" + scenario.getName() + "\"");
      TestcaseData.getInstance().serializeToFile(testDataFilePath);
    }
  }

  @Given("EE Testfalldaten entfernen")
  public void removeTestcaseDataFile() {
    try {
      Path path = Paths.get(testDataFilePath);
      boolean deleted = Files.deleteIfExists(path);
      LoggerFactory.getLogger(KonnektorStepDefinitions.class)
          .info("TestCaseData file deleted: {} ", deleted);
    } catch (IOException ioException) {
      throw new IllegalStateException(ioException.getMessage(), ioException);
    }
  }

  @Given("Das Handle eines signierenden HBAs liegt vor")
  public void determineCardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle();
  }

  // REZEPS-125
  @Given("Das Handle eines signierenden HBAs mit der ICCSN {string} liegt vor")
  public void determineCardHandle(String iccsn) throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle(iccsn);
  }

  // REZEPS-59
  @Given("Aufrufkontext {string}, {string}, {string} und {string} ist gesetzt")
  public void determineContext(String mandant, String clientSystem, String workplace, String user)
      throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(mandant, clientSystem, workplace, user));
  }

  @When("E-Rezept mittels HBA signieren")
  public void signPrescription() throws MissingPreconditionException {
    konnektorGlueCode.signPrescription();
  }

  @Given(
      "Verordnungsdatensatz für Patient wurde erstellt: {string}, {string}, {string}, {string}, "
          + "{string}, {string}, {string}, {string},")
  public void createPrescriptionWithPatientData(
      String givenName,
      String surname,
      String kvnr,
      String street,
      String houseNumber,
      String postalCode,
      String city,
      String birthday) {
    konnektorGlueCode.modifyBundleWithPatientData(
        new Patient(givenName, surname, kvnr, street, houseNumber, postalCode, city, birthday));
  }

  @Given("Verordnungsdatensatz für Coverage wurde erstellt: {string}, {string}, {string}")
  public void createPrescriptionWithCoverageData(String iknr, String name, String status) {
    konnektorGlueCode.modifyBundleWithCoverageData(new Coverage(iknr, name, status));
  }

  @Given("Verordnungsdatensatz für Medication wurde erstellt: {string}, {string}")
  public void createPrescriptionWithMedicationData(String pznValue, String pznText) {
    konnektorGlueCode.modifyBundleWithMedicationData(new Medication(pznValue, pznText));
  }

  @Given("Access Token des IDP liegt vor (verordnende Praxis)")
  public void obtainAccessTokenPrescribingEntity() throws MissingPreconditionException {
    idpGlueCode.obtainAccessTokenPrescribingEntity();
  }

  @When("E-Rezept-ID beim eRP-FD abrufen")
  public void createPrescriptionId() {
    fdClientGlueCode.invokeTaskCreate();
  }

  @When("E-Rezept vervollständigen")
  public void completePrescription() throws MissingPreconditionException {
    konnektorGlueCode.modifyBundleWithTaskData();
  }

  @Then("eRP-FD antwortet mit Code 201 Created")
  public void checkResponseCreated() {
    Assert.assertTrue(fdClientGlueCode.checkTaskCreatedOk());
  }

  @Then("Konnektor schickt SignDocument-Response mit Status OK")
  public void checkSignDocumentResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.verifySignPrescription());
  }

  // REZEPS-13
  @Given("Signiertes E-Rezept liegt vor")
  public void checkSignedPrescriptionAvailable() throws MissingPreconditionException {
    konnektorGlueCode.checkSignedPrescriptionAvailable();
  }

  @When("E-Rezept beim eRP-FD einstellen")
  public void activatePrescription() {
    fdClientGlueCode.invokeTaskActivate();
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Operation $activate")
  public void checkActivateResponseOk() {
    Assert.assertTrue(fdClientGlueCode.checkTaskActivateOk());
  }

  // REZEPS-16
  @Given("Access-Code des E-Rezepts liegt vor")
  public void checkAccessCodeAvailable() throws MissingPreconditionException {
    fdClientGlueCode.checkAccessCodeAvailable();
  }

  // REZEPS-17
  @Given("Task-ID des E-Rezepts liegt vor")
  public void ensureTaskId() throws MissingPreconditionException {
    fdClientGlueCode.checkTaskIdAvailable();
  }

  // REZEPS-18
  @When("E-Rezept durch Verordnenden löschen")
  public void abortByDoctor() throws MissingPreconditionException {
    fdClientGlueCode.invokeTaskAbortByDoctor();
  }

  // REZEPS-119
  @Then("eRP-FD antwortet mit Code 204 nach Operation $abort")
  public void checkAbortResponseNoContent() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNoContent());
  }

  // REZEPS-120
  @Then("eRP-FD antwortet mit Code 204 nach Operation $reject")
  public void checkRejectResponseNoContent() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkRejectResponseNoContent());
  }
  // TODO Name Funktion anpassen

  // REZEPS-20
  @When("E-Rezept durch Abgebenden löschen")
  public void abortByPharmacist() throws MissingPreconditionException {
    fdClientGlueCode.invokeTaskAbortByPharmacist();
  }

  @When("E-Rezept durch Abgebenden abrufen")
  public void acceptByPharmacist() {
    fdClientGlueCode.invokeTaskAccept();
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Operation $accept")
  public void checkAcceptPharmacistOk() {
    Assert.assertTrue(fdClientGlueCode.checkTaskAcceptOk());
  }

  // REZEPS-24
  @Given("Secret für abrufende Apotheke liegt vor")
  public void ensureSecret() throws MissingPreconditionException {
    Assert.assertTrue(idpGlueCode.checkSecretAvailable());
  }

  @When("Signatur des E-Rezepts prüfen")
  public void verifySignature() throws MissingPreconditionException {
    konnektorGlueCode.verifySignature();
  }

  @Then("Konnektor schickt VerifyDocument-Response mit Status OK")
  public void checkVerifySignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isValidSignature());
  }

  @Given("Signiertes E-Rezept aus eRP-FD liegt vor")
  public void ensureSignedPrescription() {
    Assert.assertTrue(fdClientGlueCode.checkTaskActivateOk());
  }

  @Given("Eine Job-ID zum Signieren eines Dokumentes ist verfügbar")
  public void determineJobId() throws MissingPreconditionException {
    konnektorGlueCode.determineJobNumber();
  }

  // REZEPS-30
  @Given("Medication Dispense wurde erstellt")
  public void determineMedicationDispense() {
    // will resolved within !E-Rezept-Abgabe vollziehen und Quittung abrufen!
  }

  @When("E-Rezept-Abgabe vollziehen und Quittung abrufen")
  public void closeTask() {
    fdClientGlueCode.invokeTaskClose();
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Operation $close")
  public void checkTaskCloseOk() {
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseOk());
  }

  // REZEPS-34
  @When("Signatur der Quittung prüfen")
  public void verifyPrescriptionSignature() throws MissingPreconditionException {
    konnektorGlueCode.verifySignature();
  }

  // REZEPS-35
  @When("Quittung erneut abrufen")
  public void getTaskById() throws MissingPreconditionException {
    fdClientGlueCode.getDispensedMedicationReceipt();
  }

  // REZEPS-36
  @When("E-Rezept zurückgeben")
  public void rejectTask() throws MissingPreconditionException {
    fdClientGlueCode.invokeTaskReject();
  }

  // REZEPS-37
  @Given("Signierte Quittung liegt vor")
  public void ensureSignedPrescriptionAfterClose() throws MissingPreconditionException {
    fdClientGlueCode.checkSignedPrescriptionAvailableAfterClose();
  }

  // REZEPS-38
  @Then("eRP-FD antwortet mit Code 200 nach Operation Quittung erneut abfragen")
  public void checkOperationOk() {
    Assert.assertTrue(fdClientGlueCode.checkSignedReceiptStatus());
  }

  @Given("Access Token des IDP liegt vor (abgebende Praxis)")
  public void obtainAccessTokenDispensingEntity() throws MissingPreconditionException {
    idpGlueCode.obtainAccessTokenDispensingEntity();
  }

  // StepDefinition für Nachricht:

  @When("Nachricht durch Abgebenden empfangen")
  public void getCommunication() {
    fdClientGlueCode.invokeCommunicationGetAsPharmacist();
  }

  // REZEPS-46
  @Then("eRP antwortet mit Code 200 OK, enthält Zuweisung")
  public void checkGetCommunicationOkDispReq() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  // REZEPS-47
  @Then("eRP antwortet mit Code 200 OK, enthält Anfrage")
  public void checkGetCommunicationOkInfoReq() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  // REZEPS-48
  @Given("Task-ID des E-Rezepts aus Nachricht liegt vor")
  public void makeTaskIdAvailableFromCommunication() {
    Assert.assertNotNull(TestcaseData.getInstance().getTaskIdFromDispReqTaskReference());
  }

  // REZEPS-49
  @Given("Access Code des E-Rezepts aus Nachricht liegt vor")
  public void makeAccessCodeAvailableFromCommunication() {
    Assert.assertNotNull(TestcaseData.getInstance().getAccessCodeFromDispReqTaskReference());
  }

  // REZEPS-51
  @Given("Nachricht wurde erstellt: {string}, {string}")
  public void determineCommunication(String kvnrRecipient, String messageText) {
    fdClientGlueCode.determineCommunication(kvnrRecipient, messageText);
  }

  // REZEPS-50
  @When("Nachricht durch Abgebenden übermitteln")
  public void sendCommunication() throws MissingPreconditionException {
    fdClientGlueCode.sendCommunication();
  }

  // REZEPS-52
  @Then("eRP antwortet mit Code 201 Created nach Operation Post Communication")
  public void checkPostCommunicationOkReply() {
    Assert.assertTrue(fdClientGlueCode.checkPostCommunicationOkReply());
  }

  // StepDefinition für Komfort-Signatur:

  // REZEPS-65
  @Given("Signaturmodus des HBA ist COMFORT")
  public void determineSignatureModeComfort() throws MissingPreconditionException {
    konnektorGlueCode.determineSignatureModeComfort();
  }

  // REZEPS-62
  @When("PS aktiviert Komfortsignatur")
  public void activateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.activateComfortSignature();
  }

  // REZEPS-69
  @When("PS deaktiviert Komfortsignatur")
  public void deactivateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.deactivateComfortSignature();
  }

  // REZEPS-63
  @Then("Konnektor schickt ActivateComfortSignature-Response mit Status OK")
  public void checkActivateComfortSignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isActivateComfortSignatureOk());
  }

  // REZEPS-70
  @Then("Konnektor schickt DeactivateComfortSignature-Response mit Status OK")
  public void checkDeActivateComfortSignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isDeactivateComfortSignatureOk());
  }

  // REZEPS-79
  @When("PS fragt Signature Mode für HBA ab")
  public void getSignatureMode() throws MissingPreconditionException {
    konnektorGlueCode.determineSignatureMode();
  }

  // REZEPS-80
  @Then("Konnektor antwortet mit Signatur-Mode COMFORT")
  public void checkGetSignatureModeResponseComfort() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isSignatureModeComfort());
  }

  // REZEPS-81
  @Then("Konnektor-Response enthält keine SessionInfo")
  public void checkGetSignatureModeResponsePin() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isSignatureModePin());
  }

  // REZEPS-82
  @When("PS fragt Status der QES-PIN ab")
  public void getPinStatusQes() throws MissingPreconditionException {
    konnektorGlueCode.determinePinStatusQes();
  }

  // REZEPS-83
  @Then("Konnektor antwortet mit PIN-Status VERIFIED")
  public void checkGetPinStatusResponseVerified() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  // REZEPS-84
  @Then("Konnektor antwortet mit PIN-Status VERIFIABLE")
  public void checkGetPinStatusResponseVerifiable() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  // REZEPS-86
  @Then("Konnektor signiert NICHT erfolgreich")
  public void checkSignDocumentNotSuccessful() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.checkSignDocumentHasFailed());
  }

  // REZEPS-87
  @Given("Aufrufkontext {string}, {string} und {string} ist gesetzt")
  public void determineContext3Parameter(String mandant, String clientSystem, String workplace)
      throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(mandant, clientSystem, workplace));
  }

  // REZEPS-88
  @When("Konnektor ermittelt IP Adresse {string}")
  public void determineIPAddress(String fqdn) {
    konnektorGlueCode.getIPAddress(fqdn);
  }

  // REZEPS-61
  @Given("UserId liegt vor")
  public void determineUserId() throws MissingPreconditionException {
    konnektorGlueCode.ensureUserId();
  }

  // REZEPS-71
  @When("PS erstellt neue UserID")
  public void createUserId() throws MissingPreconditionException {
    konnektorGlueCode.determineUserId();
  }

  // StepDefinition für IDP-Funktionen:

  @Given("Das Handle einer HBA_Q_SIG liegt vor")
  public void determineHBA_Q_SIGCardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineHBAqSigHandle();
  }

  @Given("Das Handle einer ZOD_2_0 liegt vor")
  public void determineZOD_2_0CardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineZOD20Handle();
  }

  @Given("Das Handle einer HBA_Q_SIG mit der ICCSN {string} liegt vor")
  public void determineHBA_Q_SIGCardHandle(String iccsn) throws MissingPreconditionException {
    konnektorGlueCode.determineHBAqSigHandle(iccsn);
  }

  @Given("Das Handle einer ZOD_2_0 mit der ICCSN {string} liegt vor")
  public void determineZOD_2_0CardHandle(String iccsn) throws MissingPreconditionException {
    konnektorGlueCode.determineZOD20Handle(iccsn);
  }
  // alte Karten ENDE

  // REZEPS-58
  @Given("Das Handle einer SMC-B liegt vor")
  public void determineSMCBCardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineSmcBHandle();
  }

  // REZEPS-125
  @Given("Das Handle einer SMC-B mit der ICCSN {string} liegt vor")
  public void determineSMCBCardHandle(String iccsn) throws MissingPreconditionException {
    konnektorGlueCode.determineSmcBHandle(iccsn);
  }

  // REZEPS-78
  @When("PS fragt Access-Token beim IDP an")
  public void getAccessToken() {
    idpGlueCode.obtainAccessToken();
  }

  // REZEPS-77
  @Then("IDP antwortet mit Code 200 ok, enthält ACCESS_TOKEN")
  public void checkGetAccessTokenResponseOk() {
    Assert.assertTrue(idpGlueCode.checkAccessTokenOk());
  }

  // REZEPS-57
  @When("PS signiert Challenge mittels externalAuthenticate mit SMC-B")
  public void externalAuthenticateSmcB() throws MissingPreconditionException {
    konnektorGlueCode.authenticateExternallySmcB();
  }

  // REZEPS-116
  @When("PS signiert Challenge mittels externalAuthenticate mit HBA")
  public void externalAuthenticateHba() throws MissingPreconditionException {
    konnektorGlueCode.authenticateExternallyHba();
  }

  // REZEPS-73
  @Then("Konnektor schickt externalAuthenticate-Response mit Status OK")
  public void checkExternalAuthenticateResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isExternalAuthenticateOk());
  }

  // REZEPS-103
  @When("PS schaltet PIN.CH frei")
  public void verifyPin() throws MissingPreconditionException {
    konnektorGlueCode.verifyPin();
  }

  @When("PS schaltet SMC-B PIN.CH frei")
  public void verifyPinSMCB() throws MissingPreconditionException {
    konnektorGlueCode.verifyPin(TestcaseData.getInstance().getSmcBHandle());
  }

  @When("PS schaltet HBA PIN.CH frei")
  public void verifyPinHBA() throws MissingPreconditionException {
    konnektorGlueCode.verifyPin(TestcaseData.getInstance().getHbaHandle());
  }

  // REZEPS-104
  @Then("Konnektor antwortet mit Result OK und PinResult OK")
  public void checkVerifyPinResult() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isVerifyPinOk());
  }

  // REZEPS-107
  @When("PS fordert Auswurf des HBAs an")
  public void ejectCard() throws MissingPreconditionException {
    konnektorGlueCode.ejectCard();
  }

  // REZEPS-108
  @Then("Konnektor sendet EjectCardResponse mit Result OK")
  public void checkEjectCardOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isEjectCardOk());
  }

  // REZEPS-130
  @Then("Konnektor antwortet auf EjectCard mit Fault 4203")
  public void checkSoapFault4203() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isEjectCardSoapFault4203());
  }

  // REZEPS-110
  @When("PS fordert Liste der verfügbaren Kartenterminals vom Konnektor an")
  public void determineCardTerminals() throws MissingPreconditionException {
    konnektorGlueCode.determineCardTerminals();
  }

  // REZEPS-111
  @Then("Konnektor sendet GetCardTerminalsResponse mit Status OK und mind. einem Terminal")
  public void checkGetCardTerminalsOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isGetCardTerminalsOk());
  }

  // REZEPS-109
  @When("PS fordert das Stecken des HBAs in das Terminal mit der ID {string} und in Slot {int} an")
  public void requestCard(String ctID, int slot) throws MissingPreconditionException {
    konnektorGlueCode.requestCard(ctID, slot);
  }

  // REZEPS-114
  @Then("Konnektor sendet RequestCardResponse mit Status OK und einem CardHandle")
  public void checkRequestCardOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isRequestCardOk());
  }

  // REZEPS-115
  @When("Testsystem generiert zufällige CodeChallenge")
  public void generateRandomCodeChallenge() {
    idpGlueCode.generateRandomCodeChallenge();
  }

  // REZEPS-117
  @Then("Konnektor antwortet auf ActivateComfortSignature mit Fault 4018")
  public void checkFault4018ForActivateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.isActivateComfortSignatureSoapFault4018();
  }

  @Given("Arbeitsplatz wurde auf {string} gewechselt")
  public void workplaceChange(String arg0) {
    konnektorGlueCode.changeWorkplace(arg0);
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Quittung erneut abrufen")
  public void erpFDAntwortetMitCodeOKNachQuittungErneutAbrufen() {
    Assert.assertTrue(fdClientGlueCode.checkSignedReceiptStatus());
  }

  @When("Nachricht wurde erstellt: {string}, {string}, {string}, {string}")
  public void messageCreatedWith(
      String kvnr, String info_text, String pickUpCodeHR, String pickUpCodeDMC) {
    fdClientGlueCode.determineCommunicationEx(kvnr, info_text, pickUpCodeHR, pickUpCodeDMC);
  }

  // REZEPS-127
  @Then("eRP-FD antwortet mit Code 404 Not Found nach Operation $abort")
  public void answerNotFoundAfterOperationAbort() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNotFound());
  }

  // REZEPS-128
  @Then("eRP-FD antwortet mit Code 409 Conflict nach Operation $abort")
  public void answerConflictAfterOperationAbort() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseConflict());
  }

  // REZEPS-129
  @Then("eRP-FD antwortet mit Code 409 Conflict nach Operation $close")
  public void answerConflictAfterOperationClose() throws MissingPreconditionException {
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseStatusConflict());
  }

  // REZEPS-131
  @When("Primärsystem ändert den Arbeitsplatz auf {string}")
  public void switchWorkplaceInInvocationContext(String workplace)
      throws MissingPreconditionException {
    konnektorGlueCode.switchWorkplace(workplace);
  }
}
