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
import de.gematik.rezeps.KonnektorClient;
import de.gematik.rezeps.bundle.BundleHelper;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.cardterminal.EjectCardResult;
import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.xml.sax.SAXException;

public class KonnektorGlueCodeIntegrationTest {

  private static final Logger IT_TEST_LOGGER = LoggerFactory.getLogger(KonnektorGlueCode.class);

  private static KonnektorGlueCode konnektorGlueCode;

  private static final String MANDANT = "Mandant1";
  private static final String CLIENT_SYSTEM = "ClientID1";
  private static final String WORKPLACE = "Workplace1";
  private static final String USER = "Dr. Peter Müller";
  private static final byte[] DATA_TO_BE_SIGNED = "signiere mich".getBytes(StandardCharsets.UTF_8);

  @BeforeClass
  public static void beforeClass() {
    konnektorGlueCode =
        new KonnektorGlueCode(SpringApplication.run(KonnektorClient.class, new String[] {}));
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineCardHandle() throws MissingPreconditionException {
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
    konnektorGlueCode.determineHbaHandle();
    String cardHandle = TestcaseData.getInstance().getHbaHandle();
    Assert.assertNotNull(cardHandle);
    Assert.assertFalse(cardHandle.isEmpty());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineSmcbHandle() throws MissingPreconditionException {
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
    konnektorGlueCode.determineSmcBHandle();
    String cardHandle = TestcaseData.getInstance().getSmcBHandle();
    Assert.assertNotNull(cardHandle);
    Assert.assertFalse(cardHandle.isEmpty());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineAutCertificate() throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.readCardCertificate();
    Assert.assertNotNull(TestcaseData.getInstance().getAutCertificate());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldAuthenticateExternallySmcB()
      throws MissingPreconditionException, DecoderException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineSmcBHandle();
    TestcaseData.getInstance()
        .setCodeChallenge(
            Hex.decodeHex("2cbe1bf1072302b0d9b11c37bba3799012877fa07eafffad7f4db5ecf1ebe842"));
    konnektorGlueCode.authenticateExternallySmcB();
    Assert.assertNotNull(
        TestcaseData.getInstance().getExternalAuthenticateResult().getAuthenticatedData());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldPerformSignPrescription() throws MissingPreconditionException {
    InvocationContext invokationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, "12345678");
    TestcaseData.getInstance().setInvocationContext(invokationContext);
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.modifyBundleWithPatientData(initializePatient());
    konnektorGlueCode.determineJobNumber();
    konnektorGlueCode.signPrescription();
    TestcaseData testcaseData = TestcaseData.getInstance();
    Assert.assertNotNull(testcaseData.getSignDocumentResult().getSignedBundle());
  }

  private Patient initializePatient() {
    return new Patient(
        "Bart",
        "Simpson",
        "X081504711",
        "Evergreen Terrace",
        "742",
        "12345",
        "Springfield",
        "1990-04-01");
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineJobNumber() throws MissingPreconditionException {
    konnektorGlueCode.determineJobNumber();
    String jobNumber = TestcaseData.getInstance().getJobNumber();
    Assert.assertNotNull(jobNumber);
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineContext() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    Assert.assertNotNull(invocationContext);
    Assert.assertTrue(invocationContext.isValidInvocationContext());
  }

  @Test(expected = MissingPreconditionException.class)
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineContextFail() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, "", USER));
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    Assert.assertNull(
        MessageFormat.format(
            "Aufrufkontext ist nicht <null>\r\n\tMandant: {0}, ClientSystem: {1}, Workplace: {2}",
            invocationContext.getMandant(),
            invocationContext.getClientSystem(),
            invocationContext.getWorkplace()),
        invocationContext);
  }

