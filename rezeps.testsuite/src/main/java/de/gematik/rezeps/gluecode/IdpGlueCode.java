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

import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.rezeps.ConfigurationReader;
import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.authentication.ExternalAuthenticateResult;
import de.gematik.rezeps.authentication.ExternalAuthenticator;
import de.gematik.rezeps.certificate.CardCertificateReader;
import de.gematik.rezeps.util.CommonUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

/** Ermöglich die Kommunikation mit dem IDP Dienst. */
public class IdpGlueCode {

  public static final int BYTES_IN_256_BITS = 32;

  private static final Logger LOGGER = LoggerFactory.getLogger(IdpGlueCode.class);

  private final ConfigurableApplicationContext applicationContext;
  private final IdpClient idpClient;

  public IdpGlueCode(ConfigurableApplicationContext applicationContext) throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String discoveryUrl = configurationReader.getIdpDiscoveryUrl();
    String redirectUrl = configurationReader.getIdpRedirectUrl();
    String clientId = configurationReader.getIdpClientId();
    // "Unirest.config().addDefaultHeader("X-Auth",
    // "MTRqU2cwPXx+Pit4aCVUT2pNVVN2VDllPj1cUUUqCg==");"
    this.applicationContext = applicationContext;
    this.idpClient =
        IdpClient.builder()
            .discoveryDocumentUrl(discoveryUrl)
            .redirectUrl(redirectUrl)
            .clientId(clientId)
            .build();
    this.idpClient.initialize();
  }

  public IdpGlueCode(ConfigurableApplicationContext applicationContext, IdpClient idpClient) {
    this.applicationContext = applicationContext;
    this.idpClient = idpClient;
  }

  /**
   * Holt einen gültigen Access Token vom IDP für die HBA oder SMC-B Karte. Die Daten der Response,
   * die im weiteren Testablauf benötigt werden, werden im Objekt TestcaseData bereitgestellt.
   */
  public void obtainAccessToken() {
    try {
      TestcaseData testcaseData = TestcaseData.getInstance();
      String hbaHandle = TestcaseData.getInstance().getHbaHandle();
      String smcBHandle = TestcaseData.getInstance().getSmcBHandle();

      IdpTokenResult idpTokenResult;
      if (!CommonUtils.isNullOrEmpty(hbaHandle)) {
        idpTokenResult = obtainAccessTokenInternal(hbaHandle);
        testcaseData.setAccessTokenPrescribingEntity(
            idpTokenResult.getAccessToken().getRawString());
      } else if (!CommonUtils.isNullOrEmpty(smcBHandle)) {
        idpTokenResult = obtainAccessTokenInternal(smcBHandle);
        testcaseData.setAccessTokenDispensingEntity(idpTokenResult.getAccessToken().getRawString());
      } else {
        throw new MissingPreconditionException("Not HBA or SMB-C card handle detected!");
      }

      testcaseData.setIdpTokenResult(idpTokenResult);
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }

  /**
   * Holt einen gültigen Access Token für den abgebenden LE vom IDP und speichert diesen zur
   * weiteren Verwendung in der TestcaseData ab.
   *
   * @throws MissingPreconditionException
   */
  public void obtainAccessTokenDispensingEntity() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String cardHandle = testcaseData.getSmcBHandle();
    if (CommonUtils.isNullOrEmpty(cardHandle)) {
      throw new MissingPreconditionException("Es wurde kein SMC-B-Handle ermittelt.");
    }

    try {
      IdpTokenResult idpTokenResult = obtainAccessTokenInternal(cardHandle);
      testcaseData.setAccessTokenDispensingEntity(idpTokenResult.getAccessToken().getRawString());
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }

  /**
   * Holt einen gültigen Access Token für das verordnende PS vom IDP und speichert diesen zur
   * weiteren Verwendung in der TestcaseData ab.
   *
   * @throws MissingPreconditionException
   */
  public void obtainAccessTokenPrescribingEntity() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    String cardHandle = testcaseData.getHbaHandle();
    if (CommonUtils.isNullOrEmpty(cardHandle)) {
      throw new MissingPreconditionException("Es wurde kein HBA-Handle ermittelt.");
    }

    try {
      IdpTokenResult idpTokenResult = obtainAccessTokenInternal(cardHandle);
      testcaseData.setAccessTokenPrescribingEntity(idpTokenResult.getAccessToken().getRawString());
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }

  /**
   * Check if the IDP returned a valid ACCESS_TOKEN.
   *
   * @return TRUE if a valid ACCESS_TOKEN was obtained, FALSE otherwise.
   */
  public boolean checkAccessTokenOk() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    IdpTokenResult idpTokenResult = testcaseData.getIdpTokenResult();

    return idpTokenResult != null && idpTokenResult.getAccessToken() != null;
  }

  /**
   * Holt für das übergebene cardHandle einen gültigen AccessToken vom IDP ab.
   *
   * @param cardHandle Karten Handle für das der Access Token ermittelt werden soll.
   * @return Antwort des IDP Dienstes.
   * @throws IOException
   * @throws CertificateException
   */
  private IdpTokenResult obtainAccessTokenInternal(String cardHandle)
      throws IOException, CertificateException {
    InvocationContext invocationContext = TestcaseData.getInstance().getInvocationContext();

    ExternalAuthenticator externalAuthenticator =
        applicationContext.getBeanFactory().getBean(ExternalAuthenticator.class);

    CardCertificateReader cardCertificateReader =
        applicationContext.getBeanFactory().getBean(CardCertificateReader.class);
    byte[] certData = cardCertificateReader.readCardCertificate(invocationContext, cardHandle);

    InputStream certStream = new ByteArrayInputStream(certData);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);

    return idpClient.login(
        cert,
        challenge -> {
          try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256"); // NOSONAR
            digest.update(challenge);
            byte[] hash = digest.digest();

            ExternalAuthenticateResult externalAuthenticateResult =
                externalAuthenticator.authenticateExternally(invocationContext, cardHandle, hash);
            return externalAuthenticateResult != null
                ? externalAuthenticateResult.getAuthenticatedData()
                : null;
          } catch (Exception ex) {
            LOGGER.warn("Unable to authenticate externally: {}", ex.getMessage());
            return new byte[] {};
          }
        });
  }

  /**
   * Prüft, ob ein Access Token für den abgebenden LE vom IDP in TestcaseData vorliegt
   *
   * @return true, wenn ein Access Token vorliegt
   * @throws MissingPreconditionException im Fehlerfall
   */
  public boolean checkSecretAvailable() throws MissingPreconditionException {
    return !CommonUtils.isNullOrEmpty(TestcaseData.getInstance().getAccessTokenDispensingEntity());
  }
  /**
   * Generiert eine zufällige code_challenge und speichert diese für die weitere Verarbeitung in
   * TestcaseData.
   */
  public void generateRandomCodeChallenge() {
    byte[] codeChallenge = CommonUtils.generateRandomBytes(BYTES_IN_256_BITS);
    TestcaseData.getInstance().setCodeChallenge(codeChallenge);
  }
}
