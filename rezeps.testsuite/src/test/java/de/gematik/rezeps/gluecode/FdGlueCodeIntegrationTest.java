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

import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FdGlueCodeIntegrationTest {

  @Test
  @Ignore
  public void shouldInvokeTaskCreate() throws RemoteException, NotBoundException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    fdClientGlueCode.invokeTaskCreate();
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    Assert.assertNotNull("TaskData sollte verfügbar sein");
    String taskId = taskCreateData.getTaskId();
    String prescriptionId = taskCreateData.getPrescriptionId();
    String accessCode = taskCreateData.getAccessCode();
    Assert.assertEquals("Der Status Codes sollte 201 sein", 201, taskCreateData.getStatusCode());
    Assert.assertTrue("Eine TaskID sollte vorhanden sein.", hasValue(taskId));
    Assert.assertTrue("Eine PrescriptionID sollte vorhanden sein.", hasValue(prescriptionId));
    Assert.assertTrue("Ein AccessCode sollte vorhanden sein.", hasValue(accessCode));
    Assert.assertEquals("Der Status sollte draft sein", "draft", taskCreateData.getStatus());
  }

  private boolean hasValue(String string) {
    return string != null && string.length() > 0;
  }

  @Test
  @Ignore
  public void shouldCheckTaskCreatedOk() throws RemoteException, NotBoundException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    fdClientGlueCode.invokeTaskCreate();
    Assert.assertTrue(fdClientGlueCode.checkTaskCreatedOk());
  }
}
