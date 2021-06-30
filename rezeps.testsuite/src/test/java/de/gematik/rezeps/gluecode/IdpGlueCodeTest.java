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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.token.JsonWebToken;
import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.authentication.ExternalAuthenticateResult;
import de.gematik.rezeps.authentication.ExternalAuthenticator;
import de.gematik.rezeps.certificate.CardCertificateReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.function.Function;
import javax.xml.bind.DatatypeConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class IdpGlueCodeTest {

  public static final String STATUS_OK = "OK";
  private ExternalAuthenticator externalAuthenticator;
  private CardCertificateReader cardCertificateReader;
  private ConfigurableListableBeanFactory beanFactory;
  private ConfigurableApplicationContext applicationContext;
  private IdpClient idpClient;
  private IdpGlueCode idpGlueCode;

  private static final String X509_CERT =
      "-----BEGIN CERTIFICATE-----\n"
          + "MIIBhjCCASwCFBXmab5AlBwaj5Bir+yZ6lZWmcfsMAoGCCqGSM49BAMCMEUxCzAJ\n"
          + "BgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5l\n"
          + "dCBXaWRnaXRzIFB0eSBMdGQwHhcNMjAxMTIzMTExMTU3WhcNMjMwODIxMTExMTU3\n"
          + "WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwY\n"
          + "SW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMFowFAYHKoZIzj0CAQYJKyQDAwIIAQEH\n"
          + "A0IABAjPjOhFN4IsgF7+HOgwHTyCYj+fAa8xWci9fC27/p8uNng/dGsh3bTs6LBZ\n"
          + "DHLrZ0Ge/gxPPU/FNc1VNhvYzP0wCgYIKoZIzj0EAwIDSAAwRQIhAIOKkCs1l4Et\n"
          + "DkHUaPYGCQzoSZhsCshhs923SHOpalAUAiAvTd8UCtLw/OqTce+xBktsUjS11E5/\n"
          + "tjaXr4O5AFugLA==                                                \n"
          + "-----END CERTIFICATE-----\n";
  private static final String ACCESS_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  private static final byte[] CHALLANGE =
      DatatypeConverter.parseHexBinary(
          "36030fcc7e566294905b49a720eb45bf962209d2ee1c9b73e2b7bc7ae8830376");
  private static final byte[] HASH =
      DatatypeConverter.parseHexBinary(
          "9b6310ee431b7aec8ac083f96ce5d98a83305af970b86647ce9838cc05a8f94d");
  private static final byte[] FINAL_HASH =
      DatatypeConverter.parseHexBinary(
          "20b71a57a80df028321e5c3d116e9d89a5d6bc7049562bbaceece11446fd4fee");

  @Before
  public void setup() {
    externalAuthenticator = mock(ExternalAuthenticator.class);

    cardCertificateReader = mock(CardCertificateReader.class);

    beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(beanFactory.getBean(ExternalAuthenticator.class)).thenReturn(externalAuthenticator);
    when(beanFactory.getBean(CardCertificateReader.class)).thenReturn(cardCertificateReader);

    applicationContext = mock(ConfigurableApplicationContext.class);
    when(applicationContext.getBeanFactory()).thenReturn(beanFactory);

    idpClient = mock(IdpClient.class);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(
        new InvocationContext("test-mandant", "test-client-system", "test-workplace"));

    idpGlueCode = new IdpGlueCode(applicationContext, idpClient);
  }

  @After
  public void tearDown() {
    externalAuthenticator = null;
    cardCertificateReader = null;
    beanFactory = null;
    applicationContext = null;
    idpClient = null;
    idpGlueCode = null;

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setInvocationContext(null);
  }

  @Test
  public void shouldObtainAccessTokenWithHbaHandle() throws IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle("test-hba-handle");

    when(cardCertificateReader.readCardCertificate(any(), eq("test-hba-handle")))
        .thenReturn(X509_CERT.getBytes());
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, FINAL_HASH);
    when(externalAuthenticator.authenticateExternally(any(), eq("test-hba-handle"), eq(HASH)))
        .thenReturn(externalAuthenticateResult);
    when(idpClient.login(any(X509Certificate.class), any(Function.class)))
        .thenAnswer(new LoginAnswer());

    idpGlueCode.obtainAccessToken();

    IdpTokenResult idpTokenResult = testcaseData.getIdpTokenResult();
    Assert.assertNotNull(idpTokenResult);
  }

  @Test
  public void shouldObtainAccessTokenWithSmcBHandle() throws IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    testcaseData.setSmcBHandle("test-smc-b-handle");

    when(cardCertificateReader.readCardCertificate(any(), eq("test-smc-b-handle")))
        .thenReturn(X509_CERT.getBytes());
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, FINAL_HASH);
    when(externalAuthenticator.authenticateExternally(any(), eq("test-smc-b-handle"), eq(HASH)))
        .thenReturn(externalAuthenticateResult);
    when(idpClient.login(any(X509Certificate.class), any(Function.class)))
        .thenAnswer(new LoginAnswer());

    idpGlueCode.obtainAccessToken();

    IdpTokenResult idpTokenResult = testcaseData.getIdpTokenResult();
    Assert.assertNotNull(idpTokenResult);
  }

  @Test
  public void shouldNotObtainAccessTokenWithoutHandle() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    testcaseData.setSmcBHandle(null);
    idpGlueCode.obtainAccessToken();
    IdpTokenResult idpTokenResult = testcaseData.getIdpTokenResult();
    Assert.assertNull(idpTokenResult);
  }

  @Test
  public void shouldObtainAccessTokenDispensingEntityWithValidCardHandle()
      throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setSmcBHandle("test-smc-b-handle");

    when(cardCertificateReader.readCardCertificate(any(), eq("test-smc-b-handle")))
        .thenReturn(X509_CERT.getBytes());
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, FINAL_HASH);
    when(externalAuthenticator.authenticateExternally(any(), eq("test-smc-b-handle"), eq(HASH)))
        .thenReturn(externalAuthenticateResult);
    when(idpClient.login(any(X509Certificate.class), any(Function.class)))
        .thenAnswer(new LoginAnswer());

    idpGlueCode.obtainAccessTokenDispensingEntity();

    String accessToken = testcaseData.getAccessTokenDispensingEntity();
    Assert.assertNotNull(accessToken);
  }

  @Test
  public void shouldNotObtainAccessTokenDispensingEntityWithoutCardHandle() throws IOException {
    TestcaseData.getInstance().setSmcBHandle(null);
    Assert.assertThrows(
        MissingPreconditionException.class,
        () -> {
          idpGlueCode.obtainAccessTokenDispensingEntity();
        });
  }

  @Test
  public void shouldObtainAccessTokenPrescribingEntityWithValidCardHandle()
      throws IOException, MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle("test-hba-handle");

    when(cardCertificateReader.readCardCertificate(any(), eq("test-hba-handle")))
        .thenReturn(X509_CERT.getBytes());
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(STATUS_OK, FINAL_HASH);
    when(externalAuthenticator.authenticateExternally(any(), eq("test-hba-handle"), eq(HASH)))
        .thenReturn(externalAuthenticateResult);
    when(idpClient.login(any(X509Certificate.class), any(Function.class)))
        .thenAnswer(new LoginAnswer());

    idpGlueCode.obtainAccessTokenPrescribingEntity();

    String accessToken = testcaseData.getAccessTokenPrescribingEntity();
    Assert.assertNotNull(accessToken);
  }

  @Test
  public void shouldNotObtainAccessTokenPrescribingEntityWithoutCardHandle() {
    TestcaseData.getInstance().setHbaHandle(null);
    Assert.assertThrows(
        MissingPreconditionException.class,
        () -> {
          idpGlueCode.obtainAccessTokenPrescribingEntity();
        });
  }

  class LoginAnswer implements Answer<IdpTokenResult> {

    @Override
    public IdpTokenResult answer(InvocationOnMock invocation) throws Throwable {
      Object[] args = invocation.getArguments();
      Function<byte[], byte[]> callback = (Function<byte[], byte[]>) args[1];
      byte[] ret = callback.apply(CHALLANGE);

      Assert.assertEquals(FINAL_HASH, ret);

      IdpTokenResult idpTokenResult = new IdpTokenResult();
      idpTokenResult.setAccessToken(new JsonWebToken(ACCESS_TOKEN));

      return idpTokenResult;
    }
  }

  @Test
  public void checkSecretAvailableTest() throws MissingPreconditionException, IOException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setAccessTokenDispensingEntity("sample");
    Assert.assertTrue(idpGlueCode.checkSecretAvailable());
  }

  @Test
  public void shouldGenerateRandomCodeChallenge() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setCodeChallenge(null);

    IdpGlueCode idpGlueCode = new IdpGlueCode(null, null);
    idpGlueCode.generateRandomCodeChallenge();

    byte[] codeChallenge = testcaseData.getCodeChallenge();
    Assert.assertNotNull("Die code challenge sollte nicht null sein.", codeChallenge);
    Assert.assertEquals(
        "Die code challenge hat nicht die erwartete Länge",
        IdpGlueCode.BYTES_IN_256_BITS,
        codeChallenge.length);
  }
}
