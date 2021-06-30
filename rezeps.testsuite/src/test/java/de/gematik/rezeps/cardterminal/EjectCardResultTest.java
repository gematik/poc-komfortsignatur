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

import de.gematik.rezeps.comfortsignature.ComfortSignatureResult;
import org.junit.Assert;
import org.junit.Test;

public class EjectCardResultTest {

  private static final String RESULT_OK = "OK";
  private static final String RESULT_NOK = "NOK";

  @Test
  public void shouldValidateResponse() {
    EjectCardResult ejectCardResult = new EjectCardResult(RESULT_OK);
    Assert.assertTrue(ejectCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnMissingResult() {
    EjectCardResult ejectCardResult = new EjectCardResult(null);
    Assert.assertFalse(ejectCardResult.isValidResponse());
  }

  @Test
  public void shouldNotValidateResponseOnInvalidResult() {
    EjectCardResult ejectCardResult = new EjectCardResult(RESULT_NOK);
    Assert.assertFalse(ejectCardResult.isValidResponse());
  }

  @Test
  public void shouldBeSoapFault4203() {
    EjectCardResult ejectCardResult = new EjectCardResult();
    ejectCardResult.setSoapFault(EjectCardResult.ERROR_TEXT_4203);
    Assert.assertTrue(ejectCardResult.isSoapFault4203());
  }

  @Test
  public void shouldNotBeSoapFault4203() {
    EjectCardResult ejectCardResult = new EjectCardResult();
    ejectCardResult.setSoapFault(ComfortSignatureResult.ERROR_TEXT_4018);
    Assert.assertFalse(ejectCardResult.isSoapFault4203());
  }

  @Test
  public void shouldNotBeSoapFault4203OnEmptySoapFault() {
    EjectCardResult ejectCardResult = new EjectCardResult();
    ejectCardResult.setSoapFault(null);
    Assert.assertFalse(ejectCardResult.isSoapFault4203());
  }
}
