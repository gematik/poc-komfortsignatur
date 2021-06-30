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
import de.gematik.ws.conn.cardterminalservice.v1.RequestCard;
import de.gematik.ws.conn.cardterminalservice.v1.RequestCardResponse;
import de.gematik.ws.conn.cardterminalservice.v1.Slot;
import java.io.IOException;
import java.math.BigInteger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformRequestCard extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_REQUEST_CARD =
      "http://ws.gematik.de/conn/CardTerminalService/v1.1#RequestCard";

  /**
   * Ruft die Methode RequestCard des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param ctId ID des Terminals, in das die Karte gesteckt werden soll.
   * @param slotNumber Nummer des Slots für den die Karte angefordert werden soll.
   * @return Response des Konnektors.
   */
  public RequestCardResponse performRequestCard(
      InvocationContext invocationContext, String ctId, int slotNumber) throws IOException {
    RequestCard requestCard = new RequestCard();
    requestCard.setContext(invocationContext.convertToContextType());
    Slot slot = new Slot();
    slot.setSlotId(BigInteger.valueOf(slotNumber));
    slot.setCtId(ctId);
    requestCard.setSlot(slot);
    return (RequestCardResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineCardTerminalServiceEndpoint(),
                requestCard,
                new SoapActionCallback(SOAP_ACTION_REQUEST_CARD));
  }
}
