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

package de.gematik.rezeps;

import java.text.MessageFormat;
import javax.xml.soap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class SoapClientInterceptor implements ClientInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoapClientInterceptor.class);

  @Override
  public boolean handleRequest(MessageContext messageContext) {
    return true;
  }

  @Override
  public boolean handleResponse(MessageContext messageContext) {
    return true;
  }

  @Override
  public boolean handleFault(MessageContext messageContext) {
    WebServiceMessage message = messageContext.getResponse();
    SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
    SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    try {
      SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
      SOAPBody soapBody = soapEnvelope.getBody();
      SOAPFault soapFault = soapBody.getFault();
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(
            MessageFormat.format(
                "Error occurred while invoking web service - {} ", soapFault.getFaultString()));
      }

    } catch (Exception exception) {
      LOGGER.error(exception.getMessage());
    }
    return true;
  }

  @Override
  public void afterCompletion(MessageContext messageContext, Exception ex) {
    if (ex != null) {
      LOGGER.error(ex.getMessage());
    }
  }
}
