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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.ExternalAuthenticateResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import org.junit.Assert;
import org.junit.Test;

public class ExternalAuthenticatorTest {

  private static final String MANDANT = "mandantId";
  private static final String CLIENT_SYSTEM = "clientSystemId";
  private static final String WORKPLACE = "workplaceId";
  private static final String CARD_HANDLE = "HBA-1";
  private static final byte[] DATA_TO_BE_SIGNED =
      "ich moechte signiert werden".getBytes(StandardCharsets.UTF_8);
  private static final byte[] SIGNED_DATA =
      "ich moechte signiert werden".getBytes(StandardCharsets.UTF_8);
  private static final String STATUS_OK = "OK";

  @Test
  public void shouldAuthenticateExternally() throws IOException {
    Status status = new Status();
    status.setResult(STATUS_OK);

    ExternalAuthenticateResponse externalAuthenticateResponse =
        mock(ExternalAuthenticateResponse.class);
    when(externalAuthenticateResponse.getStatus()).thenReturn(status);

    Base64Signature base64Signature = new Base64Signature();
    base64Signature.setValue(SIGNED_DATA);
    SignatureObject signatureObject = new SignatureObject();
    signatureObject.setBase64Signature(base64Signature);
    when(externalAuthenticateResponse.getSignatureObject()).thenReturn(signatureObject);

    PerformExternalAuthenticate performExternalAuthenticate =
        mock(PerformExternalAuthenticate.class);
    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(performExternalAuthenticate.performExternalAuthenticate(
            invocationContext, CARD_HANDLE, DATA_TO_BE_SIGNED))
        .thenReturn(externalAuthenticateResponse);

    ExternalAuthenticator externalAuthenticator = new ExternalAuthenticator();
    externalAuthenticator.performExternalAuthenticate = performExternalAuthenticate;

    byte[] signedData =
        externalAuthenticator.authenticateExternally(
            invocationContext, CARD_HANDLE, DATA_TO_BE_SIGNED);
    Assert.assertEquals(SIGNED_DATA, signedData);
  }
}
