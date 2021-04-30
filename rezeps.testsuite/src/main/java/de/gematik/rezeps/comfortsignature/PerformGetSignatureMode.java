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
import de.gematik.ws.conn.signatureservice.v7.GetSignatureMode;
import de.gematik.ws.conn.signatureservice.v7.GetSignatureModeResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformGetSignatureMode extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_GET_SIGNATURE_MODE =
      "http://ws.gematik.de/conn/SignatureService/v7.5#GetSignatureMode";

  /**
   * Ruft die Operation GetSignatureMode des Konnektors auf.
   *
   * @param cardHandle Das Handle des HBAs, für den der Signatur-Modus bestimmt werden soll.
   * @param invocationContext Der Aufrufkontext der den HBA beinhaltet, für den der Signatur-Modus
   *     bestimmt werden soll.
   * @return Die Response des Konnektors.
   * @throws IOException Falls der Endpunkt des Signaturdienstes nicht bestimmt werden kann.
   */
  public GetSignatureModeResponse performGetSignatureMode(
      String cardHandle, InvocationContext invocationContext) throws IOException {
    GetSignatureMode getSignatureMode = new GetSignatureMode();
    getSignatureMode.setCardHandle(cardHandle);
    getSignatureMode.setContext(invocationContext.convertToContextType());

    return (GetSignatureModeResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                getSignatureMode,
                new SoapActionCallback(SOAP_ACTION_GET_SIGNATURE_MODE));
  }
}
