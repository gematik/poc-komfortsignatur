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
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.GetJobNumber;
import de.gematik.ws.conn.signatureservice.v7.GetJobNumberResponse;
import java.io.IOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

public class PerformGetJobNumber extends WebServiceGatewaySupport {

  private static final String SOAP_ACTION_GET_JOB_NUMBER =
      "http://ws.gematik.de/conn/SignatureService/v7.5#GetJobNumber";

  public GetJobNumberResponse performGetJobNumber(InvocationContext invocationContext)
      throws IOException {

    GetJobNumber getJobNumber = new GetJobNumber();
    ContextType contextType = invocationContext.convertToContextType();
    getJobNumber.setContext(contextType);

    return (GetJobNumberResponse)
        getWebServiceTemplate()
            .marshalSendAndReceive(
                KonnektorHelper.determineSignatureServiceEndpoint(),
                getJobNumber,
                new SoapActionCallback(SOAP_ACTION_GET_JOB_NUMBER));
  }
}
