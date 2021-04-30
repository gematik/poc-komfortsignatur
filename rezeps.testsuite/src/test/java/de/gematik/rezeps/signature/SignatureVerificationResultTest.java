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

package de.gematik.rezeps.signature;

import org.junit.Assert;
import org.junit.Test;

public class SignatureVerificationResultTest {

  private static final String STATUS_OK = "OK";
  private static final String STATUS_WARNING = "Warning";
  private static final String HIGH_LEVEL_RESULT_VALID = "VALID";
  private static final String HIGH_LEVEL_RESULT_INVALID = "INVALID";

  @Test
  public void shouldValidateSignature() {
    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(STATUS_OK, HIGH_LEVEL_RESULT_VALID);
    Assert.assertTrue(signatureVerificationResult.isValidSignature());
  }

  @Test
  public void shouldNotValidateSignatureOnStatusWarning() {
    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(STATUS_WARNING, HIGH_LEVEL_RESULT_VALID);
    Assert.assertFalse(signatureVerificationResult.isValidSignature());
  }

  @Test
  public void shouldNotValidateSignatureOnHighLevelResultInvalid() {
    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(STATUS_OK, HIGH_LEVEL_RESULT_INVALID);
    Assert.assertFalse(signatureVerificationResult.isValidSignature());
  }

  @Test
  public void shouldNotValidateSignatureOnMissingStatus() {
    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(null, HIGH_LEVEL_RESULT_VALID);
    Assert.assertFalse(signatureVerificationResult.isValidSignature());
  }

  @Test
  public void shouldNotValidateSignatureOnMissingHighLevelResult() {
    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(STATUS_OK, null);
    Assert.assertFalse(signatureVerificationResult.isValidSignature());
  }
}
