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

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.bundle.BundleHelper;
import de.gematik.rezeps.card.PinStatusResult;
import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import de.gematik.rezeps.dataexchange.create.TaskAcceptData;
import de.gematik.rezeps.dataexchange.create.TaskActivateData;
import de.gematik.rezeps.dataexchange.create.TaskCloseData;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.rezeps.signature.SignatureVerificationResult;

/**
 * Ermöglicht den testfallübergreifenden Zugriff auf Daten. Implementiert mittles Sigleton-Desing-
 * Pattern.
 */
public class TestcaseData {

  private static TestcaseData instance;

  private String hbaHandle;
  private String smcBHandle;
  private byte[] autCertificate;
  private byte[] authenticatedData;
  private SignDocumentResult signDocumentResult;
  private String jobNumber;
  private BundleHelper bundle;
  private String accessTokenPrescribingEntity;
  private String accessTokenDispensingEntity;
  private TaskCreateData taskCreateData;
  private TaskActivateData taskActivateData;
  private TaskAcceptData taskAcceptData;
  private TaskCloseData taskCloseData;
  private SignatureVerificationResult signatureVerificationResult;
  private InvocationContext invocationContext;
  private ComfortSignatureResult activateComfortSignatureResult;
  private ComfortSignatureResult getSignatureModeResult;
  private ComfortSignatureResult comfortSignatureDeactivated;
  private byte[] codeChallenge;
  private PinStatusResult pinStatusResult;
  private String fdIpAddress;

  private TestcaseData() {}

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

  public byte[] getAuthenticatedData() {
    return authenticatedData;
  }

  public void setAuthenticatedData(byte[] authenticatedData) {
    this.authenticatedData = authenticatedData;
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
   * Seruzt die IP Adresse des Fachdienstes
   *
   * @param ipAddress Ip Adresse
   */
  public void setFdIpAddress(String ipAddress) {
    this.fdIpAddress = ipAddress;
  }
}
