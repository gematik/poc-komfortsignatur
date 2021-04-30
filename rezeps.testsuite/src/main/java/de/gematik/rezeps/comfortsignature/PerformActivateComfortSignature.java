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

package de.gematik.rezeps.comfortsignature;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.signatureservice.v7.ActivateComfortSignature;
import de.gematik.ws.conn.signatureservice.v7.ActivateComfortSignatureResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformActivateComfortSignature extends WebServiceGatewaySupport {

  public static final String SOAP_ACTION_ACTIVATE_COMFORT_SIGNATURE =
      "http://ws.gematik.de/conn/SignatureService/v7.5#ActivateComfortSignature";

  /**
   * Ruft die Operation ActivateComfortSignature des Konnektors auf.
   *
   * @param invocationContext Der Aufrufkontext.
   * @param cardHandle Das HBA-Handle.
   * @return Die Response des Konnektors.
   * @throws IOException Falls der Endpunkt des Signaturdienstes nicht bestimmt werden kann.
   */
  public ActivateComfortSignatureResponse performActivateComfortSignature(
      InvocationContext invocationContext, String cardHandle) throws IOException {
    ActivateComfortSignature activateComfortSignature = new ActivateComfortSignature();
    activateComfortSignature.setContext(invocationContext.convertToContextType());
    activateComfortSignature.setCardHandle(cardHandle);
    return (ActivateComfortSignatureResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                activateComfortSignature,
                new SoapActionCallback(SOAP_ACTION_ACTIVATE_COMFORT_SIGNATURE));
  }
}
