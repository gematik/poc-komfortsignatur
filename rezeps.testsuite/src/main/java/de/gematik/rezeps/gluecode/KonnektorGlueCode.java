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

import de.gematik.rezeps.ConfigurationReader;
import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.authentication.ExternalAuthenticateResult;
import de.gematik.rezeps.authentication.ExternalAuthenticator;
import de.gematik.rezeps.bundle.BundleHelper;
import de.gematik.rezeps.bundle.Coverage;
import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.card.CardHandleFinder;
import de.gematik.rezeps.card.PinStatus;
import de.gematik.rezeps.card.PinStatusResult;
import de.gematik.rezeps.card.PinVerifier;
import de.gematik.rezeps.card.VerifyPinResult;
import de.gematik.rezeps.cardterminal.CardEjector;
import de.gematik.rezeps.cardterminal.CardRequestor;
import de.gematik.rezeps.cardterminal.CardTerminalsGetter;
import de.gematik.rezeps.cardterminal.EjectCardResult;
import de.gematik.rezeps.cardterminal.GetCardTerminalsResult;
import de.gematik.rezeps.cardterminal.RequestCardResult;
import de.gematik.rezeps.certificate.CardCertificateReader;
import de.gematik.rezeps.comfortsignature.ComfortSignatureActivator;
import de.gematik.rezeps.comfortsignature.ComfortSignatureDeactivator;
import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import de.gematik.rezeps.comfortsignature.SignatureModeGetter;
import de.gematik.rezeps.dataexchange.TaskAcceptData;
import de.gematik.rezeps.dataexchange.TaskCreateData;
import de.gematik.rezeps.service.IPUtil;
import de.gematik.rezeps.signature.JobNumberFinder;
import de.gematik.rezeps.signature.PrescriptionSigner;
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.rezeps.signature.SignatureVerification;
import de.gematik.rezeps.signature.SignatureVerificationResult;
import de.gematik.rezeps.util.CommonUtils;
import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

/** Glue-Code zwischen Cucumber und Java. */
public class KonnektorGlueCode {

  private static final Logger LOGGER = LoggerFactory.getLogger(KonnektorGlueCode.class);

  private static final String PIN_QES = "PIN.QES";

  private ConfigurableApplicationContext applicationContext;

