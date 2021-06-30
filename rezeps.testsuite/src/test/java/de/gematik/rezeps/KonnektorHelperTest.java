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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KonnektorHelperTest {

  private static final String EXPECTED_ENDPOINT_AUTH_SIGNATURE_SERVICE =
      "https://127.0.0.1:443/authsignatureservice";
  private static final String EXPECTED_ENDPOINT_CERTIFICATE_SERVICE =
      "https://127.0.0.1:443/certificateservice";
  private static final String EXPECTED_ENDPOINT_EVENT_SERVICE =
      "https://127.0.0.1:443/eventservice";
  private static final String EXPECTED_ENDPOINT_SIGNATURE_SERVICE =
      "https://127.0.0.1:443/signatureservice";
  private static final String EXPECTED_ENDPOINT_CARD_SERVICE = "https://127.0.0.1:443/cardservice";

  private static final String EXPECTED_ENDPOINT_CARD_TERMINAL_SERVICE =
      "https://127.0.0.1:443/cardterminalservice";
  private static final Logger LOGGER = LoggerFactory.getLogger(KonnektorHelperTest.class);

  @BeforeClass
  public static void beforeClass() {
    LOGGER.info(
        "Set environment to use configuration.unittest.properties file (preventing build failure on other configurations)");
    System.setProperty("CFG_PROPS", "unittest");
  }

  @AfterClass
  public static void afterClass() {
    LOGGER.info("cleanup environment");
    System.clearProperty("CFG_PROPS");
  }

  @Test
  public void shouldDetermineAuthSignatureServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineAuthSignatureServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_AUTH_SIGNATURE_SERVICE, konnektorEndpoint);
  }

  @Test
  public void shouldDetermineCertificateServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineCertificateServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_CERTIFICATE_SERVICE, konnektorEndpoint);
  }

  @Test
  public void shouldDetermineEventServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineEventServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_EVENT_SERVICE, konnektorEndpoint);
  }

  @Test
  public void shouldDetermineSignatureServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineSignatureServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_SIGNATURE_SERVICE, konnektorEndpoint);
  }

  @Test
  public void shouldDetermineCardServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineCardServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_CARD_SERVICE, konnektorEndpoint);
  }

  @Test
  public void shouldDetermineCardTerminalServiceEndpoint() throws IOException {
    String konnektorEndpoint = KonnektorHelper.determineCardTerminalServiceEndpoint();
    Assert.assertEquals(EXPECTED_ENDPOINT_CARD_TERMINAL_SERVICE, konnektorEndpoint);
  }
}
