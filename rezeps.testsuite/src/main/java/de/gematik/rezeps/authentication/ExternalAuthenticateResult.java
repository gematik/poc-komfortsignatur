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

package de.gematik.rezeps.authentication;

import java.util.Arrays;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis eines Aufrufs von ExternalAuthenticate beim Konnektor. */
public class ExternalAuthenticateResult {

  public static final String STATUS_OK = "OK";

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAuthenticateResult.class);
  private static final boolean RESPONSE_VALID = true;

  private String status;
  private byte[] authenticatedData;

  public ExternalAuthenticateResult() {}

  public ExternalAuthenticateResult(String status, byte[] authenticatedData) {
    this.status = status;
    this.authenticatedData = authenticatedData;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public byte[] getAuthenticatedData() {
    return authenticatedData;
  }

  public void setAuthenticatedData(byte[] authenticatedData) {
    this.authenticatedData = authenticatedData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalAuthenticateResult that = (ExternalAuthenticateResult) o;
    return Objects.equals(status, that.status)
        && Arrays.equals(authenticatedData, that.authenticatedData);
  }

  @Override
  public int hashCode() {
    int result1 = Objects.hash(status);
    result1 = 31 * result1 + Arrays.hashCode(authenticatedData);
    return result1;
  }

  @Override
  public String toString() {
    return "ExternalAuthenticateResult{"
        + "result='"
        + status
        + '\''
        + ", authenticatedData="
        + Arrays.toString(authenticatedData)
        + '}';
  }

  /**
   * Prüft, ob die Binärdaten erfolgreich mittels nonQES signiert werden konnten.
   *
   * @return true, falls die Binärdaten erfolgreich mittels nonQES signiert werden konnten,
   *     andernfalls false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", status);
    if (status == null || !status.equals(STATUS_OK)) {
      return false;
    }

    LOGGER.info(
        "Length authenticated data: {}",
        authenticatedData == null ? "null" : authenticatedData.length);
    if (authenticatedData == null || authenticatedData.length == 0) {
      return false;
    }

    return RESPONSE_VALID;
  }
}
