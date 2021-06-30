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

package de.gematik.rezeps.authentication;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class ExternalAuthenticateResultTest {

  private static final byte[] authenticatedData =
      "authenticated data".getBytes(StandardCharsets.UTF_8);

  @Test
  public void shouldValidateResponse() {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(ExternalAuthenticateResult.STATUS_OK, authenticatedData);
    Assert.assertTrue(externalAuthenticateResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingStatus() {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(null, authenticatedData);
    Assert.assertFalse(externalAuthenticateResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnWrongStatus() {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult("NOK", authenticatedData);
    Assert.assertFalse(externalAuthenticateResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingAuthenticatedData() {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(ExternalAuthenticateResult.STATUS_OK, null);
    Assert.assertFalse(externalAuthenticateResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnEmptyAuthenticatedData() {
    ExternalAuthenticateResult externalAuthenticateResult =
        new ExternalAuthenticateResult(ExternalAuthenticateResult.STATUS_OK, new byte[] {});
    Assert.assertFalse(externalAuthenticateResult.isValidResponse());
  }
}
