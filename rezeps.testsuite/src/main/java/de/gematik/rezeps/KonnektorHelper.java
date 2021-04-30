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

package de.gematik.rezeps;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender.RemoveSoapHeadersInterceptor;

/** Stellt Hilfsmethoden für den Zugriff auf den Konnektor zur Verfügung. */
public class KonnektorHelper {

  private static final String[] SUPPORTED_PROTOCOLS = new String[] {"TLSv1.2"};
  private static final String[] SUPPORTED_CIPHER_SUITES =
      new String[] {"TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA"};
  public static final String STRING_PROTOKOLL_HTTPS = "https://";

  private KonnektorHelper() {}

  /**
   * Bestimmt den Konnektor-Endpunkt des EventService.
   *
   * @return Konnektor-Endpunkt des EventService.
   */
  public static String determineEventServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorEventServiceEndpoint();
  }

  private static String determineKonnektorEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    return configurationReader.getKonnektorIp() + ":" + configurationReader.getKonnektorPort();
  }

  /**
   * Bestimmt den Konnektor-Endpunkt des Signaturdienstes.
   *
   * @return Konnektor-Endpunkt des Signaturdienstes.
   */
  public static String determineSignatureServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorSignatureServiceEndpoint();
  }

  /**
   * Bestimmt den Konnektor-Endpunkt des AuthSignatureService.
   *
   * @return Konnektor-Endpunkt des AuthSignatureService.
   */
  public static String determineAuthSignatureServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorAuthSignatureServiceEndpoint();
  }
  /**
   * Bestimmt den Konnektor-Endpunkt des CardService.
   *
   * @return Konnektor-Endpunkt des CardService.
   */
  public static String determineCardServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorCardServiceEndpoint();
  }

  /**
   * Bestimmt den Konnektor-Endpunkt des CardTerminalService
   *
   * @return {@link String} Konnektor Service Endpunkt für 'CardTerminalService'
   * @throws IOException, wenn dieser nicht bestimmt werden kann
   */
  public static String determineCardTerminalServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorCardTerminalServiceEndpoint();
  }

  /**
   * Bestimmt den Konnektor-Endpunkt des Zertifikatsdienstes.
   *
   * @return Konnektor-Endpunkt des Zertifikatsdienstes.
   */
  public static String determineCertificateServiceEndpoint() throws IOException {
    return STRING_PROTOKOLL_HTTPS
        + determineKonnektorEndpoint()
        + ConfigurationReader.getInstance().getKonnektorCertificateServiceEndpoint();
  }

  /**
   * Konfiguriert einen HttpComponentsMessageSender für die TLS-gesicherte Kommunikation zum
   * Konnektor mittels Zertifikats-basierter Client-Authentisierung.
   *
   * @return HttpComponentsMessageSender für die TSL-gesicherte Kommunikation zum Konnektor mittels
   *     Zertifikats-basierter Client-Authentisierung.
   */
  public static HttpComponentsMessageSender determineHttpComponentsMessageSender()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
          KeyManagementException, UnrecoverableKeyException {
    HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();

    // NoopHostnameVerifier essentially turns hostname verification off
    SSLConnectionSocketFactory sslConnectionSocketFacktory =
        new SSLConnectionSocketFactory(
            determineSslContext(),
            SUPPORTED_PROTOCOLS,
            SUPPORTED_CIPHER_SUITES,
            NoopHostnameVerifier.INSTANCE);

    CloseableHttpClient httpClient =
        HttpClientBuilder.create()
            .setSSLSocketFactory(sslConnectionSocketFacktory)
            .addInterceptorFirst(new RemoveSoapHeadersInterceptor())
            .build();

    httpComponentsMessageSender.setHttpClient(httpClient);

    return httpComponentsMessageSender;
  }

  private static SSLContext determineSslContext()
      throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
          KeyStoreException, KeyManagementException {

    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    File clientKeystore = new File(configurationReader.getPathToClientKeystore());
    char[] clientKeystorePassword = configurationReader.getClientKeystorePassword().toCharArray();
    File clientTruststore = new File(configurationReader.getPathToClientTruststore());
    char[] clientTruststorePassword =
        configurationReader.getClientTruststorePassword().toCharArray();

    return SSLContextBuilder.create()
        .loadKeyMaterial(clientKeystore, clientKeystorePassword, clientKeystorePassword)
        .loadTrustMaterial(clientTruststore, clientTruststorePassword)
        .build();
  }
}
