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

import static de.gematik.rezeps.comfortsignature.ComfortSignatureResult.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.authentication.ExternalAuthenticateResult;
import de.gematik.rezeps.authentication.ExternalAuthenticator;
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
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.rezeps.util.CommonUtils;
import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class KonnektorGlueCodeTest {

  private static final String MANDANT = "mandantId";
  private static final String CLIENT_SYSTEM = "clientSystemId";
  private static final String WORKPLACE = "workplaceId";
  private static final String WORKPLACE_2 = "workplaceId_2";
  private static final String USER = "userId";
  private static final String CARD_HANDLE = "HBA-1";
  private static final String SMC_B_HANDLE = "SMC_B-1";
  private static final byte[] EXPECTED_AUT_CERTIFICATE =
      "Ich bin ein AUT-Zertifikat".getBytes(StandardCharsets.UTF_8);
  private static final byte[] SIGNED_DATA = "ich bin signiert".getBytes(StandardCharsets.UTF_8);
  private static final byte[] CODE_CHALLENGE = "code_challenge".getBytes(StandardCharsets.UTF_8);
  private static final String SIGNATURE_MODE_PIN = "PIN";
  private static final String CT_ID = "dummy_ct_id_01";

  public static final String RESULT_OK = "OK";
  public static final String RESULT_WARNING = "Warning";
  public static final int SLOT_2 = 2;

  @Test
  public void shouldReadCardCertificate() throws IOException, MissingPreconditionException {
    CardCertificateReader cardCertificateReader = mock(CardCertificateReader.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardCertificateReader.readCardCertificate(invocationContext, CARD_HANDLE))
        .thenReturn(EXPECTED_AUT_CERTIFICATE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardCertificateReader.class))
        .thenReturn(cardCertificateReader);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(invocationContext);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.readCardCertificate();

    Assert.assertEquals(EXPECTED_AUT_CERTIFICATE, testcaseData.getAutCertificate());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailReadCardCertificateOnMissingCardHandle()
      throws MissingPreconditionException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.readCardCertificate();
  }

  @Test
  public void shouldAuthenticateExternallySmcB() throws MissingPreconditionException, IOException {
    ExternalAuthenticator externalAuthenticator = mock(ExternalAuthenticator.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, SIGNED_DATA);
    when(externalAuthenticator.authenticateExternally(
            invocationContext, SMC_B_HANDLE, CODE_CHALLENGE))
        .thenReturn(externalAuthenticateResult);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    ConfigurableApplicationContext configurableApplicationContext =
        determineConfigurableApplicationContextForExternalAuthenticate(externalAuthenticator);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternallySmcB();

    ExternalAuthenticateResult expectedExternalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, SIGNED_DATA);
    Assert.assertEquals(
        expectedExternalAuthenticateResult, testcaseData.getExternalAuthenticateResult());
  }

  private ConfigurableApplicationContext
      determineConfigurableApplicationContextForExternalAuthenticate(
          ExternalAuthenticator externalAuthenticator) {

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(ExternalAuthenticator.class))
        .thenReturn(externalAuthenticator);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    return configurableApplicationContext;
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallySmcBOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternallySmcB();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallySmcBOnMissingCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setSmcBHandle(null);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternallySmcB();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallySmcBOnEmptyDataToBeSigned()
      throws MissingPreconditionException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(new byte[] {});

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternallySmcB();
  }

  @Test
  public void shouldAuthenticateExternallyHba() throws MissingPreconditionException, IOException {
    ExternalAuthenticator externalAuthenticator = mock(ExternalAuthenticator.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, SIGNED_DATA);
    when(externalAuthenticator.authenticateExternally(
            invocationContext, CARD_HANDLE, CODE_CHALLENGE))
        .thenReturn(externalAuthenticateResult);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    ConfigurableApplicationContext configurableApplicationContext =
        determineConfigurableApplicationContextForExternalAuthenticate(externalAuthenticator);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternallyHba();

    ExternalAuthenticateResult expectedExternalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, SIGNED_DATA);
    Assert.assertEquals(
        expectedExternalAuthenticateResult, testcaseData.getExternalAuthenticateResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyHbaOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternallyHba();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyHbaOnMissingCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setHbaHandle(null);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternallyHba();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyHbaOnEmptyDataToBeSigned()
      throws MissingPreconditionException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setCodeChallenge(new byte[] {});

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternallyHba();
  }

  @Test
  public void shouldNotFailCheckSignedPrescriptionAvailable() throws MissingPreconditionException {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_DATA);
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkSignedPrescriptionAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckSignedPrescriptionAvailableOnMissingSignDocumentResult()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkSignedPrescriptionAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckSignedPrescriptionAvailableOnMissingSignedBundle()
      throws MissingPreconditionException {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            null);
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkSignedPrescriptionAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckSignedPrescriptionAvailableOnEmptySignedBundle()
      throws MissingPreconditionException {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            new byte[] {});
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkSignedPrescriptionAvailable();
  }

  @Test
  public void shouldDetermineUserId() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineUserId();
    Assert.assertNotNull(
        MessageFormat.format(
            "UserId für {0} ist nicht gesetzt!",
            TestcaseData.getInstance().getInvocationContext().getMandant()),
        TestcaseData.getInstance().getInvocationContext().getUser());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDetermineUserIdFail() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData.getInstance().setInvocationContext(null);
    konnektorGlueCode.determineUserId();
    Assert.assertNotNull(
        "UserId ist gesetzt!", TestcaseData.getInstance().getInvocationContext().getUser());
  }

  @Test
  public void shouldFailDetermineUserId() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    Assert.assertTrue(CommonUtils.isNullOrEmpty(invocationContext.getUser()));
  }

  @Test
  public void shouldOverwritePredefinedUserId() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    konnektorGlueCode.determineUserId();
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    Assert.assertFalse(CommonUtils.isNullOrEmpty(invocationContext.getUser()));
    Assert.assertNotEquals(USER, invocationContext.getUser());
  }

  @Test
  public void shouldActivateComfortSignature() throws MissingPreconditionException, IOException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(configurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    ComfortSignatureActivator comfortSignatureActivator = mock(ComfortSignatureActivator.class);
    when(beanFactory.getBean(ComfortSignatureActivator.class))
        .thenReturn(comfortSignatureActivator);

    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setActivateComfortSignatureResult(null);

    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, SIGNATURE_MODE_COMFORT);
    when(comfortSignatureActivator.activateComfortSignature(invocationContext, CARD_HANDLE))
        .thenReturn(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.activateComfortSignature();

    Assert.assertTrue(
        testcaseData.getActivateComfortSignatureResult().isComfortSignatureActivated());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailActivateComfortSignatureOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setActivateComfortSignatureResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.activateComfortSignature();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailActivateComfortSignatureOnMissingCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(null);
    testcaseData.setActivateComfortSignatureResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.activateComfortSignature();
  }

  @Test
  public void shouldValidateActivateComfortSignatureOk() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, SIGNATURE_MODE_COMFORT);
    TestcaseData.getInstance().setActivateComfortSignatureResult(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isActivateComfortSignatureOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotValidateActivateComfortSignatureOnMissingActivateComfortSignatureResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setActivateComfortSignatureResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isActivateComfortSignatureOk();
  }

  @Test
  public void shouldNotValidateActivateComfortSignatureOnInvalidSignatureMode()
      throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult(STATUS_OK, "PIN");
    TestcaseData.getInstance().setActivateComfortSignatureResult(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isActivateComfortSignatureOk());
  }

  private ConfigurableApplicationContext prepareApplicationContextForDeactivationTest() {
    // Mocked applicationContext
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    // mocked BeanFactory
    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(configurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    // mocked ComfortSignatureDeActivator
    ComfortSignatureDeactivator comfortSignatureDeActivator =
        mock(ComfortSignatureDeactivator.class);
    when(beanFactory.getBean(ComfortSignatureDeactivator.class))
        .thenReturn(comfortSignatureDeActivator);
    return configurableApplicationContext;
  }

  @Test
  public void shouldValidateActivateComfortSignatureSoapFault4018()
      throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSoapFault(ERROR_TEXT_4018);
    TestcaseData.getInstance().setActivateComfortSignatureResult(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isActivateComfortSignatureSoapFault4018());
  }

  @Test(expected = MissingPreconditionException.class)
  public void
      shouldNotValidateActivateComfortSignatureSoapFault4018OnMissingActivateComfortSignatureResult()
          throws MissingPreconditionException {
    TestcaseData.getInstance().setActivateComfortSignatureResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isActivateComfortSignatureSoapFault4018();
  }

  @Test
  public void shouldNotValidateActivateComfortSignatureOnInvalidSoapFault()
      throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSoapFault("ich bin nicht der richtige SOAP-Fault");
    TestcaseData.getInstance().setActivateComfortSignatureResult(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isActivateComfortSignatureSoapFault4018());
  }

  @Test
  public void shouldDeactivateComfortSignature() throws MissingPreconditionException, IOException {
    // Mocked applicationContext
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    // mocked BeanFactory
    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(configurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    // mocked ComfortSignatureDeActivator
    ComfortSignatureDeactivator comfortSignatureDeActivator =
        mock(ComfortSignatureDeactivator.class);
    when(beanFactory.getBean(ComfortSignatureDeactivator.class))
        .thenReturn(comfortSignatureDeActivator);

    ComfortSignatureResult result = new ComfortSignatureResult(STATUS_OK, "INACTIVE");
    when(comfortSignatureDeActivator.deactivateComfortSignature(CARD_HANDLE)).thenReturn(result);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);

    TestcaseData.getInstance()
        .setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    TestcaseData.getInstance().setHbaHandle(CARD_HANDLE);

    konnektorGlueCode.deactivateComfortSignature();
    Assert.assertTrue(
        "ComfortSignature should not be active",
        konnektorGlueCode.isDeactivateComfortSignatureOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDeactivateComfortSignatureTestWithoutCardHandle()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(prepareApplicationContextForDeactivationTest());
    TestcaseData.getInstance()
        .setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    TestcaseData.getInstance().setHbaHandle(null);
    konnektorGlueCode.deactivateComfortSignature();
  }

  @Test
  public void shouldDetermineSignatureMode() throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);

    ConfigurableApplicationContext configurableApplicationContext =
        determineContextForGetSignatureMode(invocationContext, SIGNATURE_MODE_COMFORT);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.determineSignatureMode();

    ComfortSignatureResult comfortSignatureResultAfterExecution =
        testcaseData.getGetSignatureModeResult();
    Assert.assertNotNull(
        "comfortSignatureResult sollte nicht null sein", comfortSignatureResultAfterExecution);
    Assert.assertTrue(
        "Die Komfortsignatur sollte aktiviert sein.",
        comfortSignatureResultAfterExecution.isComfortSignatureActivated());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineSignatureModeOnMissingCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineSignatureMode();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineSignatureModeOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineSignatureMode();
  }

  @Test
  public void shouldValidateIsSignatureModeComfort() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, SIGNATURE_MODE_COMFORT);
    TestcaseData.getInstance().setGetSignatureModeResult(comfortSignatureResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isSignatureModeComfort());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsSignatureModeComfortOnMissingComfortSignatureResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setGetSignatureModeResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isSignatureModeComfort());
  }

  @Test
  public void shouldDeactivateComfortSignatureIsFalse() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, SIGNATURE_MODE_COMFORT);

    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(prepareApplicationContextForDeactivationTest());

    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setComfortSignatureDeactivated(comfortSignatureResult);

    Assert.assertFalse(
        "ComfortSignature is still activated", konnektorGlueCode.isDeactivateComfortSignatureOk());
  }

  @Test
  public void shouldDetermineHbaHandle() throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineHbaHandle(invocationContext)).thenReturn(CARD_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineHbaHandle();
    Assert.assertEquals(CARD_HANDLE, testcaseData.getHbaHandle());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineHbaHandeOnMissingContext() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    konnektorGlueCode.determineHbaHandle();
  }

  @Test
  public void shouldDetermineSmcBHandle() throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineSmcBHandle(invocationContext)).thenReturn(SMC_B_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineSmcBHandle();
    Assert.assertEquals(SMC_B_HANDLE, testcaseData.getSmcBHandle());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineSmcBHandeOnMissingContext() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    konnektorGlueCode.determineSmcBHandle();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDeactivateComfortSignatureOnMissingContext()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    konnektorGlueCode.deactivateComfortSignature();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDeactivateComfortSignatureOnMissingCardHandle()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setHbaHandle(null);

    konnektorGlueCode.deactivateComfortSignature();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDeterminePinStatusQesFailWhenNoCardHandleIsSet()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    konnektorGlueCode.determinePinStatusQes();
    Assert.assertNotNull(testcaseData.getPinStatusResult());
    Assert.assertNotNull(testcaseData.getPinStatusResult().getPinStatusEnum());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDeterminePinStatusQesFailWhenNoInvocationContextIsSet()
      throws MissingPreconditionException {

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    // nun wieder "entfernen"
    testcaseData.setInvocationContext(null);
    // die MissingPrecondition Exception soll nun hier ausgelöst werden
    konnektorGlueCode.determinePinStatusQes();
    Assert.assertNotNull(testcaseData.getPinStatusResult());
    Assert.assertNotNull(testcaseData.getPinStatusResult().getPinStatusEnum());
  }

  @Test
  public void shouldDetermineSignatureModeComfort()
      throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);

    ConfigurableApplicationContext configurableApplicationContext =
        determineContextForGetSignatureMode(invocationContext, SIGNATURE_MODE_COMFORT);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.determineSignatureModeComfort();
    // es wurde keine Exception geworfen
    Assert.assertTrue(testcaseData.getGetSignatureModeResult().isComfortSignatureActivated());
  }

  private ConfigurableApplicationContext determineContextForGetSignatureMode(
      InvocationContext invocationContext, String signatureMode) throws IOException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(configurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    SignatureModeGetter signatureModeGetter = mock(SignatureModeGetter.class);
    when(beanFactory.getBean(SignatureModeGetter.class)).thenReturn(signatureModeGetter);

    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, signatureMode);
    when(signatureModeGetter.determineSignatureMode(CARD_HANDLE, invocationContext))
        .thenReturn(comfortSignatureResult);

    return configurableApplicationContext;
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineSignatureModeComfortOnSignatureModePin()
      throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);

    ConfigurableApplicationContext configurableApplicationContext =
        determineContextForGetSignatureMode(invocationContext, "PIN");
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.determineSignatureModeComfort();
    Assert.assertNotNull(testcaseData.getGetSignatureModeResult());
  }

  @Test
  public void shouldDetermineInvocationContext() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    Assert.assertNotNull(TestcaseData.getInstance().getInvocationContext());
  }

  @Test
  public void shouldDetermineInvocationContextWithUser() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER));
    Assert.assertNotNull(TestcaseData.getInstance().getInvocationContext());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDetermineInvocationContextFailWhenClientSystemIsNotSet()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, null, WORKPLACE));
    Assert.assertNotNull(TestcaseData.getInstance().getInvocationContext());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDetermineInvocationContextFailWhenMandantIsNotSet()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(null, CLIENT_SYSTEM, WORKPLACE));
    Assert.assertNotNull(TestcaseData.getInstance().getInvocationContext());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldDetermineInvocationContextFailWhenKontextIsNull()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(null);
    Assert.assertNotNull(TestcaseData.getInstance().getInvocationContext());
  }

  /**
   * bereitet Tests für <i>GetPinStatusResponse</i> vor
   *
   * @param invocationContext der AnmeldeKontext
   * @param pinStatusEnumValue einen erwarteten Wert aus {@link PinStatusEnum}
   * @return ConfigurableApplicationContext der dem <code>new KonnektorGlueCode(..)</code> übergeben
   *     werden kann.
   * @throws IOException im Fehlerfall
   */
  private ConfigurableApplicationContext prepareContextForGetPinStatusResponse(
      InvocationContext invocationContext, String pinStatusEnumValue) throws IOException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(configurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    PinStatus pinStatus = mock(PinStatus.class);
    when(beanFactory.getBean(PinStatus.class)).thenReturn(pinStatus);

    PinStatusResult pinStatusResult = mock(PinStatusResult.class);
    when(pinStatusResult.getPinStatusEnum())
        .thenReturn(PinStatusEnum.fromValue(pinStatusEnumValue));

    when(pinStatus.getPinStatusResponse(invocationContext, "PIN.QES", CARD_HANDLE))
        .thenReturn(pinStatusResult);
    return configurableApplicationContext;
  }

  @Test
  public void shouldIsGetPinStatusResponseVerifiable()
      throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(
            prepareContextForGetPinStatusResponse(
                invocationContext, PinStatusEnum.VERIFIABLE.value()));
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setPinStatusResult(
        new PinStatusResult(new Status(), BigInteger.ONE, PinStatusEnum.VERIFIABLE));
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  @Test
  public void shouldIsGetPinStatusResponseVerified()
      throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(
            prepareContextForGetPinStatusResponse(
                invocationContext, PinStatusEnum.VERIFIED.value()));
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setPinStatusResult(
        new PinStatusResult(new Status(), BigInteger.ONE, PinStatusEnum.VERIFIED));
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsGetPinStatusResponseVerifiedOnNoCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(null);

    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsGetPinStatusResponseVerifiableOnNoCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(null);

    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsGetPinStatusResponseVerifiedOnNoInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(null);
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsGetPinStatusResponseVerifiableOnNoInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(null);
    Assert.assertTrue(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  @Test
  public void shouldIsGetPinStatusResponseVerifiedWithWrongPinStatus()
      throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(
            prepareContextForGetPinStatusResponse(
                invocationContext, PinStatusEnum.EMPTY_PIN.value()));
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(invocationContext);
    Assert.assertFalse(konnektorGlueCode.isGetPinStatusResponseVerified());
  }

  @Test
  public void shouldIsGetPinStatusResponseVerifiableWithWrongPinStatus()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setPinStatusResult(
        new PinStatusResult(new Status(), BigInteger.ONE, PinStatusEnum.EMPTY_PIN));
    Assert.assertFalse(konnektorGlueCode.isGetPinStatusResponseVerifiable());
  }

  @Test
  public void shouldValidateIsSignatureModePin() throws MissingPreconditionException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSignatureMode(SIGNATURE_MODE_PIN);
    TestcaseData.getInstance().setGetSignatureModeResult(comfortSignatureResult);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isSignatureModePin());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailIsSignatureModePinOnMissingComfortSignatureResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setGetSignatureModeResult(null);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isSignatureModePin());
  }

  @Test
  public void shouldEnsureUserId() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineUserId();
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();
    String expectedUserId = invocationContext.getUser();
    konnektorGlueCode.ensureUserId();
    Assert.assertEquals(expectedUserId, invocationContext.getUser());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailEnsureUserIdOnMissingInvocationContext()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData.getInstance().setInvocationContext(null);
    konnektorGlueCode.ensureUserId();
  }

  @Test
  public void shouldVerifyPin() throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);

    PinVerifier pinVerifier = mock(PinVerifier.class);
    VerifyPinResult verifyPinResultExpected =
        new VerifyPinResult(STATUS_OK, PinResultEnum.OK.value());
    when(pinVerifier.performVerifyPin(invocationContext, CARD_HANDLE))
        .thenReturn(verifyPinResultExpected);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(PinVerifier.class)).thenReturn(pinVerifier);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.verifyPin();

    Assert.assertEquals(verifyPinResultExpected, testcaseData.getVerifyPinResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailVerifyPinOnMissingInvocationContext() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setHbaHandle(CARD_HANDLE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.verifyPin();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailVerifyPinOnMissingCardHandle() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.verifyPin();
  }

  @Test
  public void shouldNotValidateCheckSignDocumentHasFailed() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    SignDocumentResult signDocumentResultMock = mock(SignDocumentResult.class);
    when(signDocumentResultMock.isValidResponse()).thenReturn(true);
    when(signDocumentResultMock.getSignedBundle()).thenReturn(new byte[] {1, 2, 3, 4, 5});
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResultMock);
    boolean actual = konnektorGlueCode.checkSignDocumentHasFailed();
    Assert.assertFalse(
        MessageFormat.format(
            "Es wird ein Wert \"false\" erwartet. Der Wert ist jedoch \"{0}\".", actual),
        actual);
  }

  @Test(expected = MissingPreconditionException.class)
  public void checkSignDocumentWillFailOnMissingSignDocumentResultTest()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData.getInstance().setSignDocumentResult(null);
    boolean actual = konnektorGlueCode.checkSignDocumentHasFailed();
    Assert.assertTrue(
        MessageFormat.format(
            "Es wird ein Wert \"true\" erwartet. Der Wert ist jedoch \"{0}\".", actual),
        actual);
  }

  @Test
  public void checkSignDocumentHasFailedTestOnInvalidSignatureResultResponse()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    SignDocumentResult signDocumentResultMock = mock(SignDocumentResult.class);
    when(signDocumentResultMock.isValidResponse()).thenReturn(false);
    boolean actual = konnektorGlueCode.checkSignDocumentHasFailed();
    Assert.assertTrue(
        MessageFormat.format(
            "Es wird ein Wert \"true\" erwartet. Der Wert ist jedoch \"{0}\".", actual),
        actual);
  }

  @Test
  public void shouldValidateVerifyPinResult() throws MissingPreconditionException {
    VerifyPinResult verifyPinResult =
        new VerifyPinResult(VerifyPinResult.STATUS_OK, VerifyPinResult.PIN_RESULT_OK);
    TestcaseData.getInstance().setVerifyPinResult(verifyPinResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isVerifyPinOk());
  }

  @Test
  public void shouldNotValidateVerifyPinResult() throws MissingPreconditionException {
    VerifyPinResult verifyPinResult = new VerifyPinResult("NOK", VerifyPinResult.PIN_RESULT_OK);
    TestcaseData.getInstance().setVerifyPinResult(verifyPinResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isVerifyPinOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotValidateVerifyPinResultOnMissingVerifyPinResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setVerifyPinResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isVerifyPinOk();
  }

  @Test
  public void shouldEjectCard() throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(CARD_HANDLE);

    CardEjector cardEjector = mock(CardEjector.class);
    EjectCardResult ejectCardResultExpected = new EjectCardResult(STATUS_OK);
    when(cardEjector.performEjectCard(invocationContext, CARD_HANDLE))
        .thenReturn(ejectCardResultExpected);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardEjector.class)).thenReturn(cardEjector);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.ejectCard();

    Assert.assertEquals(ejectCardResultExpected, testcaseData.getEjectCardResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailEjectCardOnMissingInvocationContext() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setHbaHandle(CARD_HANDLE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.ejectCard();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailEjectCardOnMissingCardHandle() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setHbaHandle(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.ejectCard();
  }

  @Test
  public void shouldValidateEjectCardResult() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = new EjectCardResult(RESULT_OK);
    TestcaseData.getInstance().setEjectCardResult(ejectCardResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isEjectCardOk());
  }

  @Test
  public void shouldNotValidateEjectCardResultOnWarning() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = new EjectCardResult(RESULT_WARNING);
    TestcaseData.getInstance().setEjectCardResult(ejectCardResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isEjectCardOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotValidateEjectCardResultOnMissingEjectCardResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setEjectCardResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isEjectCardOk();
  }

  @Test
  public void shouldDetermineCardTerminals() throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    testcaseData.setInvocationContext(invocationContext);

    CardTerminalsGetter cardTerminalsGetter = mock(CardTerminalsGetter.class);
    GetCardTerminalsResult getCardTerminalsResultExpected =
        new GetCardTerminalsResult(STATUS_OK, CT_ID);
    when(cardTerminalsGetter.performGetCardTerminals(invocationContext))
        .thenReturn(getCardTerminalsResultExpected);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardTerminalsGetter.class))
        .thenReturn(cardTerminalsGetter);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.determineCardTerminals();

    Assert.assertEquals(getCardTerminalsResultExpected, testcaseData.getGetCardTerminalsResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailDetermineCardTerminalsOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setInvocationContext(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineCardTerminals();
  }

  @Test
  public void shouldValidateGetCardTerminalsResponse() throws MissingPreconditionException {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(RESULT_OK, CT_ID);
    TestcaseData.getInstance().setGetCardTerminalsResult(getCardTerminalsResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isGetCardTerminalsOk());
  }

  @Test
  public void shouldNotValidateGetCardTerminalsResponse() throws MissingPreconditionException {
    GetCardTerminalsResult getCardTerminalsResult =
        new GetCardTerminalsResult(RESULT_WARNING, CT_ID);
    TestcaseData.getInstance().setGetCardTerminalsResult(getCardTerminalsResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isGetCardTerminalsOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingGetCardTerminalsResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setGetCardTerminalsResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isGetCardTerminalsOk();
  }

  @Test
  public void shouldRequestCard() throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    testcaseData.setInvocationContext(invocationContext);

    CardRequestor cardRequestor = mock(CardRequestor.class);
    RequestCardResult expectedRequestCardResult = new RequestCardResult(STATUS_OK, CARD_HANDLE);
    when(cardRequestor.performRequestCard(invocationContext, CT_ID, SLOT_2))
        .thenReturn(expectedRequestCardResult);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardRequestor.class)).thenReturn(cardRequestor);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.requestCard(CT_ID, SLOT_2);

    Assert.assertEquals(expectedRequestCardResult, testcaseData.getRequestCardResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailRequestCardOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(STATUS_OK, CT_ID);
    testcaseData.setGetCardTerminalsResult(getCardTerminalsResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.requestCard(CT_ID, SLOT_2);
  }

  @Test
  public void shouldValidateRequestCardResponse() throws MissingPreconditionException {
    RequestCardResult requestCardResult = new RequestCardResult(STATUS_OK, CARD_HANDLE);
    TestcaseData.getInstance().setRequestCardResult(requestCardResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isRequestCardOk());
  }

  @Test
  public void shouldNotValidateRequestCardResponse() throws MissingPreconditionException {
    RequestCardResult requestCardResult = new RequestCardResult(RESULT_WARNING, CARD_HANDLE);
    TestcaseData.getInstance().setRequestCardResult(requestCardResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isRequestCardOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingRequestCardResult() throws MissingPreconditionException {
    TestcaseData.getInstance().setRequestCardResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isRequestCardOk());
  }

  @Test
  public void shouldValidateExternalAuthenticateResponse() throws MissingPreconditionException {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, SIGNED_DATA);
    TestcaseData.getInstance().setExternalAuthenticateResult(externalAuthenticateResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isExternalAuthenticateOk());
  }

  @Test
  public void shouldNotValidateExternalAuthenticateResponse() throws MissingPreconditionException {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, null);
    TestcaseData.getInstance().setExternalAuthenticateResult(externalAuthenticateResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isExternalAuthenticateOk());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingExternalAuthenticateResult()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setExternalAuthenticateResult(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.isExternalAuthenticateOk();
  }

  @Test
  public void shouldDetermineHbaHandleWithIccsn() throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);
    TestcaseData.getInstance().setHbaHandle(null);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineHbaHandle(invocationContext, "ICCSN")).thenReturn(CARD_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineHbaHandle("ICCSN");
    Assert.assertEquals(CARD_HANDLE, testcaseData.getHbaHandle());
  }

  @Test
  public void shouldDetermineSmcBHandleWithICCSN()
      throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);
    TestcaseData.getInstance().setSmcBHandle(null);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineSmcBHandle(invocationContext, "DUMMY-ICCSN"))
        .thenReturn(SMC_B_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineSmcBHandle("DUMMY-ICCSN");
    Assert.assertEquals(SMC_B_HANDLE, testcaseData.getSmcBHandle());
  }

  @Test
  public void shouldDetermineHBAqSigHandleWithAndWithoutIccsn()
      throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineHBAQSigHandle(invocationContext, "HBAqSig-ICCSN"))
        .thenReturn(CARD_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setSmcBHandle(null);
    testcaseData.setHbaHandle(null);
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineHBAqSigHandle("HBAqSig-ICCSN");
    Assert.assertEquals(
        "Required Card Handle with ICCSN did not match.", CARD_HANDLE, testcaseData.getHbaHandle());
    // second case (without iccsn)
    testcaseData.setHbaHandle(null);
    testcaseData.setSmcBHandle(null);
    when(cardHandleFinder.determineHBAQSigHandle(invocationContext)).thenReturn(CARD_HANDLE);
    konnektorGlueCode.determineHBAqSigHandle();
    Assert.assertEquals(
        "Required Card Handle without ICCSN did not match",
        CARD_HANDLE,
        testcaseData.getHbaHandle());
  }

  @Test
  public void shouldDetermineZOD20HandleWithAndWithoutICCSN()
      throws IOException, MissingPreconditionException {
    CardHandleFinder cardHandleFinder = mock(CardHandleFinder.class);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(cardHandleFinder.determineZOD20Handle(invocationContext, "DUMMY-ZOD20-ICCSN"))
        .thenReturn(CARD_HANDLE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(CardHandleFinder.class))
        .thenReturn(cardHandleFinder);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    testcaseData.setSmcBHandle(null);
    testcaseData.setInvocationContext(invocationContext);
    konnektorGlueCode.determineZOD20Handle("DUMMY-ZOD20-ICCSN");
    Assert.assertEquals(
        "Required Card Handle with ICCSN did not match", CARD_HANDLE, testcaseData.getHbaHandle());

    // second case (without iccsn)
    testcaseData.setHbaHandle(null);
    testcaseData.setSmcBHandle(null);
    when(cardHandleFinder.determineZOD20Handle(invocationContext)).thenReturn(CARD_HANDLE);
    konnektorGlueCode.determineZOD20Handle();
    Assert.assertEquals(
        "Required Card Handle without ICCSN did not match",
        CARD_HANDLE,
        testcaseData.getHbaHandle());
  }

  @Test
  public void shouldBeEjectCardSoapFault4203() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = new EjectCardResult();
    ejectCardResult.setSoapFault(EjectCardResult.ERROR_TEXT_4203);
    TestcaseData.getInstance().setEjectCardResult(ejectCardResult);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isEjectCardSoapFault4203());
  }

  @Test
  public void shouldNotBeEjectCardSoapFault4203() throws MissingPreconditionException {
    EjectCardResult ejectCardResult = new EjectCardResult();
    ejectCardResult.setSoapFault(ERROR_TEXT_4018);
    TestcaseData.getInstance().setEjectCardResult(ejectCardResult);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertFalse(konnektorGlueCode.isEjectCardSoapFault4203());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingEjectCardResult() throws MissingPreconditionException {
    TestcaseData.getInstance().setEjectCardResult(null);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Assert.assertTrue(konnektorGlueCode.isEjectCardSoapFault4203());
  }

  @Test
  public void shouldSwitchWorkplace() throws MissingPreconditionException {
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.switchWorkplace(WORKPLACE_2);
    Assert.assertEquals(invocationContext.getMandant(), MANDANT);
    Assert.assertEquals(invocationContext.getClientSystem(), CLIENT_SYSTEM);
    Assert.assertEquals(invocationContext.getWorkplace(), WORKPLACE_2);
    Assert.assertEquals(invocationContext.getUser(), USER);
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailSwitchWorkplaceOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setInvocationContext(null);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.switchWorkplace(WORKPLACE_2);
  }
}
