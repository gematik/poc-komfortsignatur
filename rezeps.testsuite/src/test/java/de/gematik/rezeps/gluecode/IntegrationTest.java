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

package de.gematik.rezeps.gluecode;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.KonnektorClient;
import de.gematik.rezeps.dataexchange.create.MedicationData;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

public class IntegrationTest {

  @Test
  @Ignore
  public void shouldInvokeTaskActivate()
      throws IOException, NotBoundException, MissingPreconditionException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    invokeTaskActivate(fdClientGlueCode);
    Assert.assertTrue(fdClientGlueCode.checkTaskActivateOk());
  }

  private void invokeTaskActivate(FdClientGlueCode fdClientGlueCode)
      throws MissingPreconditionException {
    fdClientGlueCode.invokeTaskCreate();

    KonnektorGlueCode konnektorGlueCode =
        new KonnektorGlueCode(SpringApplication.run(KonnektorClient.class, new String[] {}));
    konnektorGlueCode.modifyBundleWithTaskData();

    InvocationContext invocationContext =
        new InvocationContext("Mandant1", "ClientID1", "Workplace1", "User");
    TestcaseData.getInstance().setInvocationContext(invocationContext);
    konnektorGlueCode.determineHbaHandle();
    konnektorGlueCode.determineJobNumber();
    konnektorGlueCode.signPrescription();

    fdClientGlueCode.invokeTaskActivate();
  }

  @Test
  @Ignore
  public void shouldInvokeTaskAccept()
      throws RemoteException, NotBoundException, MissingPreconditionException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    invokeTaskActivate(fdClientGlueCode);

    fdClientGlueCode.invokeTaskAccept();
    Assert.assertTrue(fdClientGlueCode.checkTaskAcceptOk());
  }

  @Test
  @Ignore
  public void shouldInvokeTaskClose()
      throws RemoteException, NotBoundException, MissingPreconditionException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    invokeTaskActivate(fdClientGlueCode);

    fdClientGlueCode.invokeTaskAccept();
    fdClientGlueCode.invokeTaskClose(
        new MedicationData("06313728", "Sumatriptan-1a Pharma 100 mg Tabletten"));
    Assert.assertNotNull(TestcaseData.getInstance().getTaskCloseData());
  }
}
