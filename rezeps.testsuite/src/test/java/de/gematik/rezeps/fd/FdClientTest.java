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

package de.gematik.rezeps.fd;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.dataexchange.*;
import de.gematik.rezeps.gluecode.TestcaseData;
import de.gematik.test.erezept.fd.actors.CommunicationActor;
import de.gematik.test.erezept.fd.actors.DoctorActor;
import de.gematik.test.erezept.fd.actors.PharmacistActor;
import de.gematik.test.erezept.fd.actors.response.ResponseEnvelope;
import de.gematik.test.erezept.fd.fhir.adapter.*;
import de.gematik.test.erezept.fd.fhir.adapter.Receipt.ErxSignature;
import de.gematik.test.erezept.fd.fhir.helper.Profile;
import de.gematik.test.logger.TestNGLogManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Test;

public class FdClientTest {

  static {
    TestNGLogManager.useTestNGLogger = false;
  }

  private static final String PATH_TO_EXAMPLE_TASK_CREATE_RESPONSE =
      "src/test/resources/task_create_response.xml";
  private static final String PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE =
      "src/test/resources/task_activate_doctor_response.xml";
  private static final String PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE =
      "src/test/resources/task_activate_pharmacist_response.xml";
  private static final byte[] SIGNED_PRESCRIPTION =
      "ich bin ein signierter Verordnungsdatensatz".getBytes(StandardCharsets.UTF_8);

