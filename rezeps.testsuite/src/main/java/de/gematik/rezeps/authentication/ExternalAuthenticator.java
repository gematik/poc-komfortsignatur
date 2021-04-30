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

package de.gematik.rezeps.authentication;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExternalAuthenticator {

  private static final String STATUS_OK = "OK";

  @Autowired PerformExternalAuthenticate performExternalAuthenticate;

  /**
   * Führt ein ExternalAuthenticate mittels SMC-B beim Konnektor durch.
   *
   * @param invocationContext Der für den Aufruf zu verwendende Kontext. Die für die Signatur zu
   *     verwendende SMC-B muss in diesem Kontext sichtbar sein.
   * @param cardHandle Das Handle der zu verwendenden SMC-B.
   * @param dataToBeSigned Die zu signierenden Daten.
   * @return Die signierten Daten.
   */
  public byte[] authenticateExternally(
      InvocationContext invocationContext, String cardHandle, byte[] dataToBeSigned)
      throws IOException {
    byte[] signedData = null;
    ExternalAuthenticateResponse externalAuthenticateResponse =
        performExternalAuthenticate.performExternalAuthenticate(
            invocationContext, cardHandle, dataToBeSigned);

    Status status = externalAuthenticateResponse.getStatus();
    if (status != null && status.getResult().equals(STATUS_OK)) {
      signedData =
          externalAuthenticateResponse.getSignatureObject().getBase64Signature().getValue();
    }
    return signedData;
  }
}