  public KonnektorGlueCode(ConfigurableApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  private void checkInvocationContext(InvocationContext invocationContext)
      throws MissingPreconditionException {
    if (null == invocationContext) {
      throw new MissingPreconditionException("Aufrufkontext ist nicht gesetzt");
    } else if (!invocationContext.isValidInvocationContext()) {
      throw new MissingPreconditionException(
          MessageFormat.format(
              "Aufrufkontext ist unvollständig oder invalid. Mandant: {0},"
                  + " ClientSystem:{1}, Arbeitsplatz:{2}",
              invocationContext.getMandant(),
              invocationContext.getClientSystem(),
              invocationContext.getWorkplace()));
    }
  }

  /**
   * Bestimmt das Handle des im Testfall verwendeten HBAs und stellt dieses für weitere Testschritte
   * im Objekt TestcaseData zur Verfügung.
   */
  public void determineHbaHandle() throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = cardHandleFinder.determineHbaHandle(invocationContext);
      TestcaseData.getInstance().setHbaHandle(cardHandle);
      LOGGER.info("Got HBA handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }
  /**
   * Bestimmt das Handle des im Testfall verwendeten <b>bestimmten</b> HBAs und stellt dieses für
   * weitere Testschritte im Objekt TestcaseData zur Verfügung.
   *
   * @param iccsn die ICCSN der Karte
   */
  public void determineHbaHandle(String iccsn) throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = cardHandleFinder.determineHbaHandle(invocationContext, iccsn);
      TestcaseData.getInstance().setHbaHandle(cardHandle);
      LOGGER.info("Got HBA handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten SMC-B und stellt dieses für weitere
   * Testschritte im Objekt TestcaseData zur Verfügung.
   */
  public void determineSmcBHandle() throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = cardHandleFinder.determineSmcBHandle(invocationContext);
      TestcaseData.getInstance().setSmcBHandle(cardHandle);
      LOGGER.info("Got SMC-B handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten ZOD_2_0 und stellt dieses für weitere
   * Testschritte im Objekt TestcaseData zur Verfügung.
   *
   * @param iccsn
   */
  public void determineZOD20Handle(String iccsn) throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = null;
      if (!CommonUtils.isNullOrEmpty(iccsn)) {
        cardHandle = cardHandleFinder.determineZOD20Handle(invocationContext, iccsn);
      } else {
        cardHandle = cardHandleFinder.determineZOD20Handle(invocationContext);
      }
      TestcaseData.getInstance().setHbaHandle(cardHandle);
      LOGGER.info("Got ZOD_2_0 handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten ZOD_2_0 und stellt dieses für weitere
   * Testschritte im Objekt TestcaseData zur Verfügung.
   */
  public void determineZOD20Handle() throws MissingPreconditionException {
    determineZOD20Handle(null);
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten ZOD_2_0 und stellt dieses für weitere
   * Testschritte im Objekt TestcaseData zur Verfügung.
   *
   * @param iccsn die ICCSN der Karte
   */
  public void determineHBAqSigHandle(String iccsn) throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = null;
      if (CommonUtils.isNullOrEmpty(iccsn)) {
        cardHandle = cardHandleFinder.determineHBAQSigHandle(invocationContext);
      } else {
        cardHandle = cardHandleFinder.determineHBAQSigHandle(invocationContext, iccsn);
      }
      TestcaseData.getInstance().setHbaHandle(cardHandle);
      LOGGER.info("Got HBA_Q_SIG handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten ZOD_2_0 und stellt dieses für weitere
   * Testschritte im Objekt TestcaseData zur Verfügung.
   */
  public void determineHBAqSigHandle() throws MissingPreconditionException {
    determineHBAqSigHandle(null);
  }

  /**
   * Bestimmt das Handle der im Testfall verwendeten <b>bestimmten</b> SMC-B und stellt dieses für
   * weitere Testschritte im Objekt TestcaseData zur Verfügung.
   *
   * @param iccsn die ICCSN der Karte
   */
  public void determineSmcBHandle(String iccsn) throws MissingPreconditionException {
    try {
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);

      CardHandleFinder cardHandleFinder =
          applicationContext.getBeanFactory().getBean(CardHandleFinder.class);
      String cardHandle = cardHandleFinder.determineSmcBHandle(invocationContext, iccsn);
      TestcaseData.getInstance().setSmcBHandle(cardHandle);
      LOGGER.info("Got SMC-B handle: {}", cardHandle);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Setzt den Aufrufkontext innerhalb der Testdaten bestehend aus mandant, clientSystem, workplace,
   * userID
   *
   * @param invocationContext ein gültiger {@link InvocationContext}
   */
  public void determineContext(InvocationContext invocationContext)
      throws MissingPreconditionException {
    checkInvocationContext(invocationContext);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
  }

  /**
   * Signiert einen Verordnungsdatensatz mittels eines HBAs. Der Verordnungsdatensatz und das
   * CardHandle des signierenden HBAs stehen in der Klasse TestcaseData zur Verfügung.
   */
  public void signPrescription() throws MissingPreconditionException {
    try {
      PrescriptionSigner prescriptionSigner =
          applicationContext.getBeanFactory().getBean(PrescriptionSigner.class);
      checkPreconditions();
      TestcaseData testcaseData = TestcaseData.getInstance();
      String cardHandle = testcaseData.getHbaHandle();
      String prescription = testcaseData.getBundle().readBundle();
      String jobNumber = testcaseData.getJobNumber();
      checkInvocationContext(testcaseData.getInvocationContext());
      SignDocumentResult signDocumentResult =
          prescriptionSigner.performSignPrescription(
              testcaseData.getInvocationContext(), cardHandle, prescription, jobNumber);
      TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);
    } catch (IOException | TransformerException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  protected void checkPreconditions() throws MissingPreconditionException, TransformerException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    if (StringUtils.isEmpty(testcaseData.getHbaHandle())) {
      throw new MissingPreconditionException(
          "Es wurde kein Handle für den signierende HBA ermittelt.");
    }
    BundleHelper bundleHelper = testcaseData.getBundle();
    if (bundleHelper == null || StringUtils.isEmpty(bundleHelper.readBundle())) {
      throw new MissingPreconditionException("Es wurde kein Verordnungsdatensatz erstellt.");
    }
    if (StringUtils.isEmpty(testcaseData.getJobNumber())) {
      throw new MissingPreconditionException(
          "Es wurde keine Job-Nummer für die Signatur ermittelt.");
    }
  }

  /**
   * Bestimmt die Job-Nummer für die nächste Signatur eines Dokumentes. Die Job-Nummer wird im
   * Objekt TestcaseData für die weitere Verarbeitung zur Verfügung gestellt.
   */
  public void determineJobNumber() throws MissingPreconditionException {
    try {
      JobNumberFinder jobNumberFinder =
          applicationContext.getBeanFactory().getBean(JobNumberFinder.class);
      checkInvocationContext(TestcaseData.getInstance().getInvocationContext());
      String jobNumber =
          jobNumberFinder.performGetJobNumber(TestcaseData.getInstance().getInvocationContext());
      TestcaseData.getInstance().setJobNumber(jobNumber);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft die Signatur eine E-Rezeptes. Das Ergebnis der Prüfung wird im Objekt TestcaseData für
   * die weitere Verarbeitung zur Verfügung gestellt.
   */
  public void verifySignature() throws MissingPreconditionException {
    try {
      SignatureVerification signatureVerification =
          applicationContext.getBeanFactory().getBean(SignatureVerification.class);
      TaskAcceptData taskAcceptData = TestcaseData.getInstance().getTaskAcceptData();
      if (taskAcceptData == null) {
        throw new MissingPreconditionException("Es liegt kein E-Rezept Datensatz vor.");
      }
      InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
      checkInvocationContext(invocationContext);
      SignatureVerificationResult signatureVerificationResult =
          signatureVerification.verifySignature(
              invocationContext, taskAcceptData.getSignedPrescription());
      TestcaseData.getInstance().setSignatureVerificationResult(signatureVerificationResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Trägt die Patient-Daten in das Bundle-Template ein. Anschließend wird das Bundle im Objekt
   * TestcaseData für die weiter Verarbeitung bereitgestellt.
   *
   * @param patient Enthält die Patient-Daten.
   */
  public void modifyBundleWithPatientData(Patient patient) {
    try {
      TestcaseData.getInstance().setPatient(patient);

      BundleHelper bundle = determineBundle();
      bundle.initializePatientData(patient);
    } catch (IOException
        | ParserConfigurationException
        | SAXException
        | XPathExpressionException
        | TransformerException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  private BundleHelper determineBundle()
      throws ParserConfigurationException, SAXException, IOException {
    BundleHelper bundle = TestcaseData.getInstance().getBundle();
    if (bundle == null) {
      bundle = new BundleHelper();
      TestcaseData.getInstance().setBundle(bundle);
    }
    return bundle;
  }

  /**
   * Trägt die Coverage-Daten in das Bundle-Template ein. Anschließend wird das Bundle im Objekt
   * TestcaseData für die weiter Verarbeitung bereitgestellt.
   *
   * @param coverage Enthält die Coverage-Daten.
   */
  public void modifyBundleWithCoverageData(Coverage coverage) {
    try {
      BundleHelper bundle = determineBundle();
      bundle.initializeCoverageData(coverage);
    } catch (IOException
        | ParserConfigurationException
        | SAXException
        | XPathExpressionException
        | TransformerException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Trägt die Medication-Daten in das Bundle-Template ein. Anschließend wird das Bundle im Objekt
   * TestcaseData für die weiter Verarbeitung bereitgestellt.
   *
   * @param medication Enthält die Medication-Daten.
   */
  public void modifyBundleWithMedicationData(Medication medication) {
    try {
      TestcaseData.getInstance().setMedication(medication);

      BundleHelper bundle = determineBundle();
      bundle.initializeMedicationData(medication);
    } catch (IOException
        | ParserConfigurationException
        | SAXException
        | XPathExpressionException
        | TransformerException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Ermittelt das Access Token eines verordnenden PS. Im MVP wird das Token als Konfigurations-
   * Parameter ausgelesen und im Objekt TestcaseData für die weitere Verarbeitung zur Verfügung
   * gestellt.
   */
  public void obtainAccessTokenPrescribingEntity() throws MissingPreconditionException {
    try {
      String accessTokenPrescribingEntity =
          ConfigurationReader.getInstance().getAccessTokenPrescribingEntity();
      if (StringUtils.isEmpty(accessTokenPrescribingEntity)) {
        throw new MissingPreconditionException(
            "Es wurde kein Access Token für das verordnende Primärsystem konfiguriert.");
      }
      TestcaseData.getInstance().setAccessTokenPrescribingEntity(accessTokenPrescribingEntity);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Ermittelt das Access Token eines abgebenden PS. Im MVP wird das Token als Konfigurations-
   * Parameter ausgelesen und im Objekt TestcaseData für die weitere Verarbeitung zur Verfügung
   * gestellt.
   */
  public void obtainAccessTokenDispensingEntity() throws MissingPreconditionException {
    try {
      String accessTokenDispensingEntity =
          ConfigurationReader.getInstance().getAccessTokenDispensingEntity();
      if (StringUtils.isEmpty(accessTokenDispensingEntity)) {
        throw new MissingPreconditionException(
            "Es wurde kein Access Token für das abgebende Primärsystem konfiguriert.");
      }
      TestcaseData.getInstance().setAccessTokenDispensingEntity(accessTokenDispensingEntity);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Ergänzt die Daten aus dem Task create im Verordnungsdatensatz.
   *
   * @throws MissingPreconditionException Falls noch kein Task vorliegt.
   */
  public void modifyBundleWithTaskData() throws MissingPreconditionException {
    try {
      BundleHelper bundle = determineBundle();
      TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
      if (taskCreateData == null) {
        throw new MissingPreconditionException("Es liegt noch kein Task vor.");
      }
      bundle.initializeTaskCreateData(taskCreateData);
    } catch (IOException
        | ParserConfigurationException
        | SAXException
        | XPathExpressionException
        | TransformerException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob die letzte durchgeführte Signaturprüfung erfolgreich war.
   *
   * @return True, wenn die letzte durchgeführte Signaturprüfung erfolgreich war, andernfalls false.
   * @throws MissingPreconditionException Falls kein Ergebnis einer Signaturprüfung vorliegt.
   */
  public boolean isValidSignature() throws MissingPreconditionException {
    SignatureVerificationResult signatureVerificationResult =
        TestcaseData.getInstance().getSignatureVerificationResult();
    if (signatureVerificationResult == null) {
      throw new MissingPreconditionException("Es liegt kein Ergebnis einer Signaturprüfung vor.");
    }
    return signatureVerificationResult.isValidSignature();
  }

  /**
   * Prüft, ob ein Verordnungsdatensatz erfolgreich signiert werden konnte.
   *
   * @return true, falls der Verordnungsdatensatz erfolgreich signiert werden konnte, andernfalls
   *     false.
   */
  public boolean verifySignPrescription() throws MissingPreconditionException {
    SignDocumentResult signDocumentResult = TestcaseData.getInstance().getSignDocumentResult();
    if (signDocumentResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis der Signatur eines Verordnungsdatensatzes vor.");
    }
    return signDocumentResult.isValidResponse();
  }

  /**
   * Liest das AUT-Zertifikat der im Testfall verwendeten Karte. Das Zertifikat wird für die weitere
   * Verarbeitung im Testfall zur Verfügung gestellt.
   */
  public void readCardCertificate() throws MissingPreconditionException {
    checkHbaHandle();
    try {
      TestcaseData testcaseData = TestcaseData.getInstance();
      String cardHandle = testcaseData.getHbaHandle();

      CardCertificateReader cardCertificateReader =
          applicationContext.getBeanFactory().getBean(CardCertificateReader.class);

      byte[] autCertificate =
          cardCertificateReader.readCardCertificate(
              testcaseData.getInvocationContext(), cardHandle);

      testcaseData.setAutCertificate(autCertificate);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Signiert die code_challenge mittels der Operation ExternalAuthenticate des Konnektors und einer
   * SMC-B.
   *
   * @throws MissingPreconditionException Wenn kein CardHandle einer SMC-B oder keine code_challenge
   *     vorliegen.
   */
  public void authenticateExternallySmcB() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    checkSmcBHandle();

    authenticateExternally(invocationContext, testcaseData.getSmcBHandle());
  }

  /**
   * Signiert die code_challenge mittels der Operation ExternalAuthenticate des Konnektors und einem
   * HBA.
   *
   * @throws MissingPreconditionException Wenn kein CardHandle eines HBA oder keine code_challenge
   *     vorliegen.
   */
  public void authenticateExternallyHba() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    checkHbaHandle();

    authenticateExternally(invocationContext, testcaseData.getHbaHandle());
  }

  private void authenticateExternally(InvocationContext invocationContext, String cardHandle)
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    byte[] codeChallenge = testcaseData.getCodeChallenge();
    if (codeChallenge == null || codeChallenge.length == 0) {
      throw new MissingPreconditionException("Es liegt keine code_challenge vor.");
    }

    try {
      ExternalAuthenticator externalAuthenticator =
          applicationContext.getBeanFactory().getBean(ExternalAuthenticator.class);
      ExternalAuthenticateResult externalAuthenticateResult =
          externalAuthenticator.authenticateExternally(
              invocationContext, cardHandle, codeChallenge);

      testcaseData.setExternalAuthenticateResult(externalAuthenticateResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  private void checkHbaHandle() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String cardHandle = testcaseData.getHbaHandle();
    if (StringUtils.isEmpty(cardHandle)) {
      throw new MissingPreconditionException("Es wurde kein HBA-Handle ermittelt.");
    }
  }

  private void checkSmcBHandle() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String cardHandle = testcaseData.getSmcBHandle();
    if (StringUtils.isEmpty(cardHandle)) {
      throw new MissingPreconditionException("Es wurde kein SMC-B-Handle ermittelt.");
    }
  }

  /**
   * Prüft, ob ein signierter Verordnungsdatensatz verfügbar ist.
   *
   * @throws MissingPreconditionException Falls kein signierter Verordnungsdatensatz verfügbar ist.
   */
  public void checkSignedPrescriptionAvailable() throws MissingPreconditionException {
    SignDocumentResult signDocumentResult = TestcaseData.getInstance().getSignDocumentResult();
    if (signDocumentResult == null
        || signDocumentResult.getSignedBundle() == null
        || signDocumentResult.getSignedBundle().length == 0) {
      throw new MissingPreconditionException(
          "Es ist kein signierter Verordnungsdatensatz verfügbar.");
    }
  }

  /**
   * Erstellt eine UserID lt Spezifikation A_19259
   *
   * <p>A_19259 - PS: Starke UserID für den HBA-Nutzer
   *
   * <p>Das PS MUSS bei jedem Aufruf der Operation ActivateComfortSignature eine neue
   * 128bitZufallszahl erzeugen und als UserID verwenden,<br>
   * solange die Komfortsignatur aktiv ist. Das PS MUSS diese starke UserID (schwer zu erratende
   * UserID) bei jedem Signaturvorgang des HBANutzers verwenden, <br>
   * solange der jeweils aktivierte Komfortsignaturmodus aktiviert bleibt. <br>
   * <b>Eine neue UserID darf erst wieder mit einem erneuten Aufruf von ActivateComfortSignature
   * verwendet werden.</b>
   *
   * @throws MissingPreconditionException falls der Aufrufkontext nicht gesetzt oder nur
   *     unvollständig verfügbar ist.
   */
  public void determineUserId() throws MissingPreconditionException {
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    // auslösen der MissingPreconditionException falls der Aufrufkontext nicht valid ist
    checkInvocationContext(invocationContext);
    String userId = UUID.randomUUID().toString();
    invocationContext.setUser(userId);
  }

  /**
   * Liest die persistierte UserID aus und stellt diese im Aufrufkontext zur Verfügung.
   *
   * @throws MissingPreconditionException Falls kein Aufrufkontext verfügbar ist.
   */
  public void ensureUserId() throws MissingPreconditionException {
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    // auslösen der MissingPreconditionException falls der Aufrufkontext nicht valid ist
    checkInvocationContext(invocationContext);
    String userId = invocationContext.getUser();
    if (CommonUtils.isNullOrEmpty(userId)) {
      throw new MissingPreconditionException(
          "Es steht keine UserID zur Verfügung. Eventuell muss der Testschritt \"PS erstellt neue UserID\" aufgerufen werden.");
    }
  }

  /** Aktiviert die Komfortsignatur für einen HBA. */
  public void activateComfortSignature() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);
    checkHbaHandle();

    try {
      ComfortSignatureActivator comfortSignatureActivator =
          applicationContext.getBeanFactory().getBean(ComfortSignatureActivator.class);
      ComfortSignatureResult comfortSignatureResult =
          comfortSignatureActivator.activateComfortSignature(
              invocationContext, testcaseData.getHbaHandle());
      testcaseData.setActivateComfortSignatureResult(comfortSignatureResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob die Komfortsignatur erfolgreich aktiviert werden konnte.
   *
   * @return true, falls die Komfortsignatur erfolgreich aktiviert werden konnte, andernfalls false.
   */
  public boolean isActivateComfortSignatureOk() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = isComfortSignatureResultAvailable();
    return comfortSignatureResult.isComfortSignatureActivated();
  }

  private ComfortSignatureResult isComfortSignatureResultAvailable()
      throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        TestcaseData.getInstance().getActivateComfortSignatureResult();
    if (comfortSignatureResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis der Operation ActivateComfortSignature vor");
    }
    return comfortSignatureResult;
  }
  /**
   * Prüft, ob der letzte Aufruf von ActivateComfortSignature mit Fehler 4018 beantwortet wurde.
   *
   * @return true, falls der letzte Aufruf von ActivateComfortSignature mit Fehler 4018 beantwortet
   *     wurde, andernfalls false.
   */
  public boolean isActivateComfortSignatureSoapFault4018() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = isComfortSignatureResultAvailable();
    return comfortSignatureResult.isSoapFault4018();
  }

  /**
   * Prüft, ob die Komfortsignatur erfolgreich deaktiviert werden konnte.
   *
   * @return true, falls die Komfortsignatur erfolgreich deaktiviert werden konnte, andernfalls
   *     false.
   * @throws MissingPreconditionException wenn das Ergebnis der deaktivierung {@link
   *     ComfortSignatureResult} nicht oder unvollständig abgerufen werden konnte
   */
  public boolean isDeactivateComfortSignatureOk() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        TestcaseData.getInstance().getComfortSignatureDeactivated();
    if (comfortSignatureResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis der Operation DeactivateComfortSignature vor");
    }
    return comfortSignatureResult.isDeactivateComfortSignature();
  }

  /**
   * Deaktiviert die Komfortsignatur für einen HBA.
   *
   * <p>A_19136 - PS: Deaktivieren der Komfortsignaturfunktion Das Primärsystem MUSS für die
   * Deaktivierung der Komfortsignaturfunktion die Operation <i>DeactivateComfortSignature</i> gemäß
   * [gemSpec_Kon#4.1.8.5.6] verwenden.
   */
  public void deactivateComfortSignature() throws MissingPreconditionException {
    try {
      TestcaseData testcaseData = TestcaseData.getInstance();
      InvocationContext invocationContext = testcaseData.getInvocationContext();
      checkInvocationContext(invocationContext);
      checkHbaHandle();

      ComfortSignatureDeactivator comfortSignatureDeActivator =
          applicationContext.getBeanFactory().getBean(ComfortSignatureDeactivator.class);
      ComfortSignatureResult result =
          comfortSignatureDeActivator.deactivateComfortSignature(testcaseData.getHbaHandle());
      testcaseData.setComfortSignatureDeactivated(result);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Bestimmt den Modus des zur Signatur von Verordnungsdatensätzen verwendeten HBAs.
   *
   * @throws MissingPreconditionException Falls das HBA-Handle nicht zuvor ermittelt wurde.
   */
  public void determineSignatureMode() throws MissingPreconditionException {
    checkHbaHandle();

    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    try {
      SignatureModeGetter signatureModeGetter =
          applicationContext.getBeanFactory().getBean(SignatureModeGetter.class);
      ComfortSignatureResult comfortSignatureResult =
          signatureModeGetter.determineSignatureMode(
              testcaseData.getHbaHandle(), invocationContext);
      testcaseData.setGetSignatureModeResult(comfortSignatureResult);

    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft ob das Ergebnis der Operation GetSignatureMode für den aktuell verwendeten HBA Status
   * "OK" und Signatur-Modus "COMFORT" ist.
   *
   * @return True, falls das Ergebnis der Operation GetSignatureMode für den aktuell verwendeten HBA
   *     Status "OK" und Signatur-Modus "COMFORT" ist, andernfalls false.
   * @throws MissingPreconditionException Falls kein Ergebnis der Operation GetSignatureMode
   *     vorliegt.
   */
  public boolean isSignatureModeComfort() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        TestcaseData.getInstance().getGetSignatureModeResult();

    if (comfortSignatureResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis der Operation GetSignatureMode vor.");
    }

    return comfortSignatureResult.isComfortSignatureActivated();
  }

  /**
   * TIP1-A_4570 - TUC_KON_022 „Liefere PIN-Status“ Prüft den Zustand eines PIN-Objekts einer Karte
   * im Kontext einer CardSession.
   */
  public void determinePinStatusQes() throws MissingPreconditionException {
    checkHbaHandle();

    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);
    String cardHandle = TestcaseData.getInstance().getHbaHandle();
    determinePinStatusResult(invocationContext, testcaseData, cardHandle);
  }

  /**
   * Prüft, dass der Signatur-Modus "COMFORT" ist.
   *
   * @throws MissingPreconditionException Falls der Signatur-Modus nicht "COMFORT" ist.
   */
  public void determineSignatureModeComfort() throws MissingPreconditionException {
    determineSignatureMode();
    ComfortSignatureResult getSignatureModeResult =
        TestcaseData.getInstance().getGetSignatureModeResult();
    if (!getSignatureModeResult.isComfortSignatureActivated()) {
      throw new MissingPreconditionException("Der Signatur-Modus ist nicht \"COMFORT\"");
    }
  }

  /**
   * Ermittelt den PinStatusResult und setzt die Eigenschaften in das {@link TestcaseData} objekt
   *
   * @param invocationContext {@link InvocationContext}
   * @param testcaseData {@link TestcaseData}
   */
  private void determinePinStatusResult(
      InvocationContext invocationContext, TestcaseData testcaseData, String cardHandle) {
    try {
      PinStatus pinStatus = applicationContext.getBeanFactory().getBean(PinStatus.class);
      PinStatusResult pinStatusResult =
          pinStatus.getPinStatusResponse(invocationContext, PIN_QES, cardHandle);
      testcaseData.setPinStatusResult(pinStatusResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft den erwarteten Status
   *
   * @param expectedEnum {@link PinStatusEnum}
   * @return true, wenn der Wert von expectedEnum dem ermittelten Wert entspricht andernfalls false
   *     oder aber im Fehlerfall eine {@link MissingPreconditionException}
   * @throws MissingPreconditionException im Fehlerfall
   */
  private boolean determinePinStatusResultByExpected(PinStatusEnum expectedEnum)
      throws MissingPreconditionException {
    checkHbaHandle();
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    if (null == testcaseData.getPinStatusResult()) {
      throw new MissingPreconditionException(
          MessageFormat.format(
              "PinStatusResult wurde nicht ermittelt. Erwartet: {0}", expectedEnum.value()));
    }
    return (testcaseData
        .getPinStatusResult()
        .getPinStatusEnum()
        .value()
        .equals(expectedEnum.value()));
  }

  /**
   * Prüft, ob PinStatusEnum.VERIFIED in der PinStatusResponse gesetzt und enthalten ist. REZEPS-83:
   * Prüfen, ob GetPinStatus-Response "VERIFIED" ist
   *
   * @return true, wenn {@link PinStatusEnum} VERIFIED ist
   * @throws MissingPreconditionException im Fehlerfall
   */
  public boolean isGetPinStatusResponseVerified() throws MissingPreconditionException {
    return determinePinStatusResultByExpected(PinStatusEnum.VERIFIED);
  }

  /**
   * Prüft, ob PinStatusEnum.VERIFIABLE in der PinStatusResponse gesetzt und enthalten ist.
   * REZEPS-84: Prüfen, ob GetPinStatus-Response "VERIFIABLE" ist
   *
   * @return true, wenn {@link PinStatusEnum} VERIFIABLE ist
   * @throws MissingPreconditionException im Fehlerfall
   */
  public boolean isGetPinStatusResponseVerifiable() throws MissingPreconditionException {
    return determinePinStatusResultByExpected(PinStatusEnum.VERIFIABLE);
  }

  /** @return true, wenn der SignatureMode des CardHandles in der Session "PIN" ist */
  public boolean isSignatureModePin() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        TestcaseData.getInstance().getGetSignatureModeResult();
    if (comfortSignatureResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis der Operation GetSignatureMode vor.");
    }
    return ("PIN".equals(comfortSignatureResult.getSignatureMode()));
  }

  /**
   * Gibt die IPAdresse der angegebenen FQDN zurück
   *
   * @param fqdn FQDN
   * @return String IPAdresse
   */
  public String getIPAddress(String fqdn) {
    String ipAddress = null;
    try {
      ipAddress = IPUtil.getIpAddress(fqdn);
      TestcaseData.getInstance().setFdIpAddress(ipAddress);
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(MessageFormat.format("Resolved IP: {0} for \"{1}\"", ipAddress, fqdn));
      }
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
    return ipAddress;
  }

  /**
   * Schaltet die die PIN.CH eines HBAs frei.
   *
   * @throws MissingPreconditionException Falls im Testablauf zuvor kein Aufrufkontext oder
   *     HBA-Handle bestimmt wurde.
   */
  public void verifyPin() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);
    checkHbaHandle();
    try {
      PinVerifier pinVerifier = applicationContext.getBeanFactory().getBean(PinVerifier.class);
      VerifyPinResult verifyPinResult =
          pinVerifier.performVerifyPin(invocationContext, testcaseData.getHbaHandle());
      testcaseData.setVerifyPinResult(verifyPinResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Schaltet die PIN.CH frei.
   *
   * @param cardHandle String cardHandle
   * @throws MissingPreconditionException Falls im Testablauf zuvor kein Aufrufkontext oder Handle
   *     bestimmt wurde.
   */
  public void verifyPin(String cardHandle) throws MissingPreconditionException {
    if (CommonUtils.isNullOrEmpty(cardHandle)) {
      throw new MissingPreconditionException("No valid CardHandle found!");
    }
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    try {
      PinVerifier pinVerifier = applicationContext.getBeanFactory().getBean(PinVerifier.class);
      VerifyPinResult verifyPinResult = pinVerifier.performVerifyPin(invocationContext, cardHandle);
      testcaseData.setVerifyPinResult(verifyPinResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /*
   * Prüfen, ob das SignDocument richtigerweise fehlschlägt. Das THEN soll generisch einsetzbar sein
   * unabhängig von der Fehlerursache, damit es in vielen Szenarios einsetzbar ist.
   *
   * @return false, wenn erfolgreich signiert wurde anderenfalls true
   *
   * @throws MissingPreconditionException wenn keine Daten vorliegen.
   */
  public boolean checkSignDocumentHasFailed() throws MissingPreconditionException {
    if (null == TestcaseData.getInstance().getSignDocumentResult()) {
      throw new MissingPreconditionException("Es liegt kein Ergebnis einer Signatur vor");
    }
    return (!TestcaseData.getInstance().getSignDocumentResult().isValidResponse());
  }

  /**
   * Prüft das Ergebnis der letzten Ausführung von VerifyPin.
   *
   * @return true, wenn die letzte Ausführung VerifyPin erfolgreich war, andernfalls false.
   * @throws MissingPreconditionException Falls kein Ergebnis einer Ausführung von VerifyPin
   *     vorliegt.
   */
  public boolean isVerifyPinOk() throws MissingPreconditionException {
    VerifyPinResult verifyPinResult = TestcaseData.getInstance().getVerifyPinResult();
    if (verifyPinResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis einer Ausführung von VerifyPin vor.");
    }
    return verifyPinResult.isValidResponse();
  }

  /**
   * Fordert den Auswurf des im Testfall verwendeten HBAs an. Speichert das Ergebnis des Aufrufs für
   * die weitere Verarbeitung.
   *
   * @throws MissingPreconditionException Falls kein Aufrufkontext gesetzt wurde oder kein
   *     HBA-Handle verfügbar ist.
   */
  public void ejectCard() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);
    checkHbaHandle();
    try {
      CardEjector cardEjector = applicationContext.getBeanFactory().getBean(CardEjector.class);
      EjectCardResult ejectCardResult =
          cardEjector.performEjectCard(invocationContext, testcaseData.getHbaHandle());
      testcaseData.setEjectCardResult(ejectCardResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft das Ergebnis der letzten Ausführung von EjectCard.
   *
   * @return true, wenn die letzte Ausführung von EjectCard erfolgreich war, andernfalls false.
   * @throws MissingPreconditionException Falls kein Ergebnis einer Ausführung von EjectCard
   *     vorliegt.
   */
  public boolean isEjectCardOk() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = TestcaseData.getInstance().getEjectCardResult();
    if (ejectCardResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis einer Ausführung von EjectCard vor.");
    }
    return ejectCardResult.isValidResponse();
  }

  /**
   * Ruft die Liste der in einem Aufrufkontext verfügbaren Kartenterminals vom Konnektor ab.
   *
   * @throws MissingPreconditionException Falls kein Aufrufkontext verfügbar ist.
   */
  public void determineCardTerminals() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    CardTerminalsGetter cardTerminalsGetter =
        applicationContext.getBeanFactory().getBean(CardTerminalsGetter.class);
    try {
      GetCardTerminalsResult getCardTerminalsResult =
          cardTerminalsGetter.performGetCardTerminals(invocationContext);
      testcaseData.setGetCardTerminalsResult(getCardTerminalsResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft das Ergebnis der letzten Ausführung von GetCardTerminals.
   *
   * @return true, wenn die letzte Ausführung von GetCardTerminals erfolgreich war, andernfalls
   *     false.
   * @throws MissingPreconditionException Falls kein Ergebnis einer Ausführung von GetCardTerminals
   *     vorliegt.
   */
  public boolean isGetCardTerminalsOk() throws MissingPreconditionException {
    GetCardTerminalsResult getCardTerminalsResult =
        TestcaseData.getInstance().getGetCardTerminalsResult();
    if (getCardTerminalsResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis einer Ausführung von " + "GetCardTerminals vor.");
    }
    return getCardTerminalsResult.isValidResponse();
  }

  /**
   * Fordert das Stecken des im Testfall verwendeten HBAs an. Speichert das Ergebnis des Aufrufs für
   * die weitere Verarbeitung.
   *
   * @param ctID ID des Terminals, in das die Karte gesteckt werden soll.
   * @param slot Nummer des Slots, in den die Karte gesteckt werden soll.
   * @throws MissingPreconditionException Falls kein Aufrufkontext gesetzt wurde oder keine CtId
   *     verfügbar ist.
   */
  public void requestCard(String ctID, int slot) throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = testcaseData.getInvocationContext();
    checkInvocationContext(invocationContext);

    CardRequestor cardRequestor = applicationContext.getBeanFactory().getBean(CardRequestor.class);
    try {
      RequestCardResult requestCardResult =
          cardRequestor.performRequestCard(invocationContext, ctID, slot);
      testcaseData.setRequestCardResult(requestCardResult);
    } catch (IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, dass der letzte Aufruf von RequestCard erfolgreich ausgeführt werden konnte.
   *
   * @return true, falls der letzte Aufruf von RequestCard erfolgreich ausgeführt werden konnte,
   *     andernfalls false.
   * @throws MissingPreconditionException falls kein Ergebnis einer Ausführung von RequestCard
   *     vorliegt.
   */
  public boolean isRequestCardOk() throws MissingPreconditionException {
    RequestCardResult requestCardResult = TestcaseData.getInstance().getRequestCardResult();
    if (requestCardResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis einer Ausführung von RequestCard vor.");
    }
    return requestCardResult.isValidResponse();
  }

  /**
   * Prüft, ob der letzte Aufruf von ExternalAuthenticate erfolgreich ausgeführt werden konnte.
   *
   * @return true, wenn der letzte Aufruf von ExternalAuthenticate erfolgreich ausgeführt werden
   *     konnte, andernfalls false.
   * @throws MissingPreconditionException falls kein Ergebnis einer Ausführung von
   *     ExternalAuthenticate vorliegt.
   */
  public boolean isExternalAuthenticateOk() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    ExternalAuthenticateResult externalAuthenticateResult =
        testcaseData.getExternalAuthenticateResult();
    if (externalAuthenticateResult == null) {
      throw new MissingPreconditionException(
          "Es liegt kein Ergebnis einer Ausführung von ExternalAuthenticate vor.");
    }
    return externalAuthenticateResult.isValidResponse();
  }

  /**
   * setzt den angegebenen Arbeitsplatz in den aktuellen Aufrufkontext
   *
   * @param arg0 String Workplace
   */
  public void changeWorkplace(String arg0) {
    TestcaseData.getInstance().getInvocationContext().setWorkplace(arg0);
  }

  /**
   * Prüft, ob der letzte Aufruf von EjectCard vom Konnektor mit dem SOAP-Fault 4203 beantwortet
   * wurde.
   *
   * @return True, falls der letzte Aufruf von EjectCard vom Konnektor mit dem SOAP-Fault 4203
   *     beantwortet wurde, andernfalls false.
   * @throws MissingPreconditionException Falls kein Ergebnis einer Ausführung von EjectCard
   *     vorliegt.
   */
  public boolean isEjectCardSoapFault4203() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = TestcaseData.getInstance().getEjectCardResult();
    if (ejectCardResult == null) {
      throw new MissingPreconditionException("Es liegt kein Ergebnis der Operation EjectCard vor");
    }
    return ejectCardResult.isSoapFault4203();
  }

  /**
   * Tauscht den Arbeitsplatz im Aufrufkontext.
   *
   * @param workplace Der im Aufrufkontext zu setzende Arbeitsplatz.
   * @throws MissingPreconditionException Falls noch kein Aufrufkontext initialisiert war.
   */
  public void switchWorkplace(String workplace) throws MissingPreconditionException {
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    checkInvocationContext(invocationContext);
    invocationContext.setWorkplace(workplace);
  }
}
