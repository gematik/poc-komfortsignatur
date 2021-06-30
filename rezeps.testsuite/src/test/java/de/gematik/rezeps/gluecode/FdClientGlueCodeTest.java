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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.TestUtils;
import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.dataexchange.*;
import de.gematik.rezeps.fd.FdClient;
import de.gematik.rezeps.signature.SignDocumentResult;
import de.gematik.test.erezept.fd.actors.CommunicationActor;
import de.gematik.test.erezept.fd.actors.response.ResponseEnvelope;
import de.gematik.test.erezept.fd.fhir.adapter.*;
import de.gematik.test.erezept.fd.fhir.helper.Profile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class FdClientGlueCodeTest {

  private static final String ACCESS_TOKEN_PRESCRIBING_ENTITY = "access-token-0-8-15";
  private static final String STATUS_READY = "ready";
  private static final String STATUS_IN_PROGRESS = "in-progress";
  private static final byte[] SIGNATURE = "signature".getBytes(StandardCharsets.UTF_8);
  private static final int STATUS_CODE_200 = 200;

  private Patient createTestPatient(String kvnr) {
    final String givenName = "John";
    final String surName = "Doe";
    final String street = "Street";
    final String houseNumber = "123";
    final String postalCode = "123456";
    final String city = "City";
    final String birthday = "14.06.1980";
    return new Patient(givenName, surName, kvnr, street, houseNumber, postalCode, city, birthday);
  }

  private Map<String, String> createContentMap() {
    Map<String, String> contentMap = new HashMap<>();
    contentMap.put("info_text", null);
    contentMap.put("pickUpCodeHR", "");
    contentMap.put("pickUpCodeDMC", "");
    return contentMap;
  }

  @Test
  public void shouldInvokeTaskCreate() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", "draft");
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskCreate(ACCESS_TOKEN_PRESCRIBING_ENTITY)).thenReturn(taskCreateData);

    TestcaseData.getInstance().setAccessTokenDispensingEntity(ACCESS_TOKEN_PRESCRIBING_ENTITY);
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
  public void shouldInvokeTaskAccept() {
    byte[] signedPrescription = "ich bin ein signiertes Rezept".getBytes(StandardCharsets.UTF_8);
    SignDocumentResult signDocumentResult = new SignDocumentResult("", "", "", signedPrescription);

    TaskCreateData taskCreateData =
        new TaskCreateData("testTaskId", "testPrescriptionId", "testAccessCode");

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setSignDocumentResult(signDocumentResult);
    testcaseData.setTaskCreateData(taskCreateData);

    TaskAcceptData expectedTaskAcceptData = new TaskAcceptData(201, STATUS_READY);
    expectedTaskAcceptData.setTaskID("testTaskId");
    expectedTaskAcceptData.setSecret("secret");
    expectedTaskAcceptData.setSignedPrescription(signedPrescription);
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskAccept(eq("testTaskId"), any(), eq("testAccessCode")))
        .thenReturn(expectedTaskAcceptData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskAccept();

    TaskAcceptData actual = testcaseData.getTaskAcceptData();
    Assert.assertEquals(expectedTaskAcceptData, actual);
  }

  @Test
  public void shouldInvokeTaskAbortByDoctor() throws MissingPreconditionException {

    TaskCreateData taskCreateData =
        new TaskCreateData("testTaskId", "testPrescriptionId", "testAccessCode");

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setAccessTokenDispensingEntity("AccessTokenDispensingEntity");
    taskCreateData.setTaskId("testTaskId");
    taskCreateData.setAccessCode("AccessCode");

    TaskRejectData expectedTaskRejectData = new TaskRejectData(777);

    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskAbortAsDoctor(
            "AccessTokenDispensingEntity", "testTaskId", "AccessCode"))
        .thenReturn(expectedTaskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskAbortByDoctor();

    TaskRejectData actual = testcaseData.getTaskDeleteData();
    Assert.assertEquals(expectedTaskRejectData, actual);
  }

  @Test
  public void shouldInvokeTaskAbortByPharmacist() throws MissingPreconditionException {

    TaskCreateData taskCreateData =
        new TaskCreateData("testTaskId", "testPrescriptionId", "testAccessCode");

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setAccessTokenDispensingEntity("AccessTokenDispensingEntity");
    taskCreateData.setTaskId("testTaskId");
    taskCreateData.setAccessCode("AccessCode");
    byte[] signedPrescription = "ich bin ein signiertes Rezept".getBytes(StandardCharsets.UTF_8);
    TaskAcceptData dummy = new TaskAcceptData(201, STATUS_READY);
    dummy.setTaskID("testTaskId");
    dummy.setSecret("secret");
    dummy.setSignedPrescription(signedPrescription);
    testcaseData.setTaskAcceptData(dummy);
    TaskRejectData expectedTaskRejectData = new TaskRejectData(777);
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskAbortAsPharmacist(
            "AccessTokenDispensingEntity", "testTaskId", "secret"))
        .thenReturn(expectedTaskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskAbortByPharmacist();
    TaskRejectData actual = testcaseData.getTaskDeleteData();
    Assert.assertEquals(expectedTaskRejectData, actual);
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldInvokeTaskAbortByPharmacistWillFailOnMissingTaskAcceptData()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setTaskAcceptData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.invokeTaskAbortByPharmacist();
  }

  @Test
  public void shouldCheckAbortResponseNoContent() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(204);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNoContent());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckAbortResponseNoContentFailOnMissingData()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskDeleteData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNoContent());
  }

  @Test
  public void shouldCheckAbortResponseNoContentFailOnInvalidStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(777);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkAbortResponseNoContent());
  }

  @Test
  public void shouldInvokeTaskActivate() {
    byte[] signedPrescription = "ich bin ein signiertes Rezept".getBytes(StandardCharsets.UTF_8);
    SignDocumentResult signDocumentResult = new SignDocumentResult("", "", "", signedPrescription);

    TaskCreateData taskCreateData =
        new TaskCreateData("testTaskId", "testPrescriptionId", "testAccessCode");

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setSignDocumentResult(signDocumentResult);
    testcaseData.setTaskCreateData(taskCreateData);

    TaskActivateData expectedTaskActivateData = new TaskActivateData(200, STATUS_READY);

    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskActivate(
            eq("testTaskId"), any(), eq("testAccessCode"), eq(signedPrescription)))
        .thenReturn(expectedTaskActivateData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
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
  public void shouldInvokeTaskClose() {
    MedicationData medicationData =
        new MedicationData("testKvnr", "testPznValue", "testPznText", "testPrescriptionId");
    TaskCloseData taskCloseData = new TaskCloseData(STATUS_CODE_200, SIGNATURE);

    TaskCreateData taskCreateData =
        new TaskCreateData("testTaskId", "testPrescriptionId", "testAccessCode");
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_200, STATUS_READY);
    taskAcceptData.setSecret("testSecret");

    Patient patient = mock(Patient.class);
    when(patient.getKvnr()).thenReturn("testKvnr");

    Medication medication = mock(Medication.class);
    when(medication.getPznValue()).thenReturn("testPznValue");
    when(medication.getPznText()).thenReturn("testPznText");

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setPatient(patient);
    testcaseData.setMedication(medication);
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);

    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskClose(eq("testTaskId"), any(), eq("testSecret"), eq(medicationData)))
        .thenReturn(taskCloseData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskClose();

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

  @Test
  public void shouldInvokeCommunicationGetAsPharmacist() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setAccessTokenDispensingEntity("my-test-access-token");

    CommunicationGetData communicationGetData = new CommunicationGetData(200);
    var dummyMessage = new CommunicationData();
    dummyMessage.setTaskReference(
        "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationGetData.setCommunications(List.of(dummyMessage));

    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeCommunicationGet(eq("my-test-access-token")))
        .thenReturn(communicationGetData);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeCommunicationGetAsPharmacist();

    CommunicationGetData communicationGetDataAfterExecution =
        TestcaseData.getInstance().getCommunicationGetData();
    Assert.assertEquals(communicationGetData, communicationGetDataAfterExecution);
  }

  @Test
  public void shouldCheckGetCommunicationOkInfoReq() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_INFO_REQ);
    communicationData.setMessage("Sample message");
    communications.add(communicationData);

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertTrue(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkInfoReqFailOnMultipleMessages()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_INFO_REQ);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkInfoReqFailOnMessageTypeErxCommunicationReply()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckGetCommunicationOkInfoReqFailOnMissingCommunicationGetData()
      throws MissingPreconditionException {
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setCommunicationGetData(null);

    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkInfoReqFailOnWrongStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = new CommunicationGetData(418);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkInfoReq());
  }

  @Test
  public void shouldDetermineCommunication() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);

    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender("kvnrRecipient");
    communicationData.setMessage("messageText");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));

    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    fdClientGlueCode.determineCommunication("kvnrRecipient", "messageText");
    Assert.assertEquals(
        "Message is invalid", "messageText", testcaseData.getCommunicationSetData().getMessage());
    Assert.assertEquals(
        "Sender is invalid", "kvnrRecipient", testcaseData.getCommunicationSetData().getSender());
    Assert.assertEquals(
        "Type is invalid",
        CommunicationType.ERX_COMMUNICATION_REPLY,
        testcaseData.getCommunicationSetData().getType());
  }

  @Test
  public void shouldSendCommunication() throws MissingPreconditionException {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(accessToken);
    Communication communication = mock(CommunicationInfoReq.class);
    when(communication.getTaskReference())
        .thenReturn(
            "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    when(communication.getMessage()).thenReturn("Message1");
    Value value1 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    when(communication.getSender()).thenReturn(value1);

    List<Communication> communications = List.of(communication);

    BundleCommunication bundleCommunication = mock(BundleCommunication.class);
    when(bundleCommunication.getCommunications()).thenReturn(communications);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(201);
    when(responseEnvelope.getBundleCommunication()).thenReturn(Optional.of(bundleCommunication));

    CommunicationActor communicationActor = mock(CommunicationActor.class);
    Map<String, String> contentMap = createContentMap();
    contentMap.replace("info_text", communication.getMessage());
    when(communicationActor.sendReplyMessage(
            "/Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            value1,
            contentMap))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setCommunicationActor(accessToken, communicationActor);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender(value1.getValue());
    communicationData.setMessage("Message1");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));

    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);
    fdClientGlueCode.determineCommunication("G995030566", "Message1");
    fdClientGlueCode.sendCommunication();
    Assert.assertNotNull(TestcaseData.getInstance().getCommunicationGetData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldSendCommunicationFailOnMissingAccessToken()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(null);
    Value value1 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender(value1.getValue());
    communicationData.setMessage("Message1");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));
    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    fdClientGlueCode.determineCommunication("G995030566", "Message1");
    fdClientGlueCode.sendCommunication();
    Assert.assertNotNull(TestcaseData.getInstance().getCommunicationGetData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldSendCommunicationFailOnMissingCommunicationData()
      throws MissingPreconditionException {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(accessToken);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode();
    fdClientGlueCode.sendCommunication();
    Assert.assertNotNull(TestcaseData.getInstance().getCommunicationGetData());
  }

  @Test
  public void checkPostCommunicationOkReply() throws MissingPreconditionException {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(accessToken);
    Communication communication = mock(CommunicationInfoReq.class);
    when(communication.getTaskReference())
        .thenReturn(
            "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    when(communication.getMessage()).thenReturn("Message1");
    Value value1 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    when(communication.getSender()).thenReturn(value1);

    List<Communication> communications = List.of(communication);

    BundleCommunication bundleCommunication = mock(BundleCommunication.class);
    when(bundleCommunication.getCommunications()).thenReturn(communications);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(201);
    when(responseEnvelope.getBundleCommunication()).thenReturn(Optional.of(bundleCommunication));

    CommunicationActor communicationActor = mock(CommunicationActor.class);
    Map<String, String> contentMap = createContentMap();
    contentMap.replace("info_text", communication.getMessage());
    when(communicationActor.sendReplyMessage(
            "/Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            value1,
            contentMap))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setCommunicationActor(accessToken, communicationActor);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender(value1.getValue());
    communicationData.setMessage("Message1");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));

    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    fdClientGlueCode.determineCommunication("G995030566", "Message1");
    fdClientGlueCode.sendCommunication();
    Assert.assertNotNull(
        "getCommunicationGetData is null", TestcaseData.getInstance().getCommunicationGetData());
    Assert.assertTrue(fdClientGlueCode.checkPostCommunicationOkReply());
  }

  @Test
  public void checkPostCommunicationOkReplyShouldFailOnWrongReturnCode()
      throws MissingPreconditionException {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(accessToken);
    Communication communication = mock(CommunicationInfoReq.class);
    when(communication.getTaskReference())
        .thenReturn(
            "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    when(communication.getMessage()).thenReturn("Message1");
    Value value1 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    when(communication.getSender()).thenReturn(value1);

    List<Communication> communications = List.of(communication);

    BundleCommunication bundleCommunication = mock(BundleCommunication.class);
    when(bundleCommunication.getCommunications()).thenReturn(communications);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(418);
    when(responseEnvelope.getBundleCommunication()).thenReturn(Optional.of(bundleCommunication));

    CommunicationActor communicationActor = mock(CommunicationActor.class);
    Map<String, String> contentMap = createContentMap();
    contentMap.replace("info_text", communication.getMessage());
    when(communicationActor.sendReplyMessage(
            "/Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            value1,
            contentMap))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setCommunicationActor(accessToken, communicationActor);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);

    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender(value1.getValue());
    communicationData.setMessage("Message1");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));
    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    fdClientGlueCode.determineCommunication("G995030566", "Message1");
    fdClientGlueCode.sendCommunication();
    Assert.assertNotNull(
        "getCommunicationGetData is null", TestcaseData.getInstance().getCommunicationGetData());
    Assert.assertFalse(fdClientGlueCode.checkPostCommunicationOkReply());
  }

  @Test
  public void shouldCheckGetCommunicationOkDispReq() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_DISP_REQ);
    communicationData.setMessage("Sample message");
    communications.add(communicationData);

    // exemplarische Task-Referenz: Inhalt wird bislang nicht geprüft
    testcaseData.setDispReqTaskReference(
        "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertTrue(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkDispReqFailOnMultipleMessages()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_DISP_REQ);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    // exemplarische Task-Referenz: Inhalt wird bislang nicht geprüft
    testcaseData.setDispReqTaskReference(
        "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");

    communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkDispReqFailOnMessageTypeErxCommunicationReply()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    CommunicationGetData communicationGetData = new CommunicationGetData(200);

    List<CommunicationData> communications = new ArrayList<>();
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage("Reply message");
    communications.add(communicationData);

    // exemplarische Task-Referenz: Inhalt wird bislang nicht geprüft
    testcaseData.setDispReqTaskReference(
        "Task/4711/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");

    communicationGetData.setCommunications(communications);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckGetCommunicationOkDispReqFailOnMissingCommunicationGetData()
      throws MissingPreconditionException {
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setCommunicationGetData(null);

    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  @Test
  public void shouldCheckGetCommunicationOkDispReqFailOnWrongStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    CommunicationGetData communicationGetData = new CommunicationGetData(418);
    testcaseData.setCommunicationGetData(communicationGetData);
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Assert.assertFalse(fdClientGlueCode.checkGetCommunicationOkDispReq());
  }

  @Test
  public void shouldInvokeTaskReject() throws MissingPreconditionException {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setTaskID("4711");
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    TestcaseData.getInstance().setAccessTokenDispensingEntity("token");
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskReject("4711", "token", "secret")).thenReturn(new TaskRejectData(204));

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertNotNull(TestcaseData.getInstance().getTaskRejectData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldInvokeTaskRejectFailOnMissingTaskId() throws MissingPreconditionException {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setTaskID(null);
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    TestcaseData.getInstance().setAccessTokenDispensingEntity("token");

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertNotNull(TestcaseData.getInstance().getTaskRejectData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldInvokeTaskRejectFailOnMissingTask() throws MissingPreconditionException {

    TestcaseData.getInstance().setTaskAcceptData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertNotNull(TestcaseData.getInstance().getTaskRejectData());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldInvokeTaskRejectFailOnMissingToken() throws MissingPreconditionException {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setTaskID("4711");
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    TestcaseData.getInstance().setAccessTokenDispensingEntity(null);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertNull(TestcaseData.getInstance().getTaskRejectData());
  }

  @Test
  public void checkRejectResponseNoContent() throws MissingPreconditionException {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setTaskID("4711");
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    TestcaseData.getInstance().setAccessTokenDispensingEntity("token");
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskReject("4711", "token", "secret")).thenReturn(new TaskRejectData(204));
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertTrue(fdClientGlueCode.checkRejectResponseNoContent());
  }

  @Test
  public void checkRejectResponseNoContentFailWithInvalidResponseCode()
      throws MissingPreconditionException {
    TaskAcceptData taskActivateData = new TaskAcceptData(200, STATUS_IN_PROGRESS);
    taskActivateData.setSecret("secret");
    taskActivateData.setTaskID("4711");
    TestcaseData.getInstance().setTaskAcceptData(taskActivateData);
    TestcaseData.getInstance().setAccessTokenDispensingEntity("token");
    FdClient fdClient = mock(FdClient.class);
    when(fdClient.invokeTaskReject("4711", "token", "secret")).thenReturn(new TaskRejectData(418));
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    fdClientGlueCode.invokeTaskReject();
    Assert.assertFalse(fdClientGlueCode.checkRejectResponseNoContent());
  }

  @Test
  public void shouldHaveSignedPrescriptionAfterClose() throws MissingPreconditionException {
    // create reusable constants
    final String taskId = "taskID";
    final String kvnr = "kvnr";
    final String prescriptionId = "prescriptionID";
    final String accessCode = "accessCode";
    final String accessToken = "accessToken";
    final String secret = "secret";
    final String pznValue = "pznValue";
    final String pznText = "pznText";

    final TestcaseData testcaseData = TestcaseData.getInstance();

    final Medication medication = new Medication(pznValue, pznText);
    final TaskCreateData taskCreateData =
        new TaskCreateData(201, taskId, prescriptionId, accessCode, STATUS_READY);
    final TaskAcceptData taskAcceptData = new TaskAcceptData(201, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret(secret);

    // precondition: pretend Task/$create and Task/$accept have been performed
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);
    testcaseData.setPatient(createTestPatient(kvnr));
    testcaseData.setMedication(medication);
    testcaseData.setAccessTokenDispensingEntity(accessToken);

    // mock the FdClient
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    SignedReceipt signedReceipt = new SignedReceipt(prescriptionId);
    TaskCloseData taskCloseData = new TaskCloseData(201, new byte[] {});
    taskCloseData.setSignedPrescription(signedReceipt);
    FdClient mockFdClient = mock(FdClient.class);
    when(mockFdClient.invokeTaskClose(taskId, accessToken, secret, medicationData))
        .thenReturn(taskCloseData);

    // test interaction between invokeTaskClose() and checkSignedPrescriptionAvailableAfterClose()
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(mockFdClient);
    fdClientGlueCode.invokeTaskClose();
    fdClientGlueCode.checkSignedPrescriptionAvailableAfterClose();
  }

  @Test
  public void getDispensedMedicationReceipt() throws MissingPreconditionException {
    // create reusable constants
    final String taskId = "taskID";
    final String kvnr = "kvnr";
    final String prescriptionId = "prescriptionID";
    final String accessCode = "accessCode";
    final String accessToken = "accessToken";
    final String secret = "secret";
    final String pznValue = "pznValue";
    final String pznText = "pznText";

    final TestcaseData testcaseData = TestcaseData.getInstance();

    final Medication medication = new Medication(pznValue, pznText);
    final TaskCreateData taskCreateData =
        new TaskCreateData(201, taskId, prescriptionId, accessCode, STATUS_READY);
    final TaskAcceptData taskAcceptData = new TaskAcceptData(201, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret(secret);

    // precondition: pretend Task/$create and Task/$accept have been performed
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);
    testcaseData.setPatient(createTestPatient(kvnr));
    testcaseData.setMedication(medication);
    testcaseData.setAccessTokenDispensingEntity(accessToken);

    // mock the FdClient
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    SignedReceipt signedReceipt = new SignedReceipt(201, prescriptionId);
    TaskCloseData taskCloseData = new TaskCloseData(201, new byte[] {});
    taskCloseData.setSignedPrescription(signedReceipt);
    FdClient mockFdClient = mock(FdClient.class);
    when(mockFdClient.invokeTaskClose(taskId, accessToken, secret, medicationData))
        .thenReturn(taskCloseData);
    when(mockFdClient.invokeDispensedMedicationReceipt(accessToken, prescriptionId, secret))
        .thenReturn(signedReceipt);

    // still precondition: dispense the medication via Task/$close
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(mockFdClient);
    fdClientGlueCode.invokeTaskClose();

    // test execution
    fdClientGlueCode.getDispensedMedicationReceipt();
    Assert.assertNotNull(testcaseData.getDispensedMedicationReceipt());
  }

  @Test
  public void shouldFailWithWrongStatusCodeCheckSignedReceiptStatus()
      throws MissingPreconditionException {
    TestcaseData.getInstance().setDispensedMedicationReceipt(null);
    final String taskId = "taskID";
    final String kvnr = "kvnr";
    final String prescriptionId = "prescriptionID";
    final String accessCode = "accessCode";
    final String accessToken = "accessToken";
    final String secret = "secret";
    final String pznValue = "pznValue";
    final String pznText = "pznText";

    final TestcaseData testcaseData = TestcaseData.getInstance();

    final Medication medication = new Medication(pznValue, pznText);
    final TaskCreateData taskCreateData =
        new TaskCreateData(201, taskId, prescriptionId, accessCode, STATUS_READY);
    final TaskAcceptData taskAcceptData = new TaskAcceptData(201, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret(secret);

    // precondition: pretend Task/$create and Task/$accept have been performed
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);
    testcaseData.setPatient(createTestPatient(kvnr));
    testcaseData.setMedication(medication);
    testcaseData.setAccessTokenDispensingEntity(accessToken);

    // mock the FdClient
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    SignedReceipt signedReceipt = new SignedReceipt(200, prescriptionId);
    TaskCloseData taskCloseData = new TaskCloseData(201, new byte[] {});
    taskCloseData.setSignedPrescription(signedReceipt);
    FdClient mockFdClient = mock(FdClient.class);
    when(mockFdClient.invokeTaskClose(taskId, accessToken, secret, medicationData))
        .thenReturn(taskCloseData);
    when(mockFdClient.invokeDispensedMedicationReceipt(accessToken, prescriptionId, secret))
        .thenReturn(signedReceipt);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(mockFdClient);
    fdClientGlueCode.invokeTaskClose();
    fdClientGlueCode.getDispensedMedicationReceipt();
    // test execution
    Assert.assertTrue(fdClientGlueCode.checkSignedReceiptStatus());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldFailCheckSignedReceiptStatus() throws MissingPreconditionException {
    TestcaseData.getInstance().setDispensedMedicationReceipt(null);
    final String taskId = "taskID";
    final String kvnr = "kvnr";
    final String prescriptionId = "prescriptionID";
    final String accessCode = "accessCode";
    final String accessToken = "accessToken";
    final String secret = "secret";
    final String pznValue = "pznValue";
    final String pznText = "pznText";

    final TestcaseData testcaseData = TestcaseData.getInstance();

    final Medication medication = new Medication(pznValue, pznText);
    final TaskCreateData taskCreateData =
        new TaskCreateData(201, taskId, prescriptionId, accessCode, STATUS_READY);
    final TaskAcceptData taskAcceptData = new TaskAcceptData(201, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret(secret);

    // precondition: pretend Task/$create and Task/$accept have been performed
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);
    testcaseData.setPatient(createTestPatient(kvnr));
    testcaseData.setMedication(medication);
    testcaseData.setAccessTokenDispensingEntity(accessToken);

    // mock the FdClient
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    SignedReceipt signedReceipt = new SignedReceipt(200, prescriptionId);
    TaskCloseData taskCloseData = new TaskCloseData(201, new byte[] {});
    taskCloseData.setSignedPrescription(signedReceipt);
    FdClient mockFdClient = mock(FdClient.class);
    when(mockFdClient.invokeTaskClose(taskId, accessToken, secret, medicationData))
        .thenReturn(taskCloseData);
    when(mockFdClient.invokeDispensedMedicationReceipt(accessToken, prescriptionId, secret))
        .thenReturn(signedReceipt);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(mockFdClient);
    // no task will closed!
    fdClientGlueCode.getDispensedMedicationReceipt();
    // test execution
    Assert.assertTrue(fdClientGlueCode.checkSignedReceiptStatus());
  }

  @Test
  public void shouldCheckSignedReceiptStatusIsTrue() throws MissingPreconditionException {
    TestcaseData.getInstance().setDispensedMedicationReceipt(null);
    final String taskId = "taskID";
    final String kvnr = "kvnr";
    final String prescriptionId = "prescriptionID";
    final String accessCode = "accessCode";
    final String accessToken = "accessToken";
    final String secret = "secret";
    final String pznValue = "pznValue";
    final String pznText = "pznText";

    final TestcaseData testcaseData = TestcaseData.getInstance();

    final Medication medication = new Medication(pznValue, pznText);
    final TaskCreateData taskCreateData =
        new TaskCreateData(201, taskId, prescriptionId, accessCode, STATUS_READY);
    final TaskAcceptData taskAcceptData = new TaskAcceptData(201, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret(secret);

    // precondition: pretend Task/$create and Task/$accept have been performed
    testcaseData.setTaskCreateData(taskCreateData);
    testcaseData.setTaskAcceptData(taskAcceptData);
    testcaseData.setPatient(createTestPatient(kvnr));
    testcaseData.setMedication(medication);
    testcaseData.setAccessTokenDispensingEntity(accessToken);

    // mock the FdClient
    MedicationData medicationData = new MedicationData(kvnr, pznValue, pznText, prescriptionId);
    SignedReceipt signedReceipt = new SignedReceipt(404, prescriptionId);
    TaskCloseData taskCloseData = new TaskCloseData(201, new byte[] {});
    taskCloseData.setSignedPrescription(signedReceipt);
    FdClient mockFdClient = mock(FdClient.class);
    when(mockFdClient.invokeTaskClose(taskId, accessToken, secret, medicationData))
        .thenReturn(taskCloseData);
    when(mockFdClient.invokeDispensedMedicationReceipt(accessToken, prescriptionId, secret))
        .thenReturn(signedReceipt);

    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(mockFdClient);
    fdClientGlueCode.invokeTaskClose();
    fdClientGlueCode.getDispensedMedicationReceipt();
    // test execution
    Assert.assertFalse(fdClientGlueCode.checkSignedReceiptStatus());
  }

  @Test
  public void shouldDetermineCommunicationEx() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    FdClient fdClient = mock(FdClient.class);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(fdClient);
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("info_text", "messageText");
    expectedMap.put("pickUpCodeHR", "12344321");
    expectedMap.put("pickUpCodeDMC", "0123456789abcdefggfedcba9876543210");

    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender("kvnrRecipient");
    communicationData.setMessage("messageText");
    CommunicationGetData communicationGetData = new CommunicationGetData(201);
    communicationGetData.setCommunications(List.of(communicationData));

    TestcaseData.getInstance().setCommunicationGetData(communicationGetData);

    fdClientGlueCode.determineCommunicationEx(
        "kvnrRecipient", "messageText", "12344321", "0123456789abcdefggfedcba9876543210");
    Assert.assertEquals(
        "Message is invalid", "messageText", testcaseData.getCommunicationSetData().getMessage());
    Assert.assertEquals(
        "Sender is invalid", "kvnrRecipient", testcaseData.getCommunicationSetData().getSender());
    Assert.assertEquals(
        "Type is invalid",
        CommunicationType.ERX_COMMUNICATION_REPLY,
        testcaseData.getCommunicationSetData().getType());
    Assert.assertTrue(
        TestUtils.areEqual(expectedMap, testcaseData.getCommunicationSetData().getReplayContent()));
  }

  @Test
  public void shouldCheckAbortResponseNotFound() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(404);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNotFound());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckAbortResponseNotFoundFailOnMissingData()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskDeleteData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseNotFound());
  }

  @Test
  public void shouldCheckAbortResponseNotFoundFailOnInvalidStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(777);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkAbortResponseNotFound());
  }

  @Test
  public void shouldCheckAbortResponseConflict() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(409);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseConflict());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckAbortResponseConflictFailOnMissingData()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskDeleteData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkAbortResponseConflict());
  }

  @Test
  public void shouldCheckAbortResponseConflictFailOnInvalidStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskRejectData taskRejectData = new TaskRejectData(777);
    testcaseData.setTaskDeleteData(taskRejectData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkAbortResponseConflict());
  }

  @Test
  public void shouldCheckTaskCloseStatusConflict() throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskCloseData taskCloseData = new TaskCloseData(409, new byte[] {1, 2, 3});
    testcaseData.setTaskCloseData(taskCloseData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseStatusConflict());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldCheckTaskCloseStatusConflictFailOnMissingData()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();

    testcaseData.setTaskCloseData(null);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertTrue(fdClientGlueCode.checkTaskCloseStatusConflict());
  }

  @Test
  public void shouldCheckTaskCloseStatusConflictFailOnInvalidStatusCode()
      throws MissingPreconditionException {
    TestcaseData testcaseData = TestcaseData.getInstance();
    TaskCloseData taskCloseData = new TaskCloseData(200, new byte[] {1, 2, 3});
    testcaseData.setTaskCloseData(taskCloseData);
    FdClientGlueCode fdClientGlueCode = new FdClientGlueCode(null);
    Assert.assertFalse(fdClientGlueCode.checkTaskCloseStatusConflict());
  }
}
