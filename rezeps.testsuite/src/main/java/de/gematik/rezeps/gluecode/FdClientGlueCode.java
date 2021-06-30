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

package de.gematik.rezeps.gluecode;

import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.dataexchange.*;
import de.gematik.rezeps.fd.FdClient;
import de.gematik.rezeps.fd.TaskValidator;
import de.gematik.rezeps.util.CommonUtils;
import de.gematik.test.erezept.fd.fhir.adapter.CommunicationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/** Ermöglicht die Kommunikation mit dem E-Rezept-Fachdienst. */
public class FdClientGlueCode {

  private static final Logger LOGGER = LoggerFactory.getLogger(FdClientGlueCode.class);
  public static final String ES_WURDE_KEIN_E_REZEPT_ERSTELLT = "Es wurde kein E-Rezept erstellt.";
  public static final String ES_LIEGT_KEIN_ACCESS_TOKEN_VOR = "Es liegt kein AccessToken vor";
  public static final String ES_LIEGEN_KEINE_KOMMUNIKATIONSDATEN_VOR =
      "Es liegen keine Kommunikationsdaten vor.";

  private FdClient fdClient;

  public FdClientGlueCode() {
    fdClient = new FdClient();
  }

  protected FdClientGlueCode(FdClient fdClient) {
    this.fdClient = fdClient;
  }