  @Test
  public void shouldPerformCreate() throws IOException {
    Task task = new Task(readExampleTaskCreateResponse(), false);
    String accessToken = "not relevant";

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(201);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));

    DoctorActor doctorActor = mock(DoctorActor.class);
    when(doctorActor.createPrescription()).thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setDoctorActor(accessToken, doctorActor);

    TaskCreateData taskCreateData = fdClient.invokeTaskCreate(accessToken);
    TaskCreateData expectedTaskCreateData =
        new TaskCreateData(
            201,
            "4711",
            "160.123.456.789.123.58",
            "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            "draft");

    Assert.assertEquals(expectedTaskCreateData, taskCreateData);
  }

  private String readExampleTaskCreateResponse() throws IOException {
    return new String(
        Files.readAllBytes(Paths.get(PATH_TO_EXAMPLE_TASK_CREATE_RESPONSE)),
        StandardCharsets.UTF_8);
  }

  @Test
  public void shouldPerformActivate() throws IOException {
    Task task =
        new Task(
            readExampleTaskActivateResponse(PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE), false);
    Task taskAfterCreate = new Task(readExampleTaskCreateResponse(), false);
    String taskId = taskAfterCreate.getId();
    String accessToken = "not relevant";
    String accessCode = taskAfterCreate.getAccessCode().getValue();

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));

    DoctorActor doctorActor = mock(DoctorActor.class);
    when(doctorActor.issuePrescription(eq(taskId), eq(SIGNED_PRESCRIPTION), eq(accessCode)))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setDoctorActor(accessToken, doctorActor);

    TaskActivateData taskActivateData =
        fdClient.invokeTaskActivate(taskId, "not relevant", accessCode, SIGNED_PRESCRIPTION);
    TaskActivateData expectedTaskActivateData = new TaskActivateData(200, "ready");

    Assert.assertEquals(expectedTaskActivateData, taskActivateData);
  }

  private String readExampleTaskActivateResponse(String pathToActivateResponse) throws IOException {
    return new String(
        Files.readAllBytes(Paths.get(pathToActivateResponse)), StandardCharsets.UTF_8);
  }

  @Test
  public void shouldPerformAccept() throws IOException {
    String taskAcceptBundlePharmacistAsXml =
        readExampleTaskActivateResponse(PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE);
    Optional<TaskAcceptBundle> taskAcceptBundle =
        Optional.of(new TaskAcceptBundle(taskAcceptBundlePharmacistAsXml, false));

    String taskDoctorAsXml =
        readExampleTaskActivateResponse(PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE);
    Task taskDoctor = new Task(taskDoctorAsXml, false);
    String taskId = taskDoctor.getId();
    String accessCode = taskDoctor.getAccessCode().getValue();
    String accessToken = "accessToken";

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getTaskAcceptBundle()).thenReturn(taskAcceptBundle);

    PharmacistActor pharmacistActor = mock(PharmacistActor.class);
    when(pharmacistActor.acceptPrescription(eq(taskId), eq(accessCode)))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setPharmacistActor(accessToken, pharmacistActor);

    TaskAcceptData taskAcceptData = fdClient.invokeTaskAccept(taskId, accessToken, accessCode);
    TaskAcceptData expectedTaskAcceptData = new TaskAcceptData(200, "in-progress");
    expectedTaskAcceptData.setSecret(
        "c36ca26502892b371d252c99b496e31505ff449aca9bc69e231c58148f6233cf");
    expectedTaskAcceptData.setSignedPrescription(
        Base64.decode("SWNoIGJpbiBlaW4gQmFzZTY0LVN0cmluZw=="));
    expectedTaskAcceptData.setTaskID("4711");

    Assert.assertEquals(expectedTaskAcceptData, taskAcceptData);
  }

  @Test
  public void shouldInvokeTaskClose() throws IOException {
    String taskAcceptBundlePharmacistAsXml =
        readExampleTaskActivateResponse(PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE);
    TaskAcceptBundle taskAcceptBundle =
        new TaskAcceptBundle(taskAcceptBundlePharmacistAsXml, false);
    Task task = taskAcceptBundle.getTask();
    String taskId = task.getId();
    String secret = task.getSecret().getValue();
    String kvnr = task.getFor_kvid10().getValue();
    String prescriptionId = task.getPrescriptionID().getValue();
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    MedicationData medicationData = new MedicationData(kvnr, "pznValue", "pznText", prescriptionId);

    ErxSignature erxSignature = mock(ErxSignature.class);
    when(erxSignature.getData()).thenReturn(SIGNED_PRESCRIPTION);

    Receipt receipt = mock(Receipt.class);
    when(receipt.getSignature()).thenReturn(erxSignature);
    when(receipt.getPrescriptionID()).thenReturn(task.getPrescriptionID());

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getReceipt()).thenReturn(Optional.of(receipt));

    PharmacistActor pharmacistActor = mock(PharmacistActor.class);
    when(pharmacistActor.dispenseMedication(eq(taskId), eq(secret), any()))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setPharmacistActor(accessToken, pharmacistActor);

    TaskCloseData taskCloseData =
        fdClient.invokeTaskClose(taskId, accessToken, secret, medicationData);
    TaskCloseData expectedTaskCloseData = new TaskCloseData(200, SIGNED_PRESCRIPTION);

    Assert.assertEquals(expectedTaskCloseData, taskCloseData);
  }

  @Test
  public void shouldInvokeCommunicationGet() {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";

    Communication communication0 = mock(CommunicationInfoReq.class);
    when(communication0.getTaskReference()).thenReturn("Task0");
    when(communication0.getMessage()).thenReturn("Message0");
    Value value0 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    when(communication0.getSender()).thenReturn(value0);

    Communication communication1 = mock(CommunicationInfoReq.class);
    when(communication1.getTaskReference()).thenReturn("Task1");
    when(communication1.getMessage()).thenReturn("Message1");
    Value value1 = new Value(Profile.NAMING_SYSTEM.KVID.getCanonicalUrl(), "G995030566");
    when(communication1.getSender()).thenReturn(value1);

    List<Communication> communications = List.of(communication0, communication1);

    BundleCommunication bundleCommunication = mock(BundleCommunication.class);
    when(bundleCommunication.getCommunications()).thenReturn(communications);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getBundleCommunication()).thenReturn(Optional.of(bundleCommunication));

    CommunicationActor communicationActor = mock(CommunicationActor.class);
    when(communicationActor.searchNewCommunications()).thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setCommunicationActor(accessToken, communicationActor);

    CommunicationGetData communicationGetData = fdClient.invokeCommunicationGet(accessToken);

    Assert.assertEquals(200, communicationGetData.getStatusCode());
    CommunicationData cd;
    Iterator<CommunicationData> iter = communicationGetData.getCommunications().iterator();
    Assert.assertTrue(iter.hasNext());
    cd = iter.next();
    Assert.assertEquals("Task0", cd.getTaskReference());
    Assert.assertEquals("Message0", cd.getMessage());

    Assert.assertTrue(iter.hasNext());
    cd = iter.next();
    Assert.assertEquals("Task1", cd.getTaskReference());
    Assert.assertEquals("Message1", cd.getMessage());

    Assert.assertFalse(iter.hasNext());
  }

  @Test
  public void shouldInvokeCommunicationSet() {
    String accessToken =
        "eyJhbGciOiJCUDI1NlIxIiwieDVjIjpbIk1JSUJ3ekNDQVVnQ0ZCZ2tleGhHenhkOGdxNjMwcGxsRVdQT0J4eUdNQW9HQ0NxR1NNNDlCQU1DTUVVeEN6QUpCZ05WQkFZVEFrRlZNUk13RVFZRFZRUUlEQXBUYjIxbExWTjBZWFJsTVNFd0h3WURWUVFLREJoSmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F3SGhjTk1qRXdNakUzTVRBeE1qRTNXaGNOTWpNeE1URTFNVEF4TWpFM1dqQkZNUXN3Q1FZRFZRUUdFd0pCVlRFVE1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURWhNQjhHQTFVRUNnd1lTVzUwWlhKdVpYUWdWMmxrWjJsMGN5QlFkSGtnVEhSa01IWXdFQVlIS29aSXpqMENBUVlGSzRFRUFDSURZZ0FFVkRVbXE5L0VjNVNqOG1SYkRVaGxHcDg2VFViWWRBdmpJcEZSQi9CUUpReHpES1FMTitIY2hlQ0N0THNZRzRoSHZXMFBvbmk2NWVzY0JVZE1tazRyN3NLTWx3dmtuQmxKOEo2V2w1b25lbEZJTU9NcVc1M2g3R2lybWZTUzNUQUtNQW9HQ0NxR1NNNDlCQU1DQTJrQU1HWUNNUUN0OHhYMmdlcml3WDMzRjNINjJmd0krSHdNd0dWTUV2cG1RTlA5K2lVUFJaNGV6ajhLOUF6STZSVy92bDJ2RG1RQ01RQ3BTQllacFUwSTZYTUVXT2JvMTdWSzh1anMzTHRBTm9HaUVjNWZaZDVGbGFSd1Q3MTVCNnNhNTRMZDUydFFLUEE9Il19.eyJhY3IiOiJlaWRhcy1sb2EtaGlnaCIsImF1ZCI6Imh0dHBzOi8vZXJwLnRlbGVtYXRpay5kZS9sb2dpbiIsImV4cCI6MjUyNDYwODAwMCwiZmFtaWx5X25hbWUiOiJkZXIgTmFjaG5hbWUiLCJnaXZlbl9uYW1lIjoiZGVyIFZvcm5hbWUiLCJpYXQiOjE1ODUzMzY5NTYsImlkTnVtbWVyIjoiWDIzNDU2Nzg5MSIsImlzcyI6Imh0dHBzOi8vaWRwMS50ZWxlbWF0aWsuZGUvand0IiwianRpIjoiPElEUD5fMDEyMzQ1Njc4OTAxMjM0NTY3ODkiLCJuYmYiOjE1ODUzMzY5NTYsIm5vbmNlIjoiZnV1IGJhciBiYXoiLCJvcmdhbml6YXRpb25OYW1lIjoiSW5zdGl0dXRpb25zLSBvZGVyIE9yZ2FuaXNhdGlvbnMtQmV6ZWljaG51bmciLCJwcm9mZXNzaW9uT0lEIjoiMS4yLjI3Ni4wLjc2LjQuNTQiLCJzdWIiOiJSYWJjVVN1dVdLS1pFRUhtcmNObV9rVURPVzEzdWFHVTVaazhPb0J3aU5rIn0.cAjZrFXLkDybxLmbDxDcOgXugWXifPqeFno_eSajIlXRtteFlI8DX3jxKcAVPvNPLbkFlUyYUVWLPJ4OerLC7VoLGus7TXCtrc9FxP82W7-IMDEF3tyqsrx_MvEJD9st";
    TestcaseData.getInstance().setCommunicationGetData(null);
    TestcaseData.getInstance().setCommunicationSetData(null);
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
    Map<String, String> contentMap = new HashMap<String, String>();
    contentMap.put("info_text", "Message1");
    contentMap.put("pickUpCodeHR", "");
    contentMap.put("pickUpCodeDMC", "");
    when(communicationActor.sendReplyMessage(
            "/Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            value1,
            contentMap))
        .thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setCommunicationActor(accessToken, communicationActor);

    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(
        "Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
    communicationData.setSender("G995030566");
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_REPLY);
    communicationData.setMessage("Message1");

    CommunicationGetData communicationGetData =
        fdClient.invokeCommunicationSet(accessToken, communicationData);

    Assert.assertEquals("StatusCode ungleich 201", 201, communicationGetData.getStatusCode());
  }

  @Test
  public void shouldInvokeTaskReject() throws IOException {
    Task task = new Task(readExampleTaskCreateResponse(), false);
    String accessToken = "not relevant";

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(204);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));

    PharmacistActor pharmacistActor = mock(PharmacistActor.class);
    String secret = task.getSecret().getValue();
    String taskId = task.getId();
    when(pharmacistActor.rejectPrescription(taskId, secret)).thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setPharmacistActor(accessToken, pharmacistActor);

    TaskRejectData taskRejectData = fdClient.invokeTaskReject(taskId, accessToken, secret);

    Assert.assertNotNull("Delete unsuccessfully", taskRejectData);
    Assert.assertEquals("invalid status code", 204, taskRejectData.getStatusCode());
  }

  @Test
  public void shouldInvokeTaskAbortAsPharmacist() throws IOException {
    Task task = new Task(readExampleTaskCreateResponse(), false);
    String accessToken = "not relevant";

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(204);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));

    PharmacistActor pharmacistActor = mock(PharmacistActor.class);
    String secret = task.getSecret().getValue();
    String taskId = task.getId();
    when(pharmacistActor.deletePrescription(taskId, secret)).thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setPharmacistActor(accessToken, pharmacistActor);

    TaskRejectData taskDeleteData =
        fdClient.invokeTaskAbortAsPharmacist(accessToken, taskId, secret);

    Assert.assertNotNull("Delete unsuccessfully", taskDeleteData);
    Assert.assertEquals("invalid status code", 204, taskDeleteData.getStatusCode());
    Assert.assertTrue("Status Code is not 204", taskDeleteData.isValidStatusCode(204));
  }

  @Test
  public void shouldInvokeTaskAbortAsDoctor() throws IOException {
    Task task = new Task(readExampleTaskCreateResponse(), false);
    String accessToken = "not relevant";

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));

    DoctorActor doctorActor = mock(DoctorActor.class);
    String accessCode = task.getAccessCode().getValue();
    String taskId = task.getId();
    when(doctorActor.deletePrescription(taskId, accessCode)).thenReturn(responseEnvelope);

    FdClient fdClient = new FdClient();
    fdClient.setDoctorActor(accessToken, doctorActor);

    TaskRejectData taskDeleteData =
        fdClient.invokeTaskAbortAsDoctor(accessToken, taskId, accessCode);

    Assert.assertNotNull("Delete unsuccessfully", taskDeleteData);
  }
}
