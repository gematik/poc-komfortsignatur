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
import de.gematik.ws.conn.signatureservice.v7.VerifyDocument;
import de.gematik.ws.conn.signatureservice.v7.VerifyDocument.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7.VerifyDocumentResponse;
import java.io.IOException;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_.ReturnVerificationReport;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformVerifyDocument extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_VERIFY_DOCUMENT =
      "http://ws.gematik.de/conn/SignatureService/v7.5#VerifyDocument";

  public VerifyDocumentResponse performVerifyDocument(
      InvocationContext invocationContext, byte[] signedPrescription) throws IOException {
    VerifyDocument verifyDocument = new VerifyDocument();
    verifyDocument.setContext(invocationContext.convertToContextType());
    verifyDocument.setTvMode(SignatureServiceHelper.TV_MODE_NONE);

    OptionalInputs optionalInputs = new OptionalInputs();
    ReturnVerificationReport returnVerificationReport = new ReturnVerificationReport();
    returnVerificationReport.setIncludeVerifier(false);
    returnVerificationReport.setIncludeCertificateValues(true);
    returnVerificationReport.setIncludeRevocationValues(true);
    returnVerificationReport.setExpandBinaryValues(false);
    returnVerificationReport.setReportDetailLevel(
        "urn:oasis:names:tc:dss-x:1.0:profiles:verificationreport:reportdetail:allDetails");
    optionalInputs.setReturnVerificationReport(returnVerificationReport);
    verifyDocument.setOptionalInputs(optionalInputs);

    Base64Signature base64Signature = new Base64Signature();
    base64Signature.setType(SignatureServiceHelper.SIGNATURE_TYPE);
    base64Signature.setValue(signedPrescription);
    SignatureObject signatureObject = new SignatureObject();
    signatureObject.setBase64Signature(base64Signature);
    verifyDocument.setSignatureObject(signatureObject);

    verifyDocument.setIncludeRevocationInfo(false);

    return (VerifyDocumentResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                verifyDocument,
                new SoapActionCallback(SOAP_ACTION_VERIFY_DOCUMENT));
  }
}
