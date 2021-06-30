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
import de.gematik.ws.conn.eventservice.v7.GetCardTerminals;
import de.gematik.ws.conn.eventservice.v7.GetCardTerminalsResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformGetCardTerminals extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_GET_CARD_TERMINALS =
      "http://ws.gematik.de/conn/EventService/v7.2#GetCardTerminals";

  /**
   * Ruft die Methode GetCardTerminals des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @return Response des Konnektors.
   */
  public GetCardTerminalsResponse performGetCardTerminals(InvocationContext invocationContext)
      throws IOException {
    GetCardTerminals getCardTerminals = new GetCardTerminals();
    getCardTerminals.setMandantWide(false);
    getCardTerminals.setContext(invocationContext.convertToContextType());
    return (GetCardTerminalsResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineEventServiceEndpoint(),
                getCardTerminals,
                new SoapActionCallback(SOAP_ACTION_GET_CARD_TERMINALS));
  }
}
