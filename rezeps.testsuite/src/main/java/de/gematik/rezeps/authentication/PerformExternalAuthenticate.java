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
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7_4.BinaryDocumentType;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticate.OptionalInputs;
import de.gematik.ws.conn.signatureservice.v7_4.ExternalAuthenticateResponse;
import java.io.IOException;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import org.springframework.util.MimeTypeUtils;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformExternalAuthenticate extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_EXTERNAL_AUTHENTICATE =
      "http://ws.gematik.de/conn/SignatureService/v7.4#ExternalAuthenticate";
  private static final String SIGNATURE_TYPE = "urn:ietf:rfc:3447";
  private static final String SIGNATURE_SCHEME = "RSASSA-PSS";

  /**
   * Führt ein ExternalAuthenticate mittels SMC-B beim Konnektor durch.
   *
   * @param invocationContext Der für den Aufruf zu verwendende Kontext. Die für die Signatur zu
   *     verwendende SMC-B muss in diesem Kontext sichtbar sein.
   * @param cardHandle Das Handle der zu verwendenden SMC-B.
   * @param dataToBeSigned Die zu signierenden Daten.
   * @return Die Response des Konnektors.
   */
  public ExternalAuthenticateResponse performExternalAuthenticate(
      InvocationContext invocationContext, String cardHandle, byte[] dataToBeSigned)
      throws IOException {

    ExternalAuthenticate externalAuthenticate = new ExternalAuthenticate();

    externalAuthenticate.setCardHandle(cardHandle);

    ContextType contextType = invocationContext.convertToContextType();
    externalAuthenticate.setContext(contextType);

    OptionalInputs optionalInputs = new OptionalInputs();
    optionalInputs.setSignatureType(SIGNATURE_TYPE);
    optionalInputs.setSignatureSchemes(SIGNATURE_SCHEME);
    externalAuthenticate.setOptionalInputs(optionalInputs);

    Base64Data base64Data = new Base64Data();
    base64Data.setValue(dataToBeSigned);
    base64Data.setMimeType(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
    BinaryDocumentType binaryDocumentType = new BinaryDocumentType();
    binaryDocumentType.setBase64Data(base64Data);

    externalAuthenticate.setBinaryString(binaryDocumentType);
    return (ExternalAuthenticateResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineAuthSignatureServiceEndpoint(),
                externalAuthenticate,
                new SoapActionCallback(SOAP_ACTION_EXTERNAL_AUTHENTICATE));
  }
}
