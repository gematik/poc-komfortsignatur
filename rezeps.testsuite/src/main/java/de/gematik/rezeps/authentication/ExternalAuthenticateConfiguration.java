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
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

/**
 * Die Klasse wird von Spring-Boot für das Marshalling und Unmarshalling von SOAP-Nachrichten
 * verwendet. Die enthaltenen Methoden sind nicht durch Anwendungs-Entwickler aufzurufen.
 */
@Configuration
public class ExternalAuthenticateConfiguration extends WsConfigurerAdapter {

  @Bean
  public Jaxb2Marshaller externalAuthenticateMarshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in
    // pom.xml
    // "marshaller.setContextPath(WsdlContexts.SIGNATURE_SERVICE_CONTEXT);"
    marshaller.setPackagesToScan("de.gematik.ws.conn.signatureservice.v7_4");

    Map<String, Object> map = new HashMap<>();
    map.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    map.put(Marshaller.JAXB_ENCODING, "UTF-8");
    map.put(Marshaller.JAXB_FRAGMENT, true);
    marshaller.setMarshallerProperties(map);
    return marshaller;
  }

  @Bean
  public PerformExternalAuthenticate performExternalAuthenticate(
      Jaxb2Marshaller externalAuthenticateMarshaller)
      throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
          KeyStoreException, KeyManagementException {
    PerformExternalAuthenticate client = new PerformExternalAuthenticate();
    client.setDefaultUri(KonnektorHelper.determineAuthSignatureServiceEndpoint());
    client.setMarshaller(externalAuthenticateMarshaller);
    client.setUnmarshaller(externalAuthenticateMarshaller);
    // hier wird der MessageSender für TLS mit beidseitiger Authentisierung gesetzt
    client.setMessageSender(KonnektorHelper.determineHttpComponentsMessageSender());

    client.setInterceptors(new ClientInterceptor[] {interceptor()});
    return client;
  }

  @Bean
  public SoapClientInterceptor interceptor() {
    return new SoapClientInterceptor();
  }
}
