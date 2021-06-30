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
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PinVerifier {

  @Autowired PerformVerifyPin performVerifyPin;

  /**
   * Schaltet die PIN.CH eines HBAs frei.
   *
   * @param invocationContext Der Kontext, der für den Aufruf verwendet werden soll.
   * @param cardHandle Handle des HBAs, dessen PIN freigeschaltet werden soll.
   * @return Die Daten der Response, die für den weiteren Testablauf benötigt werden.
   */
  public VerifyPinResult performVerifyPin(InvocationContext invocationContext, String cardHandle)
      throws IOException {
    PinResponseType verifyPinResponse =
        performVerifyPin.performVerifyPin(invocationContext, cardHandle);

    VerifyPinResult verifyPinResult = new VerifyPinResult();

    if (verifyPinResponse != null) {
      Status status = verifyPinResponse.getStatus();
      if (status != null) {
        verifyPinResult.setStatus(status.getResult());
      }

      PinResultEnum pinResult = verifyPinResponse.getPinResult();
      if (pinResult != null) {
        verifyPinResult.setPinResult(pinResult.value());
      }
    }
    return verifyPinResult;
  }
}
