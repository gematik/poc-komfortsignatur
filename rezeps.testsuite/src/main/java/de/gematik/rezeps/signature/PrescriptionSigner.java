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
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignDocumentResponse;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.v7.SignResponse.OptionalOutputs;
import java.io.IOException;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Signiert einen Verordnungsdatensatz */
@Component
public class PrescriptionSigner {

  @Autowired PerformSignDocument performSignDocument;

  /**
   * Signiert einen Verordnungsdatensatz mittels Konnektor.
   *
   * @param invocationContext Der Kontext für den Aufruf beim Konnektor.
   * @param cardHandle Das Handle des signierenden HBAs.
   * @param prescription Der Verordnungsdatensatz.
   * @param jobNubmer Für die Signatur zu verwendende Job-Nummer.
   * @return Der signierte Verordnungsdatensatz.
   */
  public SignDocumentResult performSignPrescription(
      InvocationContext invocationContext, String cardHandle, String prescription, String jobNubmer)
      throws IOException {
    SignDocumentResult signDocumentResult = null;

    SignDocumentResponse signDocumentResponse =
        performSignDocument.performSignDocument(
            invocationContext, cardHandle, prescription.getBytes(), jobNubmer);

    List<SignResponse> signResponses = signDocumentResponse.getSignResponse();
    if (signResponses != null) {
      for (SignResponse signResponse : signResponses) {
        String result = signResponse.getStatus().getResult();
        String mimeTypeBase64Data = determineMimeType(signResponse);
        SignatureObject signatureObject = signResponse.getSignatureObject();
        String typeBase64Signature = signatureObject.getBase64Signature().getType();
        byte[] signedDocument = signatureObject.getBase64Signature().getValue();
        signDocumentResult =
            new SignDocumentResult(result, mimeTypeBase64Data, typeBase64Signature, signedDocument);
      }
    }
    return signDocumentResult;
  }

  private String determineMimeType(SignResponse signResponse) {
    String mimeType = null;
    OptionalOutputs optionalOutputs = signResponse.getOptionalOutputs();
    if (optionalOutputs != null) {
      DocumentType documentWithSignature = optionalOutputs.getDocumentWithSignature();
      if (documentWithSignature != null) {
        Base64Data base64Data = documentWithSignature.getBase64Data();
        if (base64Data != null) {
          mimeType = base64Data.getMimeType();
        }
      }
    }
    return mimeType;
  }
}
