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

import java.io.Serializable;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Prüfung ob ein Verordnungsdatensatz erfolgreich signiert werden konnte. */
public class SignatureVerificationResult implements Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SignatureVerificationResult.class);

  private static final String STATUS_OK = "OK";
  private static final String HIGH_LEVEL_RESULT_VALID = "VALID";
  private static final long serialVersionUID = 6180160820113790970L;

  private final String status;
  private final String highLevelResult;

  public SignatureVerificationResult(String status, String highLevelResult) {
    this.status = status;
    this.highLevelResult = highLevelResult;
  }

  /**
   * Bestimmt, ob die Signaturprüfung erfolgreich war. Eine Prüfung gilt als erfolgreich bei Status
   * "OK" und High Level Result "VALID".
   *
   * @return true, wenn die Signaturprüfung erfolgreich war, andernfalls false.
   */
  public boolean isValidSignature() {
    boolean isValidSignature = true;

    boolean statusIsOk = status != null && status.equals(STATUS_OK);
    LOGGER.info("Der Status der Response lautet {}", status);
    if (!statusIsOk) {
      isValidSignature = false;
    }

    boolean highLevelResultIsValid =
        highLevelResult != null && highLevelResult.equals(HIGH_LEVEL_RESULT_VALID);
    LOGGER.info("Das High Level Result lautet {}", highLevelResult);
    if (!highLevelResultIsValid) {
      isValidSignature = false;
    }

    return isValidSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignatureVerificationResult that = (SignatureVerificationResult) o;
    return Objects.equals(status, that.status)
        && Objects.equals(highLevelResult, that.highLevelResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, highLevelResult);
  }
}
