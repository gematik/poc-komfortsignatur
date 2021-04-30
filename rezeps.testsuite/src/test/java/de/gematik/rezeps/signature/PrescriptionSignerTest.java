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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.DocumentType;
import de.gematik.ws.conn.signatureservice.v7.SignDocumentResponse;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import de.gematik.ws.conn.signatureservice.v7.SignResponse.OptionalOutputs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.junit.Assert;
import org.junit.Test;

public class PrescriptionSignerTest {

  private static final String MANDANT = "mandant001";
  private static final String CLIENT_SYSTEM = "client_system001";
  private static final String WORKPLACE = "workplace001";
  private static final String USER = "user001";
  private static final String CARD_HANDLE = "hba_handle001";
  private static final String PRESCRIPTIPN = "Beschreibung eines wirksamen Mendikamentes";
  private static final byte[] SIGNED_PRESCRIPTION =
      "Signierte Beschreibung eines wirksamen Mendikamentes".getBytes();
  private static final String JOB_NUMBER = "4711";

  @Test
  public void shouldPerformSignPrescription() throws IOException {
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);

    PerformSignDocument performSignDocument = mock(PerformSignDocument.class);
    SignDocumentResponse signDocumentResponse = determineSignDocumentResponse();
    when(performSignDocument.performSignDocument(
            invocationContext, CARD_HANDLE, PRESCRIPTIPN.getBytes(), JOB_NUMBER))
        .thenReturn(signDocumentResponse);

    PrescriptionSigner prescriptionSigner = new PrescriptionSigner();
    prescriptionSigner.performSignDocument = performSignDocument;
    SignDocumentResult signDocumentResult =
        prescriptionSigner.performSignPrescription(
            invocationContext, CARD_HANDLE, PRESCRIPTIPN, JOB_NUMBER);
    Assert.assertEquals(SIGNED_PRESCRIPTION, signDocumentResult.getSignedBundle());
  }

  private SignDocumentResponse determineSignDocumentResponse() {

    SignResponse signResponse = new SignResponse();

    Status status = new Status();
    status.setResult(SignDocumentResult.STATUS_OK);
    signResponse.setStatus(status);

    signResponse.setOptionalOutputs(determineOptionalOutputs());

    signResponse.setSignatureObject(determineSignatureObject());

    List<SignResponse> signResponseList = new ArrayList<>();
    signResponseList.add(signResponse);

    SignDocumentResponse signDocumentResponse = mock(SignDocumentResponse.class);
    when(signDocumentResponse.getSignResponse()).thenReturn(signResponseList);

    return signDocumentResponse;
  }

  private SignatureObject determineSignatureObject() {
    Base64Signature base64Signature = new Base64Signature();
    base64Signature.setType(SignDocumentResult.TYPE_BASE_64_SIGNATURE);
    base64Signature.setValue(SIGNED_PRESCRIPTION);

    SignatureObject signatureObject = new SignatureObject();
    signatureObject.setBase64Signature(base64Signature);

    return signatureObject;
  }

  private OptionalOutputs determineOptionalOutputs() {
    Base64Data base64Data = new Base64Data();
    base64Data.setMimeType(SignDocumentResult.MIME_TYPE_BASE_64_DATA);

    DocumentType documentType = new DocumentType();
    documentType.setBase64Data(base64Data);

    OptionalOutputs optionalOutputs = new OptionalOutputs();
    optionalOutputs.setDocumentWithSignature(documentType);

    return optionalOutputs;
  }
}
