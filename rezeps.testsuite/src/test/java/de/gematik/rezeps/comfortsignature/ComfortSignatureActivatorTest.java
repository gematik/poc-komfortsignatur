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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.ActivateComfortSignatureResponse;
import de.gematik.ws.conn.signatureservice.v7.SignatureModeEnum;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class ComfortSignatureActivatorTest {

  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "clientSystem";
  private static final String WORKPLACE = "workplace";
  private static final String HBA_HANDLE = "hba_handle";
  private static final String STATUS_OK = "OK";

  @Test
  public void shouldActivateComfortSignature() throws IOException {
    PerformActivateComfortSignature performActivateComfortSignature =
        mock(PerformActivateComfortSignature.class);

    ActivateComfortSignatureResponse activateComfortSignatureResponse =
        mock(ActivateComfortSignatureResponse.class);
    Status status = new Status();
    status.setResult(STATUS_OK);
    when(activateComfortSignatureResponse.getStatus()).thenReturn(status);
    when(activateComfortSignatureResponse.getSignatureMode()).thenReturn(SignatureModeEnum.COMFORT);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(performActivateComfortSignature.performActivateComfortSignature(
            invocationContext, HBA_HANDLE))
        .thenReturn(activateComfortSignatureResponse);

    ComfortSignatureActivator comfortSignatureActivator = new ComfortSignatureActivator();
    comfortSignatureActivator.performActivateComfortSignature = performActivateComfortSignature;
    ComfortSignatureResult comfortSignatureResult =
        comfortSignatureActivator.activateComfortSignature(invocationContext, HBA_HANDLE);
    Assert.assertTrue(comfortSignatureResult.isComfortSignatureActivated());
  }
}
