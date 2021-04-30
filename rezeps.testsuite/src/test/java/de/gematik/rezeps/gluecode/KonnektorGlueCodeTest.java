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
import de.gematik.rezeps.UserIdHelper;
import de.gematik.rezeps.authentication.ExternalAuthenticator;
import de.gematik.rezeps.card.CardHandleFinder;
import de.gematik.rezeps.card.PinStatus;
import de.gematik.rezeps.card.PinStatusResult;
import de.gematik.rezeps.certificate.CardCertificateReader;
import de.gematik.rezeps.comfortsignature.ComfortSignatureActivator;
import de.gematik.rezeps.comfortsignature.ComfortSignatureDeactivator;
import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import de.gematik.rezeps.comfortsignature.SignatureModeGetter;
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.rezeps.util.CommonUtils;
import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
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
  private static final String USER = "userId";
  private static final String CARD_HANDLE = "HBA-1";
  private static final String SMC_B_HANDLE = "SMC_B-1";
  private static final byte[] EXPECTED_AUT_CERTIFICATE =
      "Ich bin ein AUT-Zertifikat".getBytes(StandardCharsets.UTF_8);
  private static final byte[] DATA_TO_BE_SIGNED =
      "ich moechte signiert werden".getBytes(StandardCharsets.UTF_8);
  private static final byte[] SIGNED_DATA = "ich bin signiert".getBytes(StandardCharsets.UTF_8);
  private static final byte[] CODE_CHALLENGE = "code_challenge".getBytes(StandardCharsets.UTF_8);
  private static final String SIGNATURE_MODE_PIN = "PIN";

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
  public void shouldAuthenticateExternally() throws MissingPreconditionException, IOException {
    ExternalAuthenticator externalAuthenticator = mock(ExternalAuthenticator.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(externalAuthenticator.authenticateExternally(
            invocationContext, SMC_B_HANDLE, CODE_CHALLENGE))
        .thenReturn(SIGNED_DATA);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(invocationContext);
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    ConfigurableListableBeanFactory configurableListableBeanFactory =
        mock(ConfigurableListableBeanFactory.class);
    when(configurableListableBeanFactory.getBean(ExternalAuthenticator.class))
        .thenReturn(externalAuthenticator);

    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    when(configurableApplicationContext.getBeanFactory())
        .thenReturn(configurableListableBeanFactory);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternally();

    Assert.assertEquals(SIGNED_DATA, testcaseData.getAuthenticatedData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyOnMissingInvocationContext()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternally();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyOnMissingCardHandle()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setSmcBHandle(null);
    testcaseData.setCodeChallenge(CODE_CHALLENGE);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.authenticateExternally();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldNotAuthenticateExternallyOnEmptyDataToBeSigned()
      throws MissingPreconditionException {
    ConfigurableApplicationContext configurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    testcaseData.setSmcBHandle(SMC_B_HANDLE);
    testcaseData.setCodeChallenge(new byte[] {});

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(configurableApplicationContext);
    konnektorGlueCode.authenticateExternally();
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
  public void shouldWriteUserIdToFile() throws MissingPreconditionException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.determineContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    konnektorGlueCode.determineUserId();
    String expectedUserId = TestcaseData.getInstance().getInvocationContext().getUser();
    String userIdFromFile = UserIdHelper.readUserIdFromFile();
    Assert.assertEquals(expectedUserId, userIdFromFile);
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
      throws MissingPreconditionException, IOException {

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
    invocationContext.setUser("invalid user");
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

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailEnsureUserIdOnMissingUserId()
      throws MissingPreconditionException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    TestcaseData.getInstance()
        .setInvocationContext(new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE));
    UserIdHelper.deleteUserIdFile();
    konnektorGlueCode.ensureUserId();
  }
}
