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

import de.gematik.idp.client.IdpTokenResult;
import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.authentication.ExternalAuthenticateResult;
import de.gematik.rezeps.bundle.BundleHelper;
import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.card.PinStatusResult;
import de.gematik.rezeps.card.VerifyPinResult;
import de.gematik.rezeps.cardterminal.EjectCardResult;
import de.gematik.rezeps.cardterminal.GetCardTerminalsResult;
import de.gematik.rezeps.cardterminal.RequestCardResult;
import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import de.gematik.rezeps.dataexchange.*;
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.rezeps.signature.SignatureVerificationResult;
import java.io.*;
import javax.annotation.Nullable;
import org.slf4j.LoggerFactory;

/**
 * Ermöglicht den testfallübergreifenden Zugriff auf Daten. Implementiert mittles Sigleton-Desing-
 * Pattern.
 */
public class TestcaseData implements Serializable {

  private static final long serialVersionUID = 287754925752415443L;
  private static TestcaseData instance;

  private transient String hbaHandle;
  private transient String smcBHandle;
  private transient byte[] autCertificate;
  private SignDocumentResult signDocumentResult;
  private transient String jobNumber;
  private transient BundleHelper bundle;

  private String accessTokenPrescribingEntity;
  private String accessTokenDispensingEntity;
  private TaskCreateData taskCreateData;
  private TaskActivateData taskActivateData;
  private TaskAcceptData taskAcceptData;
  private TaskCloseData taskCloseData;
  private SignedReceipt dispensedMedicationReceipt;
  private SignatureVerificationResult signatureVerificationResult;
  private InvocationContext invocationContext;

  private transient ComfortSignatureResult activateComfortSignatureResult;
  private transient ComfortSignatureResult getSignatureModeResult;
  private transient ComfortSignatureResult comfortSignatureDeactivated;
  private transient byte[] codeChallenge;
  private transient PinStatusResult pinStatusResult;
  private transient String fdIpAddress;
  private transient VerifyPinResult verifyPinResult;
  private transient EjectCardResult ejectCardResult;
  private transient RequestCardResult requestCardResult;
  private Patient patient;
  private Medication medication;
  private transient GetCardTerminalsResult getCardTerminalsResult;
  private transient IdpTokenResult idpTokenResult;
  private CommunicationGetData communicationGetData;
  private CommunicationData communicationSetData;
  private transient ExternalAuthenticateResult externalAuthenticateResult;
  private TaskRejectData taskDeleteData;
  private TaskRejectData taskRejectData;

  /** Enthält Daten die an den Fachdienst gesendet werden sollen. */
  public CommunicationData getCommunicationSetData() {
    return communicationSetData;
  }

  public void setCommunicationSetData(CommunicationData communicationSetData) {
    this.communicationSetData = communicationSetData;
  }

  /**
   * Wenn der Apotheker eine DispenseRequest erhält, dann wird hier die darin enthaltene
   * Task-Referenz (enthält TaskID und den AccessCode) aus dem Feld basedOn abgelegt
   *
   * @see <a
   *     href="https://build.top.local/source/git/spezifikation/erp/api-erp/-/blob/master/docs/erp_communication.adoc#user-content-anwendungsfall-ein-e-rezept-verbindlich-einer-apotheke-zuweisen">
   *     API Dokumentation: Ein E-Rezept verbindlich einer Apotheke zuweisen</a>
   */
  private String dispReqTaskReference;

  public TestcaseData() {
    super();
  }

  public String getHbaHandle() {
    return hbaHandle;
  }

  public void setHbaHandle(String hbaHandle) {
    this.hbaHandle = hbaHandle;
  }

  public String getSmcBHandle() {
    return smcBHandle;
  }

  public void setSmcBHandle(String smcBHandle) {
    this.smcBHandle = smcBHandle;
  }

  public byte[] getAutCertificate() {
    return autCertificate;
  }

  public void setAutCertificate(byte[] autCertificate) {
    this.autCertificate = autCertificate;
  }

  public SignDocumentResult getSignDocumentResult() {
    return signDocumentResult;
  }

  public void setSignDocumentResult(SignDocumentResult signDocumentResult) {
    this.signDocumentResult = signDocumentResult;
  }

  public String getJobNumber() {
    return jobNumber;
  }

  public void setJobNumber(String jobNumber) {
    this.jobNumber = jobNumber;
  }

  public static void setInstance(TestcaseData instance) {
    TestcaseData.instance = instance;
  }

  public BundleHelper getBundle() {
    return bundle;
  }

  public void setBundle(BundleHelper bundle) {
    this.bundle = bundle;
  }

  public String getAccessTokenPrescribingEntity() {
    return accessTokenPrescribingEntity;
  }

  public void setAccessTokenPrescribingEntity(String accessTokenPrescribingEntity) {
    this.accessTokenPrescribingEntity = accessTokenPrescribingEntity;
  }

  public String getAccessTokenDispensingEntity() {
    return accessTokenDispensingEntity;
  }

