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

import de.gematik.rezeps.Status;
import de.gematik.rezeps.util.CommonUtils;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Repräsentiert das Ergebnis des Aufrufs der Operation EjectCard. */
public class EjectCardResult extends Status {

  public static final String STATUS_OK = "OK";
  public static final String ERROR_TEXT_4203 = "Karte deaktiviert, aber nicht entnommen";

  private static final Logger LOGGER = LoggerFactory.getLogger(EjectCardResult.class);
  private static final boolean RESPONSE_VALID = true;

  private String soapFault;

  public EjectCardResult() {}

  public EjectCardResult(String status) {
    super();
    super.setStatus(status);
  }

  public String getSoapFault() {
    return soapFault;
  }

  public void setSoapFault(String soapFault) {
    this.soapFault = soapFault;
  }

  /**
   * Prüft, ob die Karte erfolgreich ausgeworfen werden konnte.
   *
   * @return true, wenn die Karte erfolgreich ausgeworfen werden konnte, andernfalls false.
   */
  public boolean isValidResponse() {

    LOGGER.info("Response status: {}", super.getStatus());
    if (super.getStatus() == null || !super.getStatus().equals(STATUS_OK)) {
      return false;
    }
    return RESPONSE_VALID;
  }

  /**
   * Prüft, ob die letzte Ausführung von EjectCard mit dem SOAP-Fault 4203 beantwortet wurde.
   *
   * @return true, wenn die letzte Ausführung von EjectCard mit dem SOAP-Fault 4203 beantwortet
   *     wurde, andernfalls false.
   */
  public boolean isSoapFault4203() {
    return !CommonUtils.isNullOrEmpty(soapFault) && soapFault.startsWith(ERROR_TEXT_4203);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EjectCardResult that = (EjectCardResult) o;
    return Objects.equals(statusName, that.statusName)
        && Objects.equals(statusCode, that.statusCode)
        && Objects.equals(soapFault, that.soapFault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statusName, statusCode, soapFault);
  }
}
