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
import de.gematik.ws.conn.cardservice.v8.VerifyPin;
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformVerifyPin extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_VERIFY_PIN =
      "http://ws.gematik.de/conn/CardService/v8.1#VerifyPin";

  /**
   * Ruft die Methode <i>VerifyPin</i> des Konnektors auf.
   *
   * @param invocationContext Kontext, der für den Aufruf verwendet werden soll.
   * @param cardHandle Handle des HBAs der freigeschaltet werden soll.
   * @return Response des Konnektors.
   */
  public PinResponseType performVerifyPin(InvocationContext invocationContext, String cardHandle)
      throws IOException {
    ContextType contextType = invocationContext.convertToContextType();
    VerifyPin verifyPin = new VerifyPin();
    verifyPin.setContext(contextType);
    verifyPin.setCardHandle(cardHandle);
    verifyPin.setPinTyp(PinType.PIN_CH.getType());
    JAXBElement<PinResponseType> jaxbElement =
        (JAXBElement<PinResponseType>)
            getWebServiceTemplate()
                .marshalSendAndReceive(
                    KonnektorHelper.determineCardServiceEndpoint(),
                    verifyPin,
                    new SoapActionCallback(SOAP_ACTION_VERIFY_PIN));
    return jaxbElement.getValue();
  }
}
