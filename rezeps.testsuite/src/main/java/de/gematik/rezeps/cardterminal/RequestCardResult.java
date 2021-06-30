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

import de.gematik.rezeps.util.CommonUtils;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis eines Aufrufs von RequestCard. */
public class RequestCardResult {

  public static final String STATUS_OK = "OK";

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestCardResult.class);
  private static final boolean RESPONSE_VALID = true;

  private String status;
  private String cardHandle;

  public RequestCardResult() {}

  public RequestCardResult(String status, String cardHandle) {
    this.status = status;
    this.cardHandle = cardHandle;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCardHandle() {
    return cardHandle;
  }

  public void setCardHandle(String cardHandle) {
    this.cardHandle = cardHandle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RequestCardResult that = (RequestCardResult) o;
    return Objects.equals(status, that.status) && Objects.equals(cardHandle, that.cardHandle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, cardHandle);
  }

  @Override
  public String toString() {
    return "RequestCardResult{"
        + "status='"
        + status
        + '\''
        + ", cardHandle='"
        + cardHandle
        + '\''
        + '}';
  }

  /**
   * Prüft, ob die Karte erfolgreich angefordert werden konnte.
   *
   * @return true, falls die Karte erfolgreich angefordert werden konnte, andernfalls false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", status);
    if (status == null || !status.equals(STATUS_OK)) {
      return false;
    }

    LOGGER.info("CardHandle: {}", cardHandle);
    if (CommonUtils.isNullOrEmpty(cardHandle)) {
      return false;
    }

    return RESPONSE_VALID;
  }
}
