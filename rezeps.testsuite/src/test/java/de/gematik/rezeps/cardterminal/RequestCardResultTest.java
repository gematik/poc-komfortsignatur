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

package de.gematik.rezeps.cardterminal;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class RequestCardResultTest {

  private static final String CARD_HANDLE = "dummy_card_handle";

  @Test
  public void shouldValidateResponse() {
    RequestCardResult requestCardResult =
        new RequestCardResult(RequestCardResult.STATUS_OK, CARD_HANDLE);
    Assert.assertTrue(requestCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingStatus() {
    RequestCardResult requestCardResult = new RequestCardResult(null, CARD_HANDLE);
    Assert.assertFalse(requestCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnWrongStatus() {
    RequestCardResult requestCardResult = new RequestCardResult("Warning", CARD_HANDLE);
    Assert.assertFalse(requestCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingCardHandle() {
    RequestCardResult requestCardResult = new RequestCardResult(RequestCardResult.STATUS_OK, null);
    Assert.assertFalse(requestCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnEmptyCardHandle() {
    RequestCardResult requestCardResult = new RequestCardResult(RequestCardResult.STATUS_OK, "");
    Assert.assertFalse(requestCardResult.isValidResponse());
  }
}
