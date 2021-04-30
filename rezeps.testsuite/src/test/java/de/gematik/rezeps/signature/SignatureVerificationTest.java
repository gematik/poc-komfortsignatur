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
import de.gematik.ws.conn.signatureservice.v7.VerificationResultType;
import de.gematik.ws.conn.signatureservice.v7.VerifyDocumentResponse;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class SignatureVerificationTest {

  private static final String MANDANT = "mandant001";
  private static final String CLIENT_SYSTEM = "client_system001";
  private static final String WORKPLACE = "workplace001";
  private static final byte[] SIGNED_PRESCRIPTION = "Ich bin ein signiertes Rezept".getBytes();

  @Test
  public void shouldValidateSignature() throws IOException {
    VerifyDocumentResponse verifyDocumentResponse = new VerifyDocumentResponse();

    Status status = new Status();
    status.setResult(SignatureVerification.INVOCATION_RESULT_OK);
    verifyDocumentResponse.setStatus(status);

    VerificationResultType verificationResultType = new VerificationResultType();
    verificationResultType.setHighLevelResult(SignatureVerification.VALIDATION_RESULT_VALID);
    verifyDocumentResponse.setVerificationResult(verificationResultType);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    PerformVerifyDocument performVerifyDocument = mock(PerformVerifyDocument.class);
    when(performVerifyDocument.performVerifyDocument(invocationContext, SIGNED_PRESCRIPTION))
        .thenReturn(verifyDocumentResponse);

    SignatureVerification signatureVerification = new SignatureVerification();
    signatureVerification.performVerifyDocument = performVerifyDocument;
    SignatureVerificationResult signatureVerificationResult =
        signatureVerification.verifySignature(invocationContext, SIGNED_PRESCRIPTION);

    SignatureVerificationResult expectedSignatureVerificationResult =
        new SignatureVerificationResult(
            SignatureVerification.INVOCATION_RESULT_OK,
            SignatureVerification.VALIDATION_RESULT_VALID);
    Assert.assertEquals(expectedSignatureVerificationResult, signatureVerificationResult);
  }
}
