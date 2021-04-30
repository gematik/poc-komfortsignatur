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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.dataexchange.create.FdClient;
import de.gematik.rezeps.dataexchange.create.MedicationData;
import de.gematik.rezeps.dataexchange.create.TaskAcceptData;
import de.gematik.rezeps.dataexchange.create.TaskActivateData;
import de.gematik.rezeps.dataexchange.create.TaskCloseData;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import de.gematik.rezeps.signature.SignDocumentResult;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import org.junit.Assert;
import org.junit.Test;

public class FdClientGlueCodeTest {

  private static final String ACCESS_TOKEN_PRESCRIBING_ENTITY = "access-token-0-8-15";
  private static final String STATUS_READY = "ready";
  private static final String STATUS_IN_PROGRESS = "in-progress";
  private static final byte[] SIGNATURE = "signature".getBytes(StandardCharsets.UTF_8);
  private static final int STATUS_CODE_200 = 200;

  @Test
  public void shouldInvokeTaskCreate() throws RemoteException {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", "draft");
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskCreate(ACCESS_TOKEN_PRESCRIBING_ENTITY)).thenReturn(taskCreateData);

    TestcaseData.getInstance().setAccessTokenPrescribingEntity(ACCESS_TOKEN_PRESCRIBING_ENTITY);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskCreate();

    Assert.assertEquals(taskCreateData, TestcaseData.getInstance().getTaskCreateData());
  }

  @Test
  public void shouldValidateCheckTaskCreatedOk() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", "draft");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskCreatedOk());
  }

  @Test
  public void shouldFailCheckTaskCreatedOk() {
    TaskCreateData taskCreateData =
        new TaskCreateData(401, "4711", "prescription.0.8.15", "access-code", "draft");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkTaskCreatedOk());
  }

  @Test
  public void shouldInvokeTaskActivate() throws RemoteException {
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);

    byte[] signedPrescription = "ich bin ein signiertes Rezept".getBytes(StandardCharsets.UTF_8);
    SignDocumentResult signDocumentResult = new SignDocumentResult("", "", "", signedPrescription);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setSignDocumentResult(signDocumentResult);

    TaskActivateData expectedTaskActivateData = new TaskActivateData(200, STATUS_READY);
    when(fdClient.invokeTaskActivat(signedPrescription)).thenReturn(expectedTaskActivateData);

    fdClientGlueCode.invokeTaskActivate();

    TaskActivateData taskActivateData = testcaseData.getTaskActivateData();
    Assert.assertEquals(expectedTaskActivateData, taskActivateData);
  }

  @Test
  public void shouldValidateCheckTaskActivateOk() {
    TaskActivateData taskActivateData = new TaskActivateData(200, STATUS_READY);
    TestcaseData.getInstance().setTaskActivateData(taskActivateData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskActivateOk());
  }

  @Test
  public void shouldFailCheckTaskActivateOk() {
    TaskActivateData taskActivateData = new TaskActivateData(400, "");
    TestcaseData.getInstance().setTaskActivateData(taskActivateData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkTaskActivateOk());
  }

  @Test
  public void shouldValidateCheckTaskAcceptOk() {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskAcceptOk());
  }

  @Test
  public void shouldFailCheckTaskAcceptPharmacistOk() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(400, "");
    TestcaseData.getInstance().setTaskAcceptData(taskAcceptData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkTaskAcceptOk());
  }

  @Test
  public void shouldInvokeTaskClose() throws RemoteException {
    FdClient fdClient = mock(FdClient.class);
    MedicationData medicationData = new MedicationData("pznValue", "pznText");
    TaskCloseData taskCloseData = new TaskCloseData(STATUS_CODE_200, SIGNATURE);
    when(fdClient.invokeTaskClose(medicationData)).thenReturn(taskCloseData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskClose(medicationData);

    TaskCloseData taskCloseDataAfterExecution = TestcaseData.getInstance().getTaskCloseData();
    Assert.assertEquals(taskCloseData, taskCloseDataAfterExecution);
  }

  @Test
  public void shouldValidateCheckTaskCloseOk() {
    TaskCloseData taskCloseData = new TaskCloseData(STATUS_CODE_200, SIGNATURE);
    TestcaseData.getInstance().setTaskCloseData(taskCloseData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseOk());
  }

  @Test
  public void shouldFailCheckTaskClose() {
    TaskCloseData taskCloseData = new TaskCloseData(400, SIGNATURE);
    TestcaseData.getInstance().setTaskCloseData(taskCloseData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkTaskCloseOk());
  }

  @Test
  public void shouldNotFailCheckTaskIdAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData("taskId", "prescriptionId", "accessCode");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkTaskIdAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckTaskIdAvailableOnMissingTaskCreateData()
      throws MissingPreconditionException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkTaskIdAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckTaskIdAvailableOnMissingTaskId() throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData(null, "prescriptionId", "accessCode");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkTaskIdAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckTaskIdAvailableOnEmptyTaskId() throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData("", "prescriptionId", "accessCode");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkTaskIdAvailable();
  }

  @Test
  public void shouldNotFailCheckAccessCodeAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData("taskId", "prescriptionId", "accessCode");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkAccessCodeAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckAccessCodeAvailableOnMissingTaskCreateData()
      throws MissingPreconditionException {
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkAccessCodeAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckAccessCodeAvailableOnMissingAccessCode()
      throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData("taskId", "prescriptionId", null);
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkAccessCodeAvailable();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckAccessCodeAvailableOnEmptyAccessCode()
      throws MissingPreconditionException {
    TaskCreateData taskCreateData = new TaskCreateData("taskId", "prescriptionId", "");
    TestcaseData.getInstance().setTaskCreateData(taskCreateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.checkAccessCodeAvailable();
  }
}
