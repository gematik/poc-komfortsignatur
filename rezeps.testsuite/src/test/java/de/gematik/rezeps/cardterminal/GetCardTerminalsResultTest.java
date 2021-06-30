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

import org.junit.Assert;
import org.junit.Test;

public class GetCardTerminalsResultTest {

  public static final String CT_ID = "dummy_ct_id_1";
  private static final String RESULT_OK = "OK";
  private static final String RESULT_NOK = "NOK";

  @Test
  public void shouldValidateResponse() {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(RESULT_OK, CT_ID);
    Assert.assertTrue(getCardTerminalsResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingResult() {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(null, CT_ID);
    Assert.assertFalse(getCardTerminalsResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnWrongResult() {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(RESULT_NOK, CT_ID);
    Assert.assertFalse(getCardTerminalsResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingCtId() {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(RESULT_OK, null);
    Assert.assertFalse(getCardTerminalsResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnEmptyCtId() {
    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult(RESULT_OK, "");
    Assert.assertFalse(getCardTerminalsResult.isValidResponse());
  }
}
