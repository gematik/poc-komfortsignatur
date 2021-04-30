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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationReaderTest {

  private static final String EXPECTED_KONNEKTOR_IP = "127.0.0.1";
  private static final String EXPECTED_KONNEKTOR_PORT = "443";
  private static final String EXPECTED_ACCESS_TOKEN_PRESCRIBING_ENTITY = "0-8-15-4711";
  private static final String EXPECTED_ACCESS_TOKEN_DISPENSING_ENTITY = "41-42-43";
  private static final String EXPECTED_KONNEKTOR_AUTH_SIGNATURE_SERVICE_ENDPOINT =
      "/authsignatureservice";
  private static final String EXPECTED_KONNEKTOR_CERTIFICATE_SERVICE_ENDPOINT =
      "/certificateservice";
  private static final String EXPECTED_KONNEKTOR_EVENT_SERVICE_ENDPOINT = "/eventservice";
  private static final String EXPECTED_KONNEKTOR_SIGNATURE_SERVICE_ENDPOINT = "/signatureservice";
  private static final String EXPECTED_PATH_TO_CLIENT_KEYSTORE =
      "src/test/resources/kops-client-keystore.p12";
  private static final String EXPECTED_CLIENT_KEYSTORE_PASSWORD = "123456";
  private static final String EXPECTED_PATH_TO_CLIENT_TRUSTSTORE =
      "src/test/resources/kops-client-truststore.p12";
  private static final String EXPECTED_CLIENT_TRUSTSTORE_PASSWORD = "654321";
  private static final String EXPECTED_KONNEKTOR_CARD_SERVICE_ENDPOINT = "/cardservice";
  private static final String EXPECTED_KONNEKTOR_CARD_TERMINAL_SERVICE_ENDPOINT =
      "/cardterminalservice";

  @Test
  public void shouldDetermineKonnektorIp() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorIp = configurationReader.getKonnektorIp();
    Assert.assertEquals(EXPECTED_KONNEKTOR_IP, konnektorIp);
  }

  @Test
  public void shouldDetermineKonnektorPort() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorPort = configurationReader.getKonnektorPort();
    Assert.assertEquals(EXPECTED_KONNEKTOR_PORT, konnektorPort);
  }

  @Test
  public void shouldDetermineAccessTokenPrescribingEntity() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String accessTokenPrescribingEntity = configurationReader.getAccessTokenPrescribingEntity();
    Assert.assertEquals(EXPECTED_ACCESS_TOKEN_PRESCRIBING_ENTITY, accessTokenPrescribingEntity);
  }

  @Test
  public void shouldDetermineAccessTokenDispensingEntity() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String accessTokenDispensingEntity = configurationReader.getAccessTokenDispensingEntity();
    Assert.assertEquals(EXPECTED_ACCESS_TOKEN_DISPENSING_ENTITY, accessTokenDispensingEntity);
  }

  @Test
  public void shouldDetermineKonnektorAuthSignatureServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorAuthSignatureServiceEndpoint =
        configurationReader.getKonnektorAuthSignatureServiceEndpoint();
    Assert.assertEquals(
        EXPECTED_KONNEKTOR_AUTH_SIGNATURE_SERVICE_ENDPOINT, konnektorAuthSignatureServiceEndpoint);
  }

  @Test
  public void shouldDetermineKonnektorCertificateServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorCertificateServiceEndpoint =
        configurationReader.getKonnektorCertificateServiceEndpoint();
    Assert.assertEquals(
        EXPECTED_KONNEKTOR_CERTIFICATE_SERVICE_ENDPOINT, konnektorCertificateServiceEndpoint);
  }

  @Test
  public void shouldDetermineKonnektorEventServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorEventServiceEndpoint = configurationReader.getKonnektorEventServiceEndpoint();
    Assert.assertEquals(EXPECTED_KONNEKTOR_EVENT_SERVICE_ENDPOINT, konnektorEventServiceEndpoint);
  }

  @Test
  public void shouldDetermineKonnektorSignatureServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorSignatureServiceEndpoint =
        configurationReader.getKonnektorSignatureServiceEndpoint();
    Assert.assertEquals(
        EXPECTED_KONNEKTOR_SIGNATURE_SERVICE_ENDPOINT, konnektorSignatureServiceEndpoint);
  }

  @Test
  public void shouldDetermineKonnektorCardServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String konnektorCardServiceEndpoint = configurationReader.getKonnektorCardServiceEndpoint();
    Assert.assertEquals(EXPECTED_KONNEKTOR_CARD_SERVICE_ENDPOINT, konnektorCardServiceEndpoint);
  }

  @Test
  public void shouldDeterminePathToClientKeystore() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String pathToClientKeystore = configurationReader.getPathToClientKeystore();
    Assert.assertEquals(EXPECTED_PATH_TO_CLIENT_KEYSTORE, pathToClientKeystore);
  }

  @Test
  public void shouldDetermineClientKeystorePassword() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String clientKeystorePassword = configurationReader.getClientKeystorePassword();
    Assert.assertEquals(EXPECTED_CLIENT_KEYSTORE_PASSWORD, clientKeystorePassword);
  }

  @Test
  public void shoudDeterminePathToClientTruststore() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String pathToClientTruststore = configurationReader.getPathToClientTruststore();
    Assert.assertEquals(EXPECTED_PATH_TO_CLIENT_TRUSTSTORE, pathToClientTruststore);
  }

  @Test
  public void shouldDetermineClientTruststorePassword() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String clientTruststorePassword = configurationReader.getClientTruststorePassword();
    Assert.assertEquals(EXPECTED_CLIENT_TRUSTSTORE_PASSWORD, clientTruststorePassword);
  }

  @Test
  public void shouldDetermineKonnektorCardTerminalServiceEndpoint() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    String endpoint = configurationReader.getKonnektorCardTerminalServiceEndpoint();
    Assert.assertEquals(EXPECTED_KONNEKTOR_CARD_TERMINAL_SERVICE_ENDPOINT, endpoint);
  }
}