  /**
   * Ruft den Task create beim E-Rezept Fachdienst auf. Die Daten der Response, die im weiteren
   * Testablauf benötigt werden, werden im Objekt TestcaseData bereitgestellt.
   */
  public void invokeTaskCreate() {
    String accessToken = TestcaseData.getInstance().getAccessTokenDispensingEntity();
    TaskCreateData taskCreateData = fdClient.invokeTaskCreate(accessToken);
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);
  }

  /**
   * Prüft, ob der Task create erfolgreich durchgeführt werden konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskCreatedOk() {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskCreatedOk(taskCreateData);
  }

  /** Ruft den Task activate aus Sicht des verordnenden LEs auf. */
  public void invokeTaskActivate() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskCreateData taskCreateData = testcaseData.getTaskCreateData();
    String taskId = taskCreateData.getTaskId();
    String accessCode = taskCreateData.getAccessCode();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    byte[] signedBundle = testcaseData.getSignDocumentResult().getSignedBundle();
    TaskActivateData taskActivateData =
        fdClient.invokeTaskActivate(taskId, accessToken, accessCode, signedBundle);
    testcaseData.setTaskActivateData(taskActivateData);
  }

  /**
   * Prüft, ob der Task activate aus Sicht des verordnenden LEs erfolgreich durchgeführt werden
   * konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskActivateOk() {
    TaskActivateData taskActivateData = TestcaseData.getInstance().getTaskActivateData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskActivateDataOk(taskActivateData);
  }

  /** Ruft den Task activate aus Sicht des abgebenden LEs auf. */
  public void invokeTaskAccept() {
    try {
      TestcaseData testcaseData = TestcaseData.getInstance();
      TaskCreateData taskCreateData = testcaseData.getTaskCreateData();
      String taskId = taskCreateData.getTaskId();
      String accessCode = taskCreateData.getAccessCode();
      String accessToken = testcaseData.getAccessTokenDispensingEntity();
      TaskAcceptData taskAcceptData = fdClient.invokeTaskAccept(taskId, accessToken, accessCode);
      TestcaseData.getInstance().setTaskAcceptData(taskAcceptData);
    } catch (Exception exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob der Task activate aus Sicht des abgebenden LEs erfolgreich durchgeführt werden
   * konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskAcceptOk() {
    TaskAcceptData taskAcceptData = getTaskAcceptDataFromTestcaseData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskAcceptDataOk(taskAcceptData);
  }

  /** Ruft den Task close aus Sicht des abgebenden Leistungserbringers beim Fachdienst auf. */
  public void invokeTaskClose() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskCreateData taskCreateData = testcaseData.getTaskCreateData();
    Patient patient = testcaseData.getPatient();
    Medication medication = testcaseData.getMedication();
    TaskAcceptData taskAcceptData = getTaskAcceptDataFromTestcaseData();
    String taskId = taskCreateData.getTaskId();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    String secret = taskAcceptData.getSecret();
    String kvnr = patient.getKvnr();
    String pznText = medication.getPznText();
    String pznValue = medication.getPznValue();
    String prescriptionId = taskCreateData.getPrescriptionId();
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    TaskCloseData taskCloseData =
        fdClient.invokeTaskClose(taskId, accessToken, secret, medicationData);
    TestcaseData.getInstance().setTaskCloseData(taskCloseData);
  }

  /**
   * Prüft, ob der Task close aus Sicht des abgebenden LE erfolgreich durchgeführt werden konnte.
   *
   * @return true, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskCloseOk() {
    TaskCloseData taskCloseData = TestcaseData.getInstance().getTaskCloseData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskCloseDataOk(taskCloseData);
  }

  /**
   * Prüft, ob ein AccessCode verfügbar ist.
   *
   * @throws MissingPreconditionException Falls kein AccessCode verfügbar ist.
   */
  public void checkAccessCodeAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    if (taskCreateData == null || StringUtils.isEmpty(taskCreateData.getAccessCode())) {
      throw new MissingPreconditionException("Es liegt kein AccessCode vor");
    }
  }

  /**
   * Prüft, ob eine Task-ID des E-Rezepts verfügbar ist.
   *
   * @throws MissingPreconditionException Falls keine Task-ID des E-Rezepts verfügbar ist
   */
  public void checkTaskIdAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    if (taskCreateData == null || StringUtils.isEmpty(taskCreateData.getTaskId())) {
      throw new MissingPreconditionException("Es liegt keine Task-ID des E-Rezepts vor");
    }
  }

  /** Ruft alle neuen Nachrichten eines Apothekers ab */
  public void invokeCommunicationGetAsPharmacist() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();

    CommunicationGetData communicationGetData = fdClient.invokeCommunicationGet(accessToken);

    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    // suche nach DispReq und speichere die Task-Referenz
    var optionalTaskReference =
        communicationGetData.getCommunications().stream()
            .filter(
                communication ->
                    communication.getType()
                        == CommunicationType.ERX_COMMUNICATION_DISP_REQ) // nur DispenseRequests
            .filter(communication -> communication.getTaskReference() != null) // mit Task-Referenz
            .findFirst();
    optionalTaskReference.ifPresent(
        taskReference ->
            TestcaseData.getInstance().setDispReqTaskReference(taskReference.getTaskReference()));
  }

  /**
   * Prüft, ob in der StatusCode des {@link CommunicationGetData} der Status 200, die Anzahl der
   * Communication Objekte sowie den CommunicationType("CommunicationInfoReq")
   *
   * @return True wenn der StatusCode des {@link CommunicationGetData} der Status 200 ist, ein
   *     Element in der Liste der {@link CommunicationData} Objekte sowie dessen Typ
   *     CommunicationType("CommunicationInfoReq"), sonst false
   */
  public boolean checkGetCommunicationOkInfoReq() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = testcaseData.getCommunicationGetData();
    if (testcaseData.getCommunicationGetData() == null) {
      throw new MissingPreconditionException(ES_LIEGEN_KEINE_KOMMUNIKATIONSDATEN_VOR);
    }

    return communicationGetData.isValidResponse(CommunicationType.ERX_COMMUNICATION_INFO_REQ);
  }

  /**
   * Prüft, ob in der StatusCode des {@link CommunicationGetData} der Status 200, die Anzahl der
   * Communication Objekte sowie den CommunicationType("CommunicationDispReq") und ob die
   * CommunicationDispReq eine Task-Referenz enthält
   *
   * @return True wenn der StatusCode des {@link CommunicationGetData} der Status 200 ist, ein
   *     Element in der Liste der {@link CommunicationData} Objekte sowie dessen Typ
   *     CommunicationType("CommunicationDispReq"), sonst false
   */
  public boolean checkGetCommunicationOkDispReq() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = testcaseData.getCommunicationGetData();
    if (testcaseData.getCommunicationGetData() == null) {
      throw new MissingPreconditionException(ES_LIEGEN_KEINE_KOMMUNIKATIONSDATEN_VOR);
    }
    if (testcaseData.getDispReqTaskReference() == null) {
      throw new MissingPreconditionException(
          "Es liegt keine Task-Referenz aus einer Dispense-Request vor.");
    }

    return communicationGetData.isValidResponse(CommunicationType.ERX_COMMUNICATION_DISP_REQ);
  }

  public void determineCommunication(String kvnrRecipient, String messageText) {

    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage(messageText);
    communicationData.setSender(kvnrRecipient);

    communicationData.setTaskReference(
        TestcaseData.getInstance().getCommunicationGetData().getCommunications().stream()
            .map(CommunicationData::getTaskReference)
            .findFirst()
            .orElse(""));
    TestcaseData.getInstance().setCommunicationSetData(communicationData);
  }

  /**
   * Sendet die Nachricht an den Fachdienst
   *
   * @throws MissingPreconditionException tritt auf, wenn kein AccessToken vorliegt oder kein
   *     Nachrichten Objekt erstellt wurde.
   */
  public void sendCommunication() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    if (CommonUtils.isNullOrEmpty(accessToken)) {
      throw new MissingPreconditionException(ES_LIEGT_KEIN_ACCESS_TOKEN_VOR);
    }
    CommunicationData communicationData = testcaseData.getCommunicationSetData();
    if (communicationData == null) {
      throw new MissingPreconditionException("Es wurde keine Nachricht erstellt.");
    }
    if (CommonUtils.isNullOrEmpty(communicationData.getTaskReference())) {
      throw new MissingPreconditionException("Das Rezept konnte nicht ermittelt werden.");
    }
    CommunicationGetData data =
        fdClient.invokeCommunicationSet(
            testcaseData.getAccessTokenDispensingEntity(), communicationData);
    TestcaseData.getInstance().setCommunicationGetData(data);
  }

  /**
   * Überprüft den Statuscode der gesendeten Nachricht <b>201</b>
   *
   * @return true, wenn Daten vorhanden sind und der Statuscode 201 ist.
   */
  public boolean checkPostCommunicationOkReply() {
    return (TestcaseData.getInstance().getCommunicationGetData() != null)
        && (TestcaseData.getInstance().getCommunicationGetData().isValidStatusCode(201));
  }

  private TaskAcceptData getTaskAcceptDataFromTestcaseData() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    return testcaseData.getTaskAcceptData();
  }
  /** Ruft den Task abort aus Sicht des abgebenden LEs auf. */
  public void invokeTaskAbortByPharmacist() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskAcceptData taskAcceptData = getTaskAcceptDataFromTestcaseData();
    if ((taskAcceptData == null) || (CommonUtils.isNullOrEmpty(taskAcceptData.getTaskId()))) {
      throw new MissingPreconditionException(ES_WURDE_KEIN_E_REZEPT_ERSTELLT);
    }
    String taskId = taskAcceptData.getTaskId();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    String secret = taskAcceptData.getSecret();
    TaskRejectData taskDeleteData =
        fdClient.invokeTaskAbortAsPharmacist(accessToken, taskId, secret);
    testcaseData.setTaskDeleteData(taskDeleteData);
  }

  /** Ruft den Task abort aus Sicht des verordnenden LEs auf. */
  public void invokeTaskAbortByDoctor() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskCreateData taskCreateData = testcaseData.getTaskCreateData();
    if ((taskCreateData == null) || (CommonUtils.isNullOrEmpty(taskCreateData.getTaskId()))) {
      throw new MissingPreconditionException(ES_WURDE_KEIN_E_REZEPT_ERSTELLT);
    }
    String taskId = taskCreateData.getTaskId();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    String accessCode = taskCreateData.getAccessCode();
    TaskRejectData taskDeleteData =
        fdClient.invokeTaskAbortAsDoctor(accessToken, taskId, accessCode);
    testcaseData.setTaskDeleteData(taskDeleteData);
  }

  /**
   * Prüft den StatusCode auf "204 No Content"
   *
   * @return true, wenn der StatusCode 204 ist, andernfalls false
   * @exception MissingPreconditionException {@link MissingPreconditionException} wenn kein $abort
   *     ausgeführt wurde bzw. keine Daten vorliegen
   */
  public boolean checkAbortResponseNoContent() throws MissingPreconditionException {
    return checkResponseCodeOnTaskRejectData(204);
  }
  /**
   * Prüft den StatusCode auf "404 not found"
   *
   * @return true, wenn der StatusCode 404 ist, andernfalls false
   * @exception MissingPreconditionException {@link MissingPreconditionException} wenn kein $abort
   *     ausgeführt wurde bzw. keine Daten vorliegen
   */
  public boolean checkAbortResponseNotFound() throws MissingPreconditionException {
    return checkResponseCodeOnTaskRejectData(404);
  }

  /**
   * Prüft den StatusCode der vorangegangenen Aktion $abort dessen Ergebnis in {@link
   * TestcaseData#getTaskDeleteData()} liegt
   *
   * @param statusCode zu prüfender StatusCode
   * @exception MissingPreconditionException wenn kein <b>$abort</b> ausgeführt wurde bzw. keine
   *     Daten vorliegen
   */
  private boolean checkResponseCodeOnTaskRejectData(int statusCode)
      throws MissingPreconditionException {
    TaskRejectData taskDeleteData = TestcaseData.getInstance().getTaskDeleteData();
    if (taskDeleteData == null) {
      throw new MissingPreconditionException("Es wurde kein E-Rezept gelöscht.");
    }
    return taskDeleteData.isValidStatusCode(statusCode);
  }

  /** Ruft reject für einen Task auf */
  public void invokeTaskReject() throws MissingPreconditionException {
    TaskAcceptData taskAcceptData = getTaskAcceptDataFromTestcaseData();
    if ((taskAcceptData == null) || (CommonUtils.isNullOrEmpty(taskAcceptData.getTaskId()))) {
      throw new MissingPreconditionException(ES_WURDE_KEIN_E_REZEPT_ERSTELLT);
    }
    String taskId = taskAcceptData.getTaskId();
    String secret = taskAcceptData.getSecret();

    String accessToken = TestcaseData.getInstance().getAccessTokenDispensingEntity();
    if (CommonUtils.isNullOrEmpty(accessToken)) {
      throw new MissingPreconditionException(ES_LIEGT_KEIN_ACCESS_TOKEN_VOR);
    }

    TaskRejectData taskRejectData = fdClient.invokeTaskReject(taskId, accessToken, secret);
    TestcaseData.getInstance().setTaskRejectData(taskRejectData);
  }
  /** Prüft den StatusCode auf "204 No Content" */
  public boolean checkRejectResponseNoContent() throws MissingPreconditionException {
    TaskRejectData taskRejectData = TestcaseData.getInstance().getTaskRejectData();
    if (taskRejectData == null) {
      throw new MissingPreconditionException("Es wurde kein E-Rezept zurückgeben.");
    }
    return taskRejectData.isValidStatusCode(204);
  }

  /**
   * Prüft ob TaskCloseDate vorhanden ist und ob diese eine signierte Quittung enthält
   *
   * @throws MissingPreconditionException wenn Task Close vorher nicht ausgeführt wurde
   */
  public void checkSignedPrescriptionAvailableAfterClose() throws MissingPreconditionException {
    TaskCloseData taskCloseData = TestcaseData.getInstance().getTaskCloseData();

    if (taskCloseData == null) {
      throw new MissingPreconditionException("Es wurde kein Task geschlossen");
    }

    if (taskCloseData.getSignedReceipt() == null) {
      throw new MissingPreconditionException(
          "Es wurde kein signiertes Rezept beim Task abschluss hinterlegt");
    }
  }

  /**
   * Prüft, ob eine Quittung erneut abgerufen wurde und der Status 200 ist
   *
   * @return true, wenn der StatusCode 200 ist. False wenn keine Daten vorliegen oder der Code !=
   *     200 ist
   */
  public boolean checkSignedReceiptStatus() {
    if (TestcaseData.getInstance().getDispensedMedicationReceipt() == null) {
      return false;
    }
    return TestcaseData.getInstance().getDispensedMedicationReceipt().getStatusCode() == 200;
  }

  /**
   * Rufe die Quittung eines bereits dispensierten Rezeptes ab (Quittung erneut abrufen)
   *
   * @throws MissingPreconditionException wenn das Rezept noch nicht dispensiert wurde (Task Close)
   */
  public void getDispensedMedicationReceipt() throws MissingPreconditionException {
    checkSignedPrescriptionAvailableAfterClose();
    TestcaseData testcaseData = TestcaseData.getInstance();

    TaskCloseData taskCloseData = testcaseData.getTaskCloseData();
    String prescriptionId = taskCloseData.getSignedReceipt().getPrescriptionId();
    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    String secret = testcaseData.getTaskAcceptData().getSecret();

    SignedReceipt signedReceipt =
        fdClient.invokeDispensedMedicationReceipt(accessToken, prescriptionId, secret);

    testcaseData.setDispensedMedicationReceipt(signedReceipt);
  }

  /**
   * Erstellt eine Nachricht für den Versicherten
   *
   * @param kvnr {@link String} KVNR des Versicherten/Empfängers
   * @param infoText {@link String} freitext
   * @param pickUpCodeHR {@link String} Abholcode
   * @param pickUpCodeDMC {@link String} Abholcode als Datamatrix Code
   */
  public void determineCommunicationEx(
      String kvnr, String infoText, String pickUpCodeHR, String pickUpCodeDMC) {

    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage(infoText);
    communicationData.setDmcCode(pickUpCodeDMC);
    communicationData.setHrPickupCode(pickUpCodeHR);
    communicationData.setSender(kvnr);

    communicationData.setTaskReference(
        TestcaseData.getInstance().getCommunicationGetData().getCommunications().stream()
            .map(CommunicationData::getTaskReference)
            .findFirst()
            .orElse(""));
    TestcaseData.getInstance().setCommunicationSetData(communicationData);
  }

  /**
   * Prüft den StatusCode eines {@link TaskRejectData} Objects auf "409 Conflict"
   *
   * @return true, wenn der StatusCode 409 ist, andernfalls false
   * @exception MissingPreconditionException wenn kein <b>$abort</b> ausgeführt wurde bzw. keine
   *     Daten vorliegen
   */
  public boolean checkAbortResponseConflict() throws MissingPreconditionException {
    return checkResponseCodeOnTaskRejectData(409);
  }

  /**
   * Prüft den StatusCode der vorangegangenen Aktion <b>$close</b> dessen Ergebnis in {@link
   * TestcaseData#getTaskCloseData()} liegt
   *
   * @return true, wenn der StatusCode 409 ist, andernfalls false
   * @exception MissingPreconditionException wenn kein <b>$close</b> ausgeführt wurde bzw. keine
   *     Daten vorliegen
   */
  public boolean checkTaskCloseStatusConflict() throws MissingPreconditionException {
    TaskCloseData taskCloseData = TestcaseData.getInstance().getTaskCloseData();
    if (taskCloseData == null) {
      throw new MissingPreconditionException("Es wurde kein E-Rezept geschlossen.");
    }
    return (409 == taskCloseData.getStatusCode());
  }
}
