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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardservicecommon.v2.PinResponseType;
import de.gematik.ws.conn.cardservicecommon.v2.PinResultEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class PinVerifierTest {

  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "client systen";
  private static final String WORKPLACE = "workplace";
  private static final String USER = "user";
  private static final String CARD_HANDLE = "hba_1";
  private static final String STATUS_OK = "OK";

  @Test
  public void shouldVerifyPin() throws IOException {
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);

    PinResponseType pinResponseType = new PinResponseType();
    Status status = new Status();
    status.setResult(STATUS_OK);
    pinResponseType.setStatus(status);
    pinResponseType.setPinResult(PinResultEnum.OK);

    PerformVerifyPin performVerifyPin = mock(PerformVerifyPin.class);
    when(performVerifyPin.performVerifyPin(invocationContext, CARD_HANDLE))
        .thenReturn(pinResponseType);

    PinVerifier pinVerifier = new PinVerifier();
    pinVerifier.performVerifyPin = performVerifyPin;
    VerifyPinResult verifyPinResult = pinVerifier.performVerifyPin(invocationContext, CARD_HANDLE);

    VerifyPinResult expectedVerifyPinResult =
        new VerifyPinResult(STATUS_OK, PinResultEnum.OK.value());
    Assert.assertEquals(expectedVerifyPinResult, verifyPinResult);
  }
}