  public void setAccessTokenDispensingEntity(String accessTokenDispensingEntity) {
    this.accessTokenDispensingEntity = accessTokenDispensingEntity;
  }

  public TaskCreateData getTaskCreateData() {
    return taskCreateData;
  }

  public void setTaskCreateData(TaskCreateData taskCreateData) {
    this.taskCreateData = taskCreateData;
  }

  public TaskActivateData getTaskActivateData() {
    return taskActivateData;
  }

  public void setTaskActivateData(TaskActivateData taskActivateData) {
    this.taskActivateData = taskActivateData;
  }

  public TaskAcceptData getTaskAcceptData() {
    return taskAcceptData;
  }

  public void setTaskAcceptData(TaskAcceptData taskAcceptData) {
    this.taskAcceptData = taskAcceptData;
  }

  public TaskCloseData getTaskCloseData() {
    return taskCloseData;
  }

  public void setTaskCloseData(TaskCloseData taskCloseData) {
    this.taskCloseData = taskCloseData;
  }

  /**
   * setzt den Aufrufkontext für das Testdatenobjekt
   *
   * @param context {@link InvocationContext}
   */
  public void setInvocationContext(InvocationContext context) {
    this.invocationContext = context;
  }

  /**
   * Gibt den aktuellen Aufrufkontext für dieses Testdatenobjekt zurück.
   *
   * @return der Aufrufkontext {@link InvocationContext}
   */
  public InvocationContext getInvocationContext() {
    return this.invocationContext;
  }

  public SignatureVerificationResult getSignatureVerificationResult() {
    return signatureVerificationResult;
  }

  public void setSignatureVerificationResult(
      SignatureVerificationResult signatureVerificationResult) {
    this.signatureVerificationResult = signatureVerificationResult;
  }

  public ComfortSignatureResult getActivateComfortSignatureResult() {
    return activateComfortSignatureResult;
  }

  public void setActivateComfortSignatureResult(
      ComfortSignatureResult activateComfortSignatureResult) {
    this.activateComfortSignatureResult = activateComfortSignatureResult;
  }

  public ComfortSignatureResult getGetSignatureModeResult() {
    return getSignatureModeResult;
  }

  public void setGetSignatureModeResult(ComfortSignatureResult getSignatureModeResult) {
    this.getSignatureModeResult = getSignatureModeResult;
  }

  public ComfortSignatureResult getComfortSignatureDeactivated() {
    return this.comfortSignatureDeactivated;
  }

  public void setComfortSignatureDeactivated(ComfortSignatureResult comfortSignatureResult) {
    this.comfortSignatureDeactivated = comfortSignatureResult;
  }

  public byte[] getCodeChallenge() {
    return codeChallenge;
  }

