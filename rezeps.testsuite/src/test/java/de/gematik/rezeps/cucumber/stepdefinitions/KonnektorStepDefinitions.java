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
import de.gematik.rezeps.dataexchange.create.MedicationData;
import de.gematik.rezeps.gluecode.FdClientGlueCode;
import de.gematik.rezeps.gluecode.KonnektorGlueCode;
import de.gematik.rezeps.gluecode.MissingPreconditionException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.junit.Assert;
import org.springframework.boot.SpringApplication;

public class KonnektorStepDefinitions {

  private KonnektorGlueCode konnektorGlueCode;
  private FdClientGlueCode fdClientGlueCode;

  @Before()
  public void set_the_stage() throws RemoteException, NotBoundException {
    OnStage.setTheStage(new OnlineCast());
    konnektorGlueCode =
        new KonnektorGlueCode(SpringApplication.run(KonnektorClient.class, new String[] {}));
    fdClientGlueCode = new FdClientGlueCode();
  }

  @Given("Das Handle eines signierenden HBAs liegt vor")
  public void determineCardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle();
  }

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
    konnektorGlueCode.obtainAccessTokenPrescribingEntity();
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

  @Given("Access-Code des E-Rezepts liegt vor")
  public void checkAccessCodeAvailable() throws MissingPreconditionException {
    fdClientGlueCode.checkAccessCodeAvailable();
  }

  @Given("Task-ID des E-Rezepts liegt vor")
  public void ensureTaskId() throws MissingPreconditionException {
    fdClientGlueCode.checkTaskIdAvailable();
  }

  @When("E-Rezept durch Abgebenden abrufen")
  public void acceptByPharmacist() {
    fdClientGlueCode.invokeTaskAccept();
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Operation $accept")
  public void checkAcceptPharmacistOk() {
    Assert.assertTrue(fdClientGlueCode.checkTaskAcceptOk());
  }

  @When("Signatur des E-Rezepts prüfen")
  public void verifySignature() throws MissingPreconditionException {
    konnektorGlueCode.verifySignature();
  }

  @Then("Konnektor schickt VerifyDocument-Response mit Status OK")
  public void checkVerifySignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isValidSignature());
  }

  @Given("Eine Job-ID zum Signieren eines Dokumentes ist verfügbar")
  public void determineJobId() throws MissingPreconditionException {
    konnektorGlueCode.determineJobNumber();
  }

  @When("Quittung abrufen für PZN {string} und Text {string}")
  public void closeTask(String pznValue, String pznText) {
    fdClientGlueCode.invokeTaskClose(new MedicationData(pznValue, pznText));
  }

  @Then("eRP-FD antwortet mit Code 200 OK nach Operation $close")
  public void checkTaskCloseOk() {
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseOk());
  }

  @Given("Access Token des IDP liegt vor (abgebende Praxis)")
  public void obtainAccessTokenDispensingEntity() throws MissingPreconditionException {
    konnektorGlueCode.obtainAccessTokenDispensingEntity();
  }

  @Given("Signaturmodus des HBA ist COMFORT")
  public void determineSignatureModeComfort() throws MissingPreconditionException {
    konnektorGlueCode.determineSignatureModeComfort();
  }

  @When("PS aktiviert Komfortsignatur")
  public void activateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.activateComfortSignature();
  }

  @When("PS deaktiviert Komfortsignatur")
  public void deactivateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.deactivateComfortSignature();
  }

  @Then("Konnektor schickt ActivateComfortSignature-Response mit Status OK")
  public void checkActivateComfortSignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isActivateComfortSignatureOk());
  }

  @Then("Konnektor schickt DeactivateComfortSignature-Response mit Status OK")
  public void checkDeActivateComfortSignatureResponseOk() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isDeactivateComfortSignatureOk());
  }

  @When("PS fragt Signature Mode für HBA ab")
  public void getSignatureMode() throws MissingPreconditionException {
    konnektorGlueCode.determineSignatureMode();
  }

  @Then("Konnektor antwortet mit Signatur-Mode COMFORT")
  public void checkGetSignatureModeResponseComfort() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isSignatureModeComfort());
  }

  @Then("Konnektor-Response enthält keine SessionInfo")
  public void checkGetSignatureModeResponsePin() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isSignatureModePin());
  }

  @When("PS fragt Status der QES-PIN ab")
  public void getPinStatusQes() throws MissingPreconditionException {
    konnektorGlueCode.determinePinStatusQes();
  }

  @Then("Konnektor antwortet mit PIN-Status VERIFIED")
  public void checkGetPinStatusResponseVerified() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  @Then("Konnektor antwortet mit PIN-Status VERIFIABLE")
  public void checkGetPinStatusResponseVerifiable() throws MissingPreconditionException {
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  @Given("Aufrufkontext {string}, {string} und {string} ist gesetzt")
  public void determineContext3Parameter(String mandant, String clientSystem, String workplace)
      throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(mandant, clientSystem, workplace));
  }

  @When("Konnektor ermittelt IP Adresse ")
  public void determineIPAddress(String fqdn) throws MissingPreconditionException {
    konnektorGlueCode.getIPAddress(fqdn);
  }

  @Given("UserId liegt vor")
  public void determineUserId() throws MissingPreconditionException {
    konnektorGlueCode.ensureUserId();
  }

  @When("PS erstellt neue UserID")
  public void createUserId() throws MissingPreconditionException {
    konnektorGlueCode.determineUserId();
  }

  @Given("Das Handle einer SMC-B liegt vor")
  public void determineSMCBCardHandle() throws MissingPreconditionException {
    konnektorGlueCode.determineSmcBHandle();
  }

  @When("PS signiert Challenge mittels externalAuthenticate mit SMC-B")
  public void externalAuthenticate() throws MissingPreconditionException {
    konnektorGlueCode.authenticateExternally();
  }
}
