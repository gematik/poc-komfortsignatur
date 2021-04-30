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
import de.gematik.ws.conn.cardservice.v8.GetPinStatus;
import de.gematik.ws.conn.cardservice.v8.GetPinStatusResponse;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformGetPinStatus extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_GET_PIN_STATUS =
      "http://ws.gematik.de/conn/CardService/v8.1#GetPinStatus";

  /**
   * Ruft die Methode <i>GetPinStatus</i> des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param pinType pin type (e.g. PIN.QES)
   * @param cardHandle CardHandle
   * @return Response des Konnektors.
   */
  public GetPinStatusResponse getPinStatusResponse(
      InvocationContext invocationContext, String pinType, String cardHandle) throws IOException {

    ContextType contextType = invocationContext.convertToContextType();
    GetPinStatus getPinStatus = new GetPinStatus();
    getPinStatus.setPinTyp(pinType);
    getPinStatus.setCardHandle(cardHandle);
    getPinStatus.setContext(contextType);

    return (GetPinStatusResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineCardServiceEndpoint(),
                getPinStatus,
                new SoapActionCallback(SOAP_ACTION_GET_PIN_STATUS));
  }
}
