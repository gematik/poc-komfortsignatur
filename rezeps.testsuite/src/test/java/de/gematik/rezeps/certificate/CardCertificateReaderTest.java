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

package de.gematik.rezeps.certificate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType.X509DataInfo;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType.X509DataInfo.X509Data;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CardCertificateReaderTest {

  private static final String MANDANT = "mandantId";
  private static final String CLIENT_SYSTEM = "clientSystemId";
  private static final String WORKPLACE = "workplaceId";
  private static final String CARD_HANDLE = "HBA-1";
  private static final byte[] EXPECTED_AUT_CERTIFICATE =
      "Ich bin ein AUT-Zertifikat".getBytes(StandardCharsets.UTF_8);

  @Test
  public void shouldReadCardCertificate() throws IOException {
    PerformReadCardCertificate performReadCardCertificate = mock(PerformReadCardCertificate.class);

    ReadCardCertificateResponse readCardCertificateResponse =
        mock(ReadCardCertificateResponse.class);

    Status status = new Status();
    status.setResult("OK");
    when(readCardCertificateResponse.getStatus()).thenReturn(status);

    X509DataInfoListType x509DataInfoListType = new X509DataInfoListType();
    List<X509DataInfo> x509DataInfos = x509DataInfoListType.getX509DataInfo();
    X509DataInfo x509DataInfo = new X509DataInfo();
    X509Data x509Data = new X509Data();
    x509Data.setX509Certificate(EXPECTED_AUT_CERTIFICATE);
    x509DataInfo.setX509Data(x509Data);
    x509DataInfos.add(x509DataInfo);
    when(readCardCertificateResponse.getX509DataInfoList()).thenReturn(x509DataInfoListType);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(performReadCardCertificate.performReadCardCertificate(invocationContext, CARD_HANDLE))
        .thenReturn(readCardCertificateResponse);

    CardCertificateReader cardCertificateReader = new CardCertificateReader();
    cardCertificateReader.performReadCardCertificate = performReadCardCertificate;

    byte[] autCertificate =
        cardCertificateReader.readCardCertificate(invocationContext, CARD_HANDLE);

    Assert.assertEquals(EXPECTED_AUT_CERTIFICATE, autCertificate);
  }
}
