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

package de.gematik.rezeps.card;

import de.gematik.rezeps.KonnektorHelper;
import de.gematik.rezeps.SoapClientInterceptor;
import de.gematik.rezeps.WsdlContexts;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

@Configuration
public class GetPinStatusConfiguration {

  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in pom.xml
    marshaller.setContextPath(WsdlContexts.CARD_SERVICE_CONTEXT);
    return marshaller;
  }

  @Bean
  public PerformGetPinStatus performGetPinStatus(Jaxb2Marshaller marshaller)
      throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
          KeyStoreException, KeyManagementException {
    PerformGetPinStatus getPinStatus = new PerformGetPinStatus();
    getPinStatus.setMarshaller(marshaller);
    getPinStatus.setDefaultUri(KonnektorHelper.determineEventServiceEndpoint());
    getPinStatus.setUnmarshaller(marshaller);
    getPinStatus.setMessageSender(KonnektorHelper.determineHttpComponentsMessageSender());

    getPinStatus.setInterceptors(new ClientInterceptor[] {interceptor()});
    return getPinStatus;
  }

  @Bean
  public SoapClientInterceptor interceptor() {
    return new SoapClientInterceptor();
  }
}
