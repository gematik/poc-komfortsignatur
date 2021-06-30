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
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticateResponse;
import java.io.IOException;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExternalAuthenticator {

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
  public ExternalAuthenticateResult authenticateExternally(
      InvocationContext invocationContext, String cardHandle, byte[] dataToBeSigned)
      throws IOException {
    ExternalAuthenticateResponse externalAuthenticateResponse =
        performExternalAuthenticate.performExternalAuthenticate(
            invocationContext, cardHandle, dataToBeSigned);

    ExternalAuthenticateResult externalAuthenticateResult = null;
    if (externalAuthenticateResponse != null) {
      externalAuthenticateResult = new ExternalAuthenticateResult();
      Status status = externalAuthenticateResponse.getStatus();
      if (status != null) {
        externalAuthenticateResult.setStatus(status.getResult());
      }
      SignatureObject signatureObject = externalAuthenticateResponse.getSignatureObject();
      if (signatureObject != null) {
        Base64Signature base64Signature = signatureObject.getBase64Signature();
        if (base64Signature != null) {
          externalAuthenticateResult.setAuthenticatedData(base64Signature.getValue());
        }
      }
    }
    return externalAuthenticateResult;
  }
}
