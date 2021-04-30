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
import de.gematik.ws.conn.cardservice.v8.GetPinStatusResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PinStatus {

  @Autowired PerformGetPinStatus performGetPinStatus;

  /**
   * ermittelt Einzelheiten über den PinStatus der angegebenen CardSession mit dem Typ
   *
   * @param invocationContext AnmeldeKontext {@link InvocationContext}
   * @param pinType pinType <br>
   *     <b>HBAx:</b>
   *     <ul>
   *       <li>PIN.CH
   *       <li>PIN.QES
   *     </ul>
   *     <br>
   *     <b>SM-B</b>
   *     <ul>
   *       <li>PIN.SMC
   *     </ul>
   *
   * @param cardHandle das cardHandle
   * @return PinStatusResult {@link PinStatusResult}
   * @throws IOException wenn der Aufruf fehlschlägt
   */
  public PinStatusResult getPinStatusResponse(
      InvocationContext invocationContext, String pinType, String cardHandle) throws IOException {
    GetPinStatusResponse getPinStatusResponse =
        performGetPinStatus.getPinStatusResponse(invocationContext, pinType, cardHandle);

    return new PinStatusResult(
        getPinStatusResponse.getStatus(),
        getPinStatusResponse.getLeftTries(),
        getPinStatusResponse.getPinStatus());
  }
}
