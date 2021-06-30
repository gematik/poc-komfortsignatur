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

package de.gematik.rezeps.card;

import org.junit.Assert;
import org.junit.Test;

public class VerifyPinResultTest {

  private static final String STATUS_OK = "OK";
  private static final String STATUS_NOK = "NOK";
  private static final String PIN_RESULT_OK = "OK";
  private static final String PIN_RESULT_REJECTED = "REJECTED";

  @Test
  public void shouldValidateResponse() {
    VerifyPinResult verifyPinResult = new VerifyPinResult(STATUS_OK, PIN_RESULT_OK);
    Assert.assertTrue(verifyPinResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnEmptyStatus() {
    VerifyPinResult verifyPinResult = new VerifyPinResult(null, PIN_RESULT_OK);
    Assert.assertFalse(verifyPinResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnInvalidStatus() {
    VerifyPinResult verifyPinResult = new VerifyPinResult(STATUS_NOK, PIN_RESULT_OK);
    Assert.assertFalse(verifyPinResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnEmptyPinResult() {
    VerifyPinResult verifyPinResult = new VerifyPinResult(STATUS_OK, null);
    Assert.assertFalse(verifyPinResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnInvalidPinResult() {
    VerifyPinResult verifyPinResult = new VerifyPinResult(STATUS_OK, PIN_RESULT_REJECTED);
    Assert.assertFalse(verifyPinResult.isValidResponse());
  }
}
