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
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.cardterminalservice.v1.EjectCard;
import de.gematik.ws.conn.cardterminalservice.v1.EjectCardResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformEjectCard extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_EJECT_CARD =
      "http://ws.gematik.de/conn/CardTerminalService/v1.1#EjectCard";

  /**
   * Ruft die Methode EjectCard des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param cardHandle Das Handle der Karte, für die der Auswurf angeforder werden soll.
   * @return Response des Konnektors.
   */
  public EjectCardResponse performEjectCard(InvocationContext invocationContext, String cardHandle)
      throws IOException {
    EjectCard ejectCard = new EjectCard();
    ejectCard.setContext(invocationContext.convertToContextType());
    ejectCard.setCardHandle(cardHandle);
    return (EjectCardResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineCardTerminalServiceEndpoint(),
                ejectCard,
                new SoapActionCallback(SOAP_ACTION_EJECT_CARD));
  }
}
