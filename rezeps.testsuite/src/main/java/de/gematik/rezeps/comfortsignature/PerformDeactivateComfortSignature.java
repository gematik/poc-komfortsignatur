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

import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.signatureservice.v7.DeactivateComfortSignature;
import de.gematik.ws.conn.signatureservice.v7.DeactivateComfortSignatureResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformDeactivateComfortSignature extends WebServiceGatewaySupport {

  public static final String SOAP_ACTION_DEACTIVATE_COMFORT_SIGNATURE =
      "http://ws.gematik.de/conn/SignatureService/v7.5#DeactivateComfortSignature";

  /**
   * Ruft die Operation <i>DeactivateComfortSignature</i> des Konnektors auf.
   *
   * @return Die Response des Konnektors.
   * @throws IOException Falls der Endpunkt des Signaturdienstes nicht bestimmt werden kann.
   */
  public DeactivateComfortSignatureResponse performDeActivateComfortSignature(String cardHandle)
      throws IOException {
    DeactivateComfortSignature deactivateComfortSignature = new DeactivateComfortSignature();
    deactivateComfortSignature.getCardHandle().add(cardHandle);
    return (DeactivateComfortSignatureResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                deactivateComfortSignature,
                new SoapActionCallback(SOAP_ACTION_DEACTIVATE_COMFORT_SIGNATURE));
  }
}
