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

package de.gematik.rezeps.fd;

import de.gematik.idp.token.JsonWebToken;
import de.gematik.rezeps.dataexchange.*;
import de.gematik.test.erezept.fd.actors.ActorType;
import de.gematik.test.erezept.fd.actors.CommunicationActor;
import de.gematik.test.erezept.fd.actors.DoctorActor;
import de.gematik.test.erezept.fd.actors.PharmacistActor;
import de.gematik.test.erezept.fd.actors.response.ResponseEnvelope;
import de.gematik.test.erezept.fd.fhir.adapter.*;
import de.gematik.test.erezept.fd.fhir.helper.Profile;
import de.gematik.test.erezept.keystore.IdentityHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FdClient {

  private static final String EMPTY_STRING = "";

  private String doctorAccessToken;
  private DoctorActor doctorActor;

  private String pharmacistAccessToken;
  private PharmacistActor pharmacistActor;

  private String communicationAccessToken;
  private CommunicationActor communicationActor;

  public FdClient() {
    // empty Constructor description just need for Sonar
  }

  /**
   * Ruft den Task create beim Fachdienst auf.
   *
   * @param accessToken Access Token des Artzes (vom IDP).
   * @return Ergebnis des Aufrufs von Task create, das für die weitere Testdurchführung benötigt
   *     wird.
   */
  public TaskCreateData invokeTaskCreate(String accessToken) {
    DoctorActor actor = getDoctorActor(accessToken);
    ResponseEnvelope prescription = actor.createPrescription();
    TaskCreateData taskCreateData =
        new TaskCreateData(
            prescription.getStatusCode(), EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    if (prescription.getTask().isPresent()) {
      Task task = prescription.getTask().get(); // NOSONAR
      taskCreateData.setTaskId(task.getId());
      taskCreateData.setPrescriptionId(task.getPrescriptionID().getValue());
      taskCreateData.setAccessCode(task.getAccessCode().getValue());
      taskCreateData.setStatus(task.getStatus());
    }

    return taskCreateData;
  }

  /**
   * Ruft den Task activate aus Sicht des verschreibenden LE beim Fachdienst auf.
   *
   * @param taskId Id des Tasks der aktiviert werden soll.
   * @param accessToken Access Token des Artzes (vom IDP).
   * @param accessCode Access Code des Tasks.
   * @param signedBundle Der signierte Verordnungsdatensatz.
   * @return Ergebnis des Tasks activate für die Auswertung
   */
  public TaskActivateData invokeTaskActivate(
      String taskId, String accessToken, String accessCode, byte[] signedBundle) {
    DoctorActor actor = getDoctorActor(accessToken);
    ResponseEnvelope responseEnvelope = actor.issuePrescription(taskId, signedBundle, accessCode);
    TaskActivateData taskActivateData =
        new TaskActivateData(responseEnvelope.getStatusCode(), EMPTY_STRING);
    if (responseEnvelope.getTask().isPresent()) {
      Task task = responseEnvelope.getTask().get(); // NOSONAR
      taskActivateData.setStatus(task.getStatus());
    }

    return taskActivateData;
  }

  /**
   * Ruft den Task accept aus Sicht des abgebenden LE beim Fachdienst auf.
   *
   * @param taskId Id des Tasks.
   * @param accessToken Das vom IDP erhaltene Access Token.
   * @param accessCode Access Code des Tasks.
   * @return Ergebnis des Aufrufs von Task accept, das für die weitere Testdurchführung benötigt
   *     wird.
   */
  public TaskAcceptData invokeTaskAccept(String taskId, String accessToken, String accessCode) {
    PharmacistActor actor = getPharmacistActor(accessToken);
    ResponseEnvelope responseEnvelope = actor.acceptPrescription(taskId, accessCode);
    TaskAcceptData taskAcceptData =
        new TaskAcceptData(responseEnvelope.getStatusCode(), EMPTY_STRING);
    if (responseEnvelope.getTaskAcceptBundle().isPresent()) {
      TaskAcceptBundle taskAcceptBundle = responseEnvelope.getTaskAcceptBundle().get(); // NOSONAR
      Task task = taskAcceptBundle.getTask();
      taskAcceptData.setTaskID(task.getId());
      taskAcceptData.setStatus(task.getStatus());
      taskAcceptData.setSecret(task.getSecret().getValue());
      ErxBinary binary = taskAcceptBundle.getBinary();
      taskAcceptData.setSignedPrescription(binary.getData());
    }

    return taskAcceptData;
  }

  /**
   * Ruft den Task reject aus Sicht des abgebenden LE beim Fachdienst auf.
   *
   * @param taskId Id des Tasks.
   * @param accessToken Das vom IDP erhaltene Access Token.
   * @param secret Secret des Tasks.
   * @return Ergebnis des Aufrufs von Task accept, das für die weitere Testdurchführung benötigt
   *     wird.
   */
  public TaskRejectData invokeTaskReject(String taskId, String accessToken, String secret) {
    PharmacistActor actor = getPharmacistActor(accessToken);
    ResponseEnvelope responseEnvelope = actor.rejectPrescription(taskId, secret);
    return new TaskRejectData(responseEnvelope.getStatusCode());
  }

  /**
   * Ruft den Task close aus Sicht des abgebenden Leistungserbringers auf.
   *
   * @param taskId Id des Tasks.
   * @param accessToken Das vom IDP erhaltene Access Token.
   * @param secret Secret des Tasks.
   * @param medicationData Die Daten des ausgegebenen Medikamentes.
   * @return Ergebnis des Tasks close für die Auswertung und weitere Verarbeitung.
   */
  public TaskCloseData invokeTaskClose(
      String taskId, String accessToken, String secret, MedicationData medicationData) {
    PharmacistActor actor = getPharmacistActor(accessToken);

    Value code = new Value(Profile.CODE_SYSTEM.PZN.getUrl(), medicationData.getPznValue());
    KBV_Medication_PZN kbvMedicationPzn =
        KBV_Medication_PZN.builder().code(code).codeText(medicationData.getPznText()).build();

    JsonWebToken token = new JsonWebToken(accessToken);
    String telematikId = token.extractBodyClaims().get("idNummer").toString();

    Value patient = new Value(Profile.NAMING_SYSTEM.KVID.getUrl(), medicationData.getKvnr());
    Value practitioner = new Value(Profile.NAMING_SYSTEM.TelematikIdNS.getUrl(), telematikId);
    Value prescriptionID =
        new Value(
            Profile.NAMING_SYSTEM.PrescriptionIdNS.getUrl(), medicationData.getPrescriptionId());
    MedicationDispense medicationDispense =
        MedicationDispense.builder()
            .prescriptionID(prescriptionID)
            .patient(patient)
            .practitioner(practitioner)
            .medication(kbvMedicationPzn)
            .supportingInformation("Task/" + taskId)
            .build();

    ResponseEnvelope responseEnvelope =
        actor.dispenseMedication(taskId, secret, medicationDispense);

    int statusCode = responseEnvelope.getStatusCode();
    byte[] signature = null;
    SignedReceipt signedReceipt = null;
    Optional<Receipt> receipt = responseEnvelope.getReceipt();
    if (receipt.isPresent()) {
      signature = receipt.get().getSignature().getData();

      signedReceipt = new SignedReceipt(receipt.get().getPrescriptionID().getValue());
    }

    TaskCloseData taskCloseData = new TaskCloseData(statusCode, signature);
    taskCloseData.setSignedPrescription(signedReceipt);

    return taskCloseData;
  }

  private CommunicationGetData createCommunicationDataFromResponse(ResponseEnvelope response) {
    CommunicationGetData communicationGetData = new CommunicationGetData(response.getStatusCode());
    if (response.getBundleCommunication().isPresent()) {
      BundleCommunication bundleCommunication = response.getBundleCommunication().get(); // NOSONAR
      List<CommunicationData> communications =
          bundleCommunication.getCommunications().stream()
              .map(
                  communication -> {
                    CommunicationData communicationData = new CommunicationData();
                    communicationData.setType(communication.getCommunicationType());
                    communicationData.setTaskReference(communication.getTaskReference());
                    communicationData.setMessage(communication.getMessage());
                    communicationData.setSender(communication.getSender().getValue());
                    return communicationData;
                  })
              .collect(Collectors.toList());
      communicationGetData.setCommunications(communications);
    }
    return communicationGetData;
  }

  /**
   * Sendet eine Nachricht
   *
   * @param accessToken Access Token vom IDP der den User identifiziert.
   * @param dataToSend zu sendende Daten {@link CommunicationData}
   * @return {@link CommunicationGetData} antwort auf die gesendete Nachricht
   */
  public CommunicationGetData invokeCommunicationSet(
      String accessToken, CommunicationData dataToSend) {
    CommunicationActor actor = getCommunicationActor(accessToken);
    Value recipient =
        new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), dataToSend.getSender());

    Map<String, String> replyContent = dataToSend.getReplayContent();
    ResponseEnvelope response =
        actor.sendReplyMessage("/" + dataToSend.getTaskReference(), recipient, replyContent);

    return createCommunicationDataFromResponse(response);
  }

  /**
   * Holt alle ungelesenen Nachrichten eines bestimmten Nutzers vom Fachdienst ab.
   *
   * @param accessToken Access Token vom IDP der den User identifiziert.
   * @return Ergebnis der Operation.
   */
  public CommunicationGetData invokeCommunicationGet(String accessToken) {
    CommunicationActor actor = getCommunicationActor(accessToken);
    ResponseEnvelope response = actor.searchNewCommunications();
    return createCommunicationDataFromResponse(response);
  }

  /**
   * Set the doctor actor.
   *
   * @param doctorAccessToken Access token that corresponds to the new doctor actor.
   * @param doctorActor Doctor actor to be set.
   */
  public void setDoctorActor(String doctorAccessToken, DoctorActor doctorActor) {
    this.doctorAccessToken = doctorAccessToken;
    this.doctorActor = doctorActor;
  }

  /**
   * Get the doctor actor or create a new one if it does not exsists or the corresponding access
   * token has changed.
   *
   * @param accessToken Corresponding access token.
   * @return Doctor actor.
   */
  private DoctorActor getDoctorActor(String accessToken) {
    if (doctorActor == null || !doctorAccessToken.equals(accessToken)) {
      doctorActor = new DoctorActor(IdentityHelper.getHbaArztRsa2048());
      doctorActor.idpLogin(new StaticTokenIdpClient(accessToken));
      doctorAccessToken = accessToken;
    }

    return doctorActor;
  }

  /**
   * Set the pharmacist actor.
   *
   * @param pharmacistAccessToken Access token that corresponds to the new pharmacist actor.
   * @param pharmacistActor Pharmacist actor to be set.
   */
  public void setPharmacistActor(String pharmacistAccessToken, PharmacistActor pharmacistActor) {
    this.pharmacistAccessToken = pharmacistAccessToken;
    this.pharmacistActor = pharmacistActor;
  }

  /**
   * Get the pharmacist actor or create a new one if it does not exsists or the corresponding access
   * token has changed.
   *
   * @param accessToken Corresponding access token.
   * @return Pharmacist actor.
   */
  private PharmacistActor getPharmacistActor(String accessToken) {
    if (pharmacistActor == null || !pharmacistAccessToken.equals(accessToken)) {
      pharmacistActor = new PharmacistActor(IdentityHelper.getSmcbApothekeRsa2048());
      pharmacistActor.idpLogin(new StaticTokenIdpClient(accessToken));
      pharmacistAccessToken = accessToken;
    }

    return pharmacistActor;
  }

  /**
   * Set the communication actor.
   *
   * @param communicationAccessToken Access token that corresponds to the new communication actor.
   * @param communicationActor Communication actor to be set.
   */
  public void setCommunicationActor(
      String communicationAccessToken, CommunicationActor communicationActor) {
    this.communicationAccessToken = communicationAccessToken;
    this.communicationActor = communicationActor;
  }

  /**
   * Get the communication actor or create a new one if it does not exists or the corresponding
   * access token has changed.
   *
   * @param accessToken Corresponding access token.
   * @return Communication actor.
   */
  private CommunicationActor getCommunicationActor(String accessToken) {
    if (communicationActor == null || !communicationAccessToken.equals(accessToken)) {
      communicationActor =
          new CommunicationActor(ActorType.Pharmacist, IdentityHelper.getSmcbApothekeRsa2048());
      communicationActor.idpLogin(new StaticTokenIdpClient(accessToken));
      communicationAccessToken = accessToken;
    }

    return communicationActor;
  }

  /**
   * Ruft den Task $abort am Fachdienst auf
   *
   * @param accessToken Access Token des
   * @param taskId Id des Tasks.
   * @param secret {@link String} Secret des Apothekers
   * @return {@link TaskRejectData} mit Informationen über StatusCode der Aktion
   */
  public TaskRejectData invokeTaskAbortAsPharmacist(
      String accessToken, String taskId, String secret) {
    this.pharmacistActor = getPharmacistActor(accessToken);
    ResponseEnvelope responseEnvelope = pharmacistActor.deletePrescription(taskId, secret);
    return new TaskRejectData(responseEnvelope.getStatusCode());
  }

  /**
   * Ruft den Task $abort am Fachdienst auf
   *
   * @param accessToken {@link String} AccessToken des Arztes
   * @param taskId Id des Tasks.
   * @param accessCode accessCode des Rezepts
   * @return {@link TaskRejectData} mit Informationen über StatusCode der Aktion
   */
  public TaskRejectData invokeTaskAbortAsDoctor(
      String accessToken, String taskId, String accessCode) {
    this.doctorActor = getDoctorActor(accessToken);
    ResponseEnvelope responseEnvelope = doctorActor.deletePrescription(taskId, accessCode);
    return new TaskRejectData(responseEnvelope.getStatusCode());
  }

  /**
   * Ruft die Quittung eines bereits dispensierten Rezeptes (erneutes abrufen) als Apotheker ab
   *
   * @param prescriptionId {@link String} ID des Rezeptes
   */
  public SignedReceipt invokeDispensedMedicationReceipt(
      final String accessToken, final String prescriptionId, final String secret) {
    this.pharmacistActor = getPharmacistActor(accessToken);

    Value secretValue = new Value(Profile.NAMING_SYSTEM.SecretNS.getCanonicalUrl(), secret);

    ResponseEnvelope response = this.pharmacistActor.getReceiptBundle(prescriptionId, secretValue);
    ReceiptBundle fdReceiptBundle = response.getReceiptBundle().orElseThrow();

    return new SignedReceipt(
        response.getStatusCode(), fdReceiptBundle.getReceipt().getPrescriptionID().getValue());
  }
}
