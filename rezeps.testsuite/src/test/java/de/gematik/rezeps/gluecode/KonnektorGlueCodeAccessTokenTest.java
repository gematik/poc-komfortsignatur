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
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KonnektorGlueCodeAccessTokenTest {

  private static final String EXPECTED_ACCESS_TOKEN_PRESCRIBING_ENTITY = "0-8-15-4711";
  private static final String EXPECTED_ACCESS_TOKEN_DISPENSING_ENTITY = "41-42-43";

  private String accessTokenPrescribingEntity;
  private String accessTokenDispensingEntity;

  @Before
  public void setup() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    accessTokenPrescribingEntity = configurationReader.getAccessTokenPrescribingEntity();
    accessTokenDispensingEntity = configurationReader.getAccessTokenDispensingEntity();
  }

  @After
  public void tearDown() throws IOException {
    ConfigurationReader configurationReader = ConfigurationReader.getInstance();
    configurationReader.setAccessTokenPrescribingEntity(accessTokenPrescribingEntity);
    configurationReader.setAccessTokenDispensingEntity(accessTokenDispensingEntity);
  }

  @Test
  public void shouldSupplyAccessTokenPrescribingEntity() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.obtainAccessTokenPrescribingEntity();
    String accessToken = TestcaseData.getInstance().getAccessTokenPrescribingEntity();
    Assert.assertEquals(EXPECTED_ACCESS_TOKEN_PRESCRIBING_ENTITY, accessToken);
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingAccessTokenPrescribingEntity()
      throws MissingPreconditionException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    ConfigurationReader.getInstance().setAccessTokenPrescribingEntity("");
    konnektorGlueCode.obtainAccessTokenPrescribingEntity();
  }

  @Test
  public void shouldSupplyAccessTokenDispensingEntity() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.obtainAccessTokenDispensingEntity();
    String accessToken = TestcaseData.getInstance().getAccessTokenDispensingEntity();
    Assert.assertEquals(EXPECTED_ACCESS_TOKEN_DISPENSING_ENTITY, accessToken);
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingAccessTokenDispensingEntity()
      throws MissingPreconditionException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    ConfigurationReader.getInstance().setAccessTokenDispensingEntity("");
    konnektorGlueCode.obtainAccessTokenDispensingEntity();
  }
}
