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
import de.gematik.ws.conn.signatureservice.v7.GetSignatureModeResponse;
import de.gematik.ws.conn.signatureservice.v7.SessionInfo;
import de.gematik.ws.conn.signatureservice.v7.SignatureModeEnum;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignatureModeGetter {

  @Autowired PerformGetSignatureMode performGetSignatureMode;

  /**
   * Bestimmt den Signatur-Modus eines HBA.
   *
   * @param cardHandle Das Handle des HBAs, dessen Signatur-Modus bestimmt werden soll.
   * @param invocationContext Der Aufrufkontext, der zum Aktivieren der Komfortsignatur genutzt
   *     wurde.
   */
  public ComfortSignatureResult determineSignatureMode(
      String cardHandle, InvocationContext invocationContext) throws IOException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();
    GetSignatureModeResponse getSignatureResponse =
        performGetSignatureMode.performGetSignatureMode(cardHandle, invocationContext);
    // retrieve the session information for cardHandle
    SessionInfo sessionInfo = getSignatureResponse.getSessionInfo();
    Status status = getSignatureResponse.getStatus();
    if (status != null) {
      comfortSignatureResult.setStatus(status.getResult());
    }
    String signatureMode =
        sessionInfo == null ? SignatureModeEnum.PIN.value() : SignatureModeEnum.COMFORT.value();
    comfortSignatureResult.setSignatureMode(signatureMode);
    return comfortSignatureResult;
  }
}
