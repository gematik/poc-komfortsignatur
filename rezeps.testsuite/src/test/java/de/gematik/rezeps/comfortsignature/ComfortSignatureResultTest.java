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

package de.gematik.rezeps.comfortsignature;

import static de.gematik.rezeps.comfortsignature.ComfortSignatureResult.ERROR_TEXT_4018;
import static de.gematik.rezeps.comfortsignature.ComfortSignatureResult.SIGNATURE_MODE_COMFORT;
import static de.gematik.rezeps.comfortsignature.ComfortSignatureResult.STATUS_OK;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class ComfortSignatureResultTest {

  @Test
  public void shouldValidateIsComfortSignatureActivated() {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, SIGNATURE_MODE_COMFORT);
    assertTrue(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldFailIsComfortSignatureActivatedOnMissingStatus() {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(null, SIGNATURE_MODE_COMFORT);
    assertFalse(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldFailIsComfortSignatureActivatedOnWrongStatus() {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult("NOK", SIGNATURE_MODE_COMFORT);
    assertFalse(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldFailIsComfortSignatureActivatedOnMissingSignatureMode() {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult(STATUS_OK, null);
    assertFalse(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldFailIsComfortSignatureActivatedOnWrongSignatureMode() {
    ComfortSignatureResult comfortSignatureResult =
        new ComfortSignatureResult(STATUS_OK, "not COMFORT");
    assertFalse(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldRecognizeSoapFault4018() {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSoapFault(ERROR_TEXT_4018);
    Assert.assertTrue(comfortSignatureResult.isSoapFault4018());
  }

  @Test
  public void shouldNotRecognizeSoapFault4018OnMissingSoapFault() {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSoapFault(null);
    Assert.assertFalse(comfortSignatureResult.isSoapFault4018());
  }

  @Test
  public void shouldNotRecognizeSoapFault4018OnDifferentSoapFault() {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    comfortSignatureResult.setSoapFault("Ungültige Mandanten-ID");
    Assert.assertFalse(comfortSignatureResult.isSoapFault4018());
  }
}
