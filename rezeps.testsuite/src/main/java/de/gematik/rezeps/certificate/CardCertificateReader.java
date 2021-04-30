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

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType;
import de.gematik.ws.conn.certificateservicecommon.v2.X509DataInfoListType.X509DataInfo;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardCertificateReader {

  private static final String STATUS_OK = "OK";

  @Autowired PerformReadCardCertificate performReadCardCertificate;

  /**
   * Liest das AUT-Zertifikat einer im Konnektor verwalteten Karte.
   *
   * @param invocationContext Der Kontext für den Aufruf beim Konnektor.
   * @param cardHandle Das Handle der Karte.
   * @return Das AUT-Zertifikat der Karte.
   */
  public byte[] readCardCertificate(InvocationContext invocationContext, String cardHandle)
      throws IOException {
    byte[] x509Certificate = null;

    ReadCardCertificateResponse readCardCertificateResponse =
        performReadCardCertificate.performReadCardCertificate(invocationContext, cardHandle);

    Status status = readCardCertificateResponse.getStatus();
    if (status != null && status.getResult().equals(STATUS_OK)) {
      X509DataInfoListType x509DataInfoList = readCardCertificateResponse.getX509DataInfoList();
      List<X509DataInfo> x509DataInfos = x509DataInfoList.getX509DataInfo();
      if (x509DataInfos != null && !x509DataInfos.isEmpty()) {
        X509DataInfo x509DataInfo = x509DataInfos.get(0);
        x509Certificate = x509DataInfo.getX509Data().getX509Certificate();
      }
    }

    return x509Certificate;
  }
}
