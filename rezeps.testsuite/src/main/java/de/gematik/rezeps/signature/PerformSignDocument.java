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
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignDocument;
import de.gematik.ws.conn.signatureservice.v7.SignDocumentResponse;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignRequest.OptionalInputs;
import java.io.IOException;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformSignDocument extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_SIGN_DOCUMENT =
      "http://ws.gematik.de/conn/SignatureService/v7.5#SignDocument";

  private static final boolean INCLUDE_E_CONTENT = true;
  private static final String DOCUMENT_ID = "CMS-Doc1";
  private static final String DOCUMENT_SHORT_TEXT = "a CMSDocument2sign";
  private static final String REQUEST_ID = "Doc1";
  private static final String DOCUMENT_MIME_TYPE = "text/plain; charset=utf-8";

  public SignDocumentResponse performSignDocument(
      InvocationContext invocationContext, String cardHandle, byte[] prescription, String jobNumber)
      throws IOException {
    SignDocument signDocument = new SignDocument();
    signDocument.setCardHandle(cardHandle);

    ContextType contextType = invocationContext.convertToContextType();
    signDocument.setContext(contextType);

    signDocument.setTvMode(SignatureServiceHelper.TV_MODE_NONE);
    signDocument.setJobNumber(jobNumber);

    List<SignRequest> signRequests = signDocument.getSignRequest();
    SignRequest signRequest = new SignRequest();
    signRequest.setRequestID(REQUEST_ID);

    OptionalInputs optionalInputs = new OptionalInputs();
    optionalInputs.setSignatureType(SignatureServiceHelper.SIGNATURE_TYPE);
    optionalInputs.setIncludeEContent(INCLUDE_E_CONTENT);
    signRequest.setOptionalInputs(optionalInputs);

    DocumentType documentType = new DocumentType();
    documentType.setID(DOCUMENT_ID);
    documentType.setShortText(DOCUMENT_SHORT_TEXT);
    Base64Data base64Data = new Base64Data();
    base64Data.setValue(prescription);
    base64Data.setMimeType(DOCUMENT_MIME_TYPE);
    documentType.setBase64Data(base64Data);
    signRequest.setDocument(documentType);

    signRequest.setIncludeRevocationInfo(SignatureServiceHelper.INCLUDE_REVOCATION_INFO);
    signRequests.add(signRequest);

    return (SignDocumentResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                signDocument,
                new SoapActionCallback(SOAP_ACTION_SIGN_DOCUMENT));
  }
}
