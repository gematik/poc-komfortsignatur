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

package de.gematik.rezeps.signature;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.signatureservice.v7.VerifyDocumentResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Verifiziert die Signatur eines E-Rezepts. */
@Component
public class SignatureVerification {

  protected static final String INVOCATION_RESULT_OK = "OK";
  protected static final String VALIDATION_RESULT_VALID = "VALID";

  @Autowired PerformVerifyDocument performVerifyDocument;

  /**
   * Verifiziert die Signatur eines E-Rezepts.
   *
   * @param invocationContext Der Kontext für den Aufruf zum Konnektor.
   * @param signedPrescription Das signierte Rezept.
   * @return Status und High Level Result der Signaturprüfung des Konnektors.
   */
  public SignatureVerificationResult verifySignature(
      InvocationContext invocationContext, byte[] signedPrescription) throws IOException {
    VerifyDocumentResponse verifyDocumentResponse =
        performVerifyDocument.performVerifyDocument(invocationContext, signedPrescription);
    String status = verifyDocumentResponse.getStatus().getResult();
    String highLevelResult = verifyDocumentResponse.getVerificationResult().getHighLevelResult();
    return new SignatureVerificationResult(status, highLevelResult);
  }
}
