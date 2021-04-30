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

package de.gematik.rezeps.comfortsignature;

import static org.mockito.Mockito.*;

import de.gematik.rezeps.gluecode.TestcaseData;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.DeactivateComfortSignatureResponse;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class ComfortSignatureDeactivatorTest {

  private static final String STATUS_NOK = "NOK";

  @Test
  public void shouldDeactivateComfortSignature() throws IOException {
    PerformDeactivateComfortSignature performDeactivateComfortSignature =
        mock(PerformDeactivateComfortSignature.class);
    DeactivateComfortSignatureResponse deactivateComfortSignatureResponse =
        mock(DeactivateComfortSignatureResponse.class);
    ComfortSignatureDeactivator comfortSignatureDeActivator = new ComfortSignatureDeactivator();

    String cardHandle = TestcaseData.getInstance().getHbaHandle();

    Status status = new Status();
    status.setResult(STATUS_NOK);
    when(deactivateComfortSignatureResponse.getStatus()).thenReturn(status);

    when(performDeactivateComfortSignature.performDeActivateComfortSignature(cardHandle))
        .thenReturn(deactivateComfortSignatureResponse);
    comfortSignatureDeActivator.performDeactivateComfortSignature =
        performDeactivateComfortSignature;

    ComfortSignatureResult comfortSignatureResult =
        comfortSignatureDeActivator.deactivateComfortSignature(cardHandle);
    Assert.assertNull(comfortSignatureResult.getSignatureMode());

    Assert.assertEquals(status, deactivateComfortSignatureResponse.getStatus());
    verify(deactivateComfortSignatureResponse, times(2)).getStatus();
  }
}
