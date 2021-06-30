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
import de.gematik.ws.conn.cardservice.v8.GetPinStatusResponse;
import de.gematik.ws.conn.cardservice.v8.PinStatusEnum;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

public class PinStatusTest {

  public static final String PIN_TYPE_PIN_QES = "PIN.QES";
  public static final String CARD_HANDLE = "HBA-1";

  @Test
  public void shouldGetPinStatusResponse() throws IOException {
    InvocationContext invocationContext =
        new InvocationContext("Mandant1", "ClientSystem1", "Anmeldung");

    GetPinStatusResponse getPinStatusResponseDummy = new GetPinStatusResponse();
    getPinStatusResponseDummy.setPinStatus(PinStatusEnum.BLOCKED);
    Status status = new Status();
    status.setResult("SUPER:)");
    getPinStatusResponseDummy.setStatus(status);
    getPinStatusResponseDummy.setLeftTries(BigInteger.TEN);

    PerformGetPinStatus performGetPinStatusMock = mock(PerformGetPinStatus.class);
    when(performGetPinStatusMock.getPinStatusResponse(
            invocationContext, PIN_TYPE_PIN_QES, CARD_HANDLE))
        .thenReturn(getPinStatusResponseDummy);

    PinStatus pinStatus = new PinStatus();
    pinStatus.performGetPinStatus = performGetPinStatusMock;
    PinStatusResult pinStatusResult =
        pinStatus.getPinStatusResponse(invocationContext, PIN_TYPE_PIN_QES, CARD_HANDLE);
    Assert.assertEquals(status, pinStatusResult.getStatus());
    Assert.assertEquals(PinStatusEnum.BLOCKED, pinStatusResult.getPinStatusEnum());
    Assert.assertEquals(BigInteger.TEN, pinStatusResult.getLeftTries());
  }
}
