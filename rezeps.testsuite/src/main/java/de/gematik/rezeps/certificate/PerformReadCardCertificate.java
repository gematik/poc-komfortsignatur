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
import de.gematik.rezeps.KonnektorHelper;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificate.CertRefList;
import de.gematik.ws.conn.certificateservice.v6.ReadCardCertificateResponse;
import de.gematik.ws.conn.certificateservicecommon.v2.CertRefEnum;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformReadCardCertificate extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_READ_CARD_CERTIFICATE =
      "http://ws.gematik.de/conn/CertificateService/v6.0#ReadCardCertificate";

  /**
   * Liest das AUT-Zertifikat einer Karte.
   *
   * @param invocationContext Der Aufrufkontext über den die Karte zugegriffen werden kann.
   * @param cardHandle Das Handle der Karte, deren AUT-Zertifikat gelesen werden soll.
   * @return Das gelesene AUT-Zertifikat.
   */
  public ReadCardCertificateResponse performReadCardCertificate(
      InvocationContext invocationContext, String cardHandle) throws IOException {

    ReadCardCertificate readCardCertificate = new ReadCardCertificate();
    ContextType contextType = invocationContext.convertToContextType();
    readCardCertificate.setContext(contextType);

    readCardCertificate.setCardHandle(cardHandle);

    CertRefList certRefList = new CertRefList();
    certRefList.getCertRef().add(CertRefEnum.C_AUT);
    readCardCertificate.setCertRefList(certRefList);

    return (ReadCardCertificateResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineCertificateServiceEndpoint(),
                readCardCertificate,
                new SoapActionCallback(SOAP_ACTION_READ_CARD_CERTIFICATE));
  }
}