  @Test(expected = MissingPreconditionException.class)
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldFailIsValidInvocationContext() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, "", USER));
    InvocationContext inValidInvocationContext = TestcaseData.getInstance().getInvocationContext();

    Assert.assertTrue(
        MessageFormat.format(
            "Fehler beim validieren des Aufrufkontextes:\r\n\t Mandant: {0}, ClientSystem: {1}, Workplace: {2} ",
            inValidInvocationContext.isValidInvocationContext(),
            inValidInvocationContext.getMandant(),
            inValidInvocationContext.getClientSystem(),
            inValidInvocationContext.getWorkplace()),
        inValidInvocationContext.isValidInvocationContext());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldVerifySignature()
      throws MissingPreconditionException, ParserConfigurationException, SAXException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.getInvocationContext().setUser(USER);

    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.determineJobNumber();

    testcaseData.setBundle(new BundleHelper());
    konnektorGlueCode.signPrescription();
    konnektorGlueCode.verifySignature();
  }

  @Test
  @Ignore(
      "Ist nur lauffähig, wenn de.gematik.rezeps.fdclient.ApplicationStarter gestartet wurde und eine Konnektor-Gegenstelle verfügbar ist. (KoPS funktion nicht verfügbar)")
  public void shouldDeactivateComfortSignature() throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.deactivateComfortSignature();
    Assert.assertTrue(konnektorGlueCode.isDeactivateComfortSignatureOk());
    // TODO: "Validieren wenn Konnektor-Gegenstelle verfügbar ist"

  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineUserIdSetExplicit() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    // setzt explizit den Benutzer
    invocationContext.setUser(USER);
    // Überschreiben durch generierte UserId
    konnektorGlueCode.determineUserId();
    // Prüfung, ob im Anmeldekontext nun der Benutzer enthalten ist; erwartet wird die generierte
    // Benutzer-Id
    Assert.assertNotEquals(USER, invocationContext.getUser());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineSignatureMode() throws MissingPreconditionException {
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.determineSignatureMode();
    Assert.assertNotNull(TestcaseData.getInstance().getGetSignatureModeResult());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDeterminePinStatusQes() throws MissingPreconditionException {

    TestcaseData testcaseData = TestcaseData.getInstance();
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.determinePinStatusQes();
    Assert.assertNotNull(testcaseData.getPinStatusResult());
    IT_TEST_LOGGER.info(
        MessageFormat.format(
            "Status for Card {0} is {1}",
            testcaseData.getHbaHandle(), testcaseData.getPinStatusResult().getPinStatusEnum()));
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineSignatureModeComfort() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    konnektorGlueCode.determineUserId();
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.activateComfortSignature();
    konnektorGlueCode.determineSignatureModeComfort();
    // Es wurde keine MissingPreconditionException geworfen
    ComfortSignatureResult getSignatureModeResult =
        TestcaseData.getInstance().getGetSignatureModeResult();
    Assert.assertTrue(getSignatureModeResult.isComfortSignatureActivated());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineIPAddressFromFqdn() {
    konnektorGlueCode.getIPAddress("www.cloudflare.com");
    Assert.assertNotNull(TestcaseData.getInstance().getFdIpAddress());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldVerifyPin() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.verifyPin();
    Assert.assertNotNull(TestcaseData.getInstance().getVerifyPinResult());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldEjectCard() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.ejectCard();
    Assert.assertNotNull(TestcaseData.getInstance().getEjectCardResult());
  }

  @Test
  @Ignore(
      "Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist. Karte bei "
          + "Testdurchführung nicht ziehen.")
  public void shouldBeSoapFault4203WithEjectCard() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.ejectCard();
    EjectCardResult ejectCardResult = TestcaseData.getInstance().getEjectCardResult();
    Assert.assertNotNull(ejectCardResult);
    Assert.assertTrue(ejectCardResult.isSoapFault4203());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldDetermineCardTerminals() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineCardTerminals();
    Assert.assertNotNull(TestcaseData.getInstance().getGetCardTerminalsResult());
  }

  @Test
  @Ignore("Ist nur lauffähig, wenn eine Konnektor-Gegenstelle verfügbar ist.")
  public void shouldRequestCard() throws MissingPreconditionException {
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineCardTerminals();
    String ctId = TestcaseData.getInstance().getGetCardTerminalsResult().getCtId();
    konnektorGlueCode.requestCard(ctId, 1);
    Assert.assertNotNull(TestcaseData.getInstance().getRequestCardResult());
  }
}