  public void setCodeChallenge(byte[] codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  /**
   * Konstruktor erstellt ein {@link TestcaseData} Objekt.
   *
   * @return Instanz der {@link TestcaseData}.
   */
  public static TestcaseData getInstance() {
    if (instance == null) {
      instance = new TestcaseData();
    }
    return instance;
  }

  /**
   * Gibt ein Objekt mit relevanten Informationen über den PinStatus zurück
   *
   * @return {@link PinStatusResult}
   */
  public PinStatusResult getPinStatusResult() {
    return pinStatusResult;
  }

  /**
   * Setzt Informationen zum PinStatus
   *
   * @param pinStatusResult {@link PinStatusResult}
   */
  public void setPinStatusResult(PinStatusResult pinStatusResult) {
    this.pinStatusResult = pinStatusResult;
  }

  /**
   * Gibt die IP Adresse des Fachdienstes welche durch den Konnektor ermittelt wurde zurück
   *
   * @return {@link String} IP Adresse des Fachdienstes
   */
  public String getFdIpAddress() {
    return fdIpAddress;
  }

  /**
   * Setzt die IP Adresse des Fachdienstes
   *
   * @param ipAddress Ip Adresse
   */
  public void setFdIpAddress(String ipAddress) {
    this.fdIpAddress = ipAddress;
  }

  public VerifyPinResult getVerifyPinResult() {
    return verifyPinResult;
  }

  public void setVerifyPinResult(VerifyPinResult verifyPinResult) {
    this.verifyPinResult = verifyPinResult;
  }

  public EjectCardResult getEjectCardResult() {
    return ejectCardResult;
  }

  public void setEjectCardResult(EjectCardResult ejectCardResult) {
    this.ejectCardResult = ejectCardResult;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public Medication getMedication() {
    return medication;
  }

  public void setMedication(Medication medication) {
    this.medication = medication;
  }

  public IdpTokenResult getIdpTokenResult() {
    return idpTokenResult;
  }

  public void setIdpTokenResult(IdpTokenResult idpTokenResult) {
    this.idpTokenResult = idpTokenResult;
  }

  public GetCardTerminalsResult getGetCardTerminalsResult() {
    return getCardTerminalsResult;
  }

  public void setGetCardTerminalsResult(GetCardTerminalsResult getCardTerminalsResult) {
    this.getCardTerminalsResult = getCardTerminalsResult;
  }

  /**
   * read the stored TestcaseData object from file the TestcaseData instance will replaced
   *
   * @param filePath filepath to
   */
  public void deserializeFromFile(String filePath) {
    try {
      try (FileInputStream fis = new FileInputStream(filePath)) {
        try (ObjectInputStream in = new ObjectInputStream(fis)) {
          TestcaseData newInstance = (TestcaseData) in.readObject(); // NOSONAR
          setInstance(newInstance);
        }
      }
    } catch (Exception exception) {
      LoggerFactory.getLogger(TestcaseData.class)
          .error("Error: read or get TestCaseData : {} ", exception.getMessage());
    }
  }

  /**
   * store TestcaseData object to file
   *
   * @param filePath filepath to store
   */
  public void serializeToFile(String filePath) {
    try {
      try (FileOutputStream fos = new FileOutputStream(filePath)) {
        try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
          out.writeObject(getInstance());
          out.flush();
        }
      }
    } catch (Exception exception) {
      LoggerFactory.getLogger(TestcaseData.class)
          .error("Error: write TestCaseData : {} ", exception.getMessage());
    }
  }

  public RequestCardResult getRequestCardResult() {
    return requestCardResult;
  }

  public void setRequestCardResult(RequestCardResult requestCardResult) {
    this.requestCardResult = requestCardResult;
  }

  public CommunicationGetData getCommunicationGetData() {
    return communicationGetData;
  }

  public void setCommunicationGetData(CommunicationGetData communicationGetData) {
    this.communicationGetData = communicationGetData;
  }

  /**
   * Task-Referenz aus der DispReq
   *
   * @return Task-Referenz (Task-ID und AccessCode) wenn DispReq bereits erhalten oder null
   * @see <a
   *     href="https://build.top.local/source/git/spezifikation/erp/api-erp/-/blob/master/docs/erp_communication.adoc#user-content-anwendungsfall-ein-e-rezept-verbindlich-einer-apotheke-zuweisen">
   *     API Dokumentation: Ein E-Rezept verbindlich einer Apotheke zuweisen</a>
   */
  public String getDispReqTaskReference() {
    return dispReqTaskReference;
  }

  public @Nullable String getTaskIdFromDispReqTaskReference() {
    String taskId = null;
    if (dispReqTaskReference != null) {
      // Task-Reference example:
      // Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea
      var tokens = dispReqTaskReference.split("/");
      if (tokens.length >= 2) {
        taskId = tokens[1];
      }
    }
    return taskId;
  }

  public @Nullable String getAccessCodeFromDispReqTaskReference() {
    String accessCode = null;
    if (dispReqTaskReference != null) {
      // Task-Reference example:
      // Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea
      var tokens = dispReqTaskReference.split("/");
      if (tokens.length >= 3) {
        accessCode = tokens[2].replace("$accept?ac=", "");
      }
    }
    return accessCode;
  }

  /**
   * Setze die Task-Referenz aus einer DispReq Nachricht
   *
   * @param dispReqTaskReference ist die Task-Referenz mit Task-ID und AccessCode
   * @see <a
   *     href="https://build.top.local/source/git/spezifikation/erp/api-erp/-/blob/master/docs/erp_communication.adoc#user-content-anwendungsfall-ein-e-rezept-verbindlich-einer-apotheke-zuweisen">
   *     API Dokumentation: Ein E-Rezept verbindlich einer Apotheke zuweisen</a>
   */
  public void setDispReqTaskReference(final String dispReqTaskReference) {
    this.dispReqTaskReference = dispReqTaskReference;
  }

  public ExternalAuthenticateResult getExternalAuthenticateResult() {
    return externalAuthenticateResult;
  }

  public void setExternalAuthenticateResult(ExternalAuthenticateResult externalAuthenticateResult) {
    this.externalAuthenticateResult = externalAuthenticateResult;
  }

  public void setTaskDeleteData(TaskRejectData taskDeleteData) {
    this.taskDeleteData = taskDeleteData;
  }

  public TaskRejectData getTaskDeleteData() {
    return this.taskDeleteData;
  }

  public TaskRejectData getTaskRejectData() {
    return this.taskRejectData;
  }

  public void setTaskRejectData(TaskRejectData taskRejectData) {
    this.taskRejectData = taskRejectData;
  }

  /**
   * Rufe eine signierte Quittung ab, welche erneut abgerufen wurde und nicht über Task/$close
   * erhalten wurde (Quittung erneut abrufen)
   *
   * @return signierte Quittung oder null wenn diese noch nicht abgerufen wurde
   */
  public SignedReceipt getDispensedMedicationReceipt() {
    return this.dispensedMedicationReceipt;
  }

  public void setDispensedMedicationReceipt(SignedReceipt signedReceipt) {
    this.dispensedMedicationReceipt = signedReceipt;
  }
}
