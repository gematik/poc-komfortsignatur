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

import de.gematik.rezeps.KonnektorHelper;
import de.gematik.rezeps.SoapClientInterceptor;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.Marshaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

/**
 * Die Klasse wird von Spring-Boot für das Marshalling und Unmarshalling von SOAP-Nachrichten
 * verwendet. Die enthaltenen Methoden sind nicht durch Anwendungs-Entwickler aufzurufen.
 */
@Configuration
public class GetJobNumberConfiguration {

  @Bean
  public Jaxb2Marshaller getJobNumberMarshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in
    // pom.xml
    marshaller.setPackagesToScan("de.gematik.ws.conn.signatureservice.v7_4");

    Map<String, Object> map = new HashMap<>();
    map.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    map.put(Marshaller.JAXB_ENCODING, "UTF-8");
    map.put(Marshaller.JAXB_FRAGMENT, true);
    marshaller.setMarshallerProperties(map);
    return marshaller;
  }

  @Bean
  public PerformGetJobNumber performGetJobNumber(Jaxb2Marshaller signDocumentMarshaller)
      throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
          KeyStoreException, KeyManagementException {
    PerformGetJobNumber performGetJobNumber = new PerformGetJobNumber();
    performGetJobNumber.setDefaultUri(KonnektorHelper.determineSignatureServiceEndpoint());
    performGetJobNumber.setMarshaller(signDocumentMarshaller);
    performGetJobNumber.setUnmarshaller(signDocumentMarshaller);
    // hier wird der MessageSender für TLS mit beidseitiger Authentisierung gesetzt
    performGetJobNumber.setMessageSender(KonnektorHelper.determineHttpComponentsMessageSender());
    performGetJobNumber.setInterceptors(new ClientInterceptor[] {interceptor()});
    return performGetJobNumber;
  }

  @Bean
  public SoapClientInterceptor interceptor() {
    return new SoapClientInterceptor();
  }
}
