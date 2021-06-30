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

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis einer Anfrage von GetCardTerminals an den Konnektor. */
public class GetCardTerminalsResult {

  public static final String STATUS_OK = "OK";

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCardTerminalsResult.class);
  private static final boolean RESPONSE_VALID = true;

  private String status;
  private String ctId;

  public GetCardTerminalsResult() {}

  public GetCardTerminalsResult(String status, String ctId) {
    this.status = status;
    this.ctId = ctId;
  }

  public static String getStatusOk() {
    return STATUS_OK;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCtId() {
    return ctId;
  }

  public void setCtId(String ctId) {
    this.ctId = ctId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetCardTerminalsResult that = (GetCardTerminalsResult) o;
    return Objects.equals(status, that.status) && Objects.equals(ctId, that.ctId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, ctId);
  }

  @Override
  public String toString() {
    return "GetCardTerminalsResult{" + "status='" + status + '\'' + ", ctId='" + ctId + '\'' + '}';
  }

  /**
   * Prüft, ob die Liste der Kartenterminals erfolgreich geladen werden konnte.
   *
   * @return true, wenn die Liste der Kartenterminals erfolgreich geladen werden konnte, andernfalls
   *     false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", status);
    if (status == null || !status.equals(STATUS_OK)) {
      return false;
    }

    LOGGER.info("CtId: {}", ctId);
    if (StringUtils.isEmpty(ctId)) {
      return false;
    }

    return RESPONSE_VALID;
  }
}
