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
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.ActivateComfortSignatureResponse;
import de.gematik.ws.conn.signatureservice.v7.SignatureModeEnum;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.client.SoapFaultClientException;

@Component
public class ComfortSignatureActivator {

  @Autowired PerformActivateComfortSignature performActivateComfortSignature;

  /**
   * Aktiviert die Komfortsignatur für einen HBA.
   *
   * @param invocationContext Der Kontext für den Aufruf beim Konnektor.
   * @param cardHandle Das HBA-Handle
   */
  public ComfortSignatureResult activateComfortSignature(
      InvocationContext invocationContext, String cardHandle) throws IOException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    try {
      ActivateComfortSignatureResponse activateComfortSignatureResponse =
          performActivateComfortSignature.performActivateComfortSignature(
              invocationContext, cardHandle);
      Status status = activateComfortSignatureResponse.getStatus();
      if (status != null) {
        comfortSignatureResult.setStatus(status.getResult());
      }
      SignatureModeEnum signatureMode = activateComfortSignatureResponse.getSignatureMode();
      if (signatureMode != null) {
        comfortSignatureResult.setSignatureMode(signatureMode.value());
      }
    } catch (SoapFaultClientException exception) {
      SoapFault soapFault = exception.getSoapFault();
      comfortSignatureResult.setSoapFault(soapFault.getFaultStringOrReason());
    }
    return comfortSignatureResult;
  }
}
