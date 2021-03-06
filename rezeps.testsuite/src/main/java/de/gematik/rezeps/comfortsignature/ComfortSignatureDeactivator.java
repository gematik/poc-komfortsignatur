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

import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.DeactivateComfortSignatureResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComfortSignatureDeactivator {

  @Autowired PerformDeactivateComfortSignature performDeactivateComfortSignature;

  /** Deaktiviert die Komfortsignatur für einen HBA. */
  public ComfortSignatureResult deactivateComfortSignature(String cardHandle) throws IOException {
    ComfortSignatureResult comfortSignatureResult = new ComfortSignatureResult();

    DeactivateComfortSignatureResponse deactivateComfortSignatureResponse =
        performDeactivateComfortSignature.performDeActivateComfortSignature(cardHandle);
    Status status = deactivateComfortSignatureResponse.getStatus();
    if (status != null) {
      comfortSignatureResult.setStatus(status.getResult());
    }
    return comfortSignatureResult;
  }
}
