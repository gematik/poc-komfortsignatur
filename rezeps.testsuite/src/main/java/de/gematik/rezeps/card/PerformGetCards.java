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

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.v7.GetCards;
import de.gematik.ws.conn.eventservice.v7.GetCardsResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformGetCards extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_GET_CARDS =
      "http://ws.gematik.de/conn/EventService/v7.2#GetCards";

  /**
   * Ruft die Methode GetCards des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @return Response des Konnektors.
   */
  public GetCardsResponse performGetCards(InvocationContext invocationContext) throws IOException {
    GetCards getCards = new GetCards();
    getCards.setMandantWide(true);
    ContextType contextType = invocationContext.convertToContextType();
    getCards.setContext(contextType);
    return (GetCardsResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineEventServiceEndpoint(),
                getCards,
                new SoapActionCallback(SOAP_ACTION_GET_CARDS));
  }
}
