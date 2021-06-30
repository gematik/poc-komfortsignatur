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

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis einer PIN-Verifikation. */
public class VerifyPinResult {

  private static final Logger LOGGER = LoggerFactory.getLogger(VerifyPinResult.class);

  public static final String STATUS_OK = "OK";
  public static final String PIN_RESULT_OK = "OK";

  private static final boolean RESPONSE_VALID = true;

  private String status;
  private String pinResult;

  public VerifyPinResult() {}

  public VerifyPinResult(String status, String pinResult) {
    this.status = status;
    this.pinResult = pinResult;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPinResult() {
    return pinResult;
  }

  public void setPinResult(String pinResult) {
    this.pinResult = pinResult;
  }

  @Override
  public String toString() {
    return "VerifyPinResult{"
        + "status='"
        + status
        + '\''
        + ", pinResult='"
        + pinResult
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifyPinResult that = (VerifyPinResult) o;
    return Objects.equals(status, that.status) && Objects.equals(pinResult, that.pinResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, pinResult);
  }

  /**
   * Prüft, ob die PIN erfolgreich feigeschaltet werden konnte.
   *
   * @return true, wenn die PIN erfolgreich freigeschaltet werden konnte, andernfalls false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", status);
    if (status == null || !status.equals(STATUS_OK)) {
      return false;
    }

    LOGGER.info("PinResult: {}", pinResult);
    if (pinResult == null || !pinResult.equals(PIN_RESULT_OK)) {
      return false;
    }

    return RESPONSE_VALID;
  }
}
