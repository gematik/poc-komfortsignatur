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

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardterminalservice.v1.EjectCardResponse;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.client.SoapFaultClientException;

@Component
public class CardEjector {

  private static final Logger LOGGER = LoggerFactory.getLogger(CardEjector.class);

  @Autowired PerformEjectCard performEjectCard;

  /**
   * Ruft die Operation EjectCard des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param cardHandle Das Handle der Karte, die ausgeworfen werden soll.
   * @return Das Ergebnis der Operation.
   */
  public EjectCardResult performEjectCard(InvocationContext invocationContext, String cardHandle)
      throws IOException {

    EjectCardResult ejectCardResult = new EjectCardResult();
    try {
      EjectCardResponse ejectCardResponse =
          performEjectCard.performEjectCard(invocationContext, cardHandle);

      if (ejectCardResponse != null) {
        Status status = ejectCardResponse.getStatus();
        if (status != null) {
          ejectCardResult.setStatus(status.getResult());
        }
      }
    } catch (SoapFaultClientException exception) {
      SoapFault soapFault = exception.getSoapFault();
      ejectCardResult.setSoapFault(soapFault.getFaultStringOrReason());
    } catch (Exception exception) {
      LOGGER.error(exception.getMessage());
    }
    return ejectCardResult;
  }
}
