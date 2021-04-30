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

package de.gematik.rezeps.fdclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.dataexchange.create.MedicationData;
import de.gematik.rezeps.dataexchange.create.TaskAcceptData;
import de.gematik.rezeps.dataexchange.create.TaskActivateData;
import de.gematik.rezeps.dataexchange.create.TaskCloseData;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import de.gematik.test.erezept.fd.actors.DoctorActor;
import de.gematik.test.erezept.fd.actors.PharmacistActor;
import de.gematik.test.erezept.fd.actors.response.ResponseEnvelope;
import de.gematik.test.erezept.fd.fhir.adapter.KBV_Medication_PZN;
import de.gematik.test.erezept.fd.fhir.adapter.Receipt;
import de.gematik.test.erezept.fd.fhir.adapter.Receipt.ErxSignature;
import de.gematik.test.erezept.fd.fhir.adapter.Task;
import de.gematik.test.erezept.fd.fhir.adapter.TaskAcceptBundle;
import de.gematik.test.erezept.fd.fhir.adapter.Value;
import de.gematik.test.logger.TestNGLogManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Optional;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class FdClientImplTest {

  static {
    TestNGLogManager.useTestNGLogger = false;
  }

  private static final String PATH_TO_EXAMPLE_TASK_CREATE_RESPONSE = "src/test/resources/task_create_response.xml";
  private static final String PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE = "src/test/resources/task_activate_doctor_response.xml";
  private static final String PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE = "src/test/resources/task_activate_pharmacist_response.xml";
  private static final byte[] SIGNED_PRESCRIPTION = "ich bin ein signierter Verordnungsdatensatz"
      .getBytes(
          StandardCharsets.UTF_8);

  @Test
  public void shouldPerformCreate() throws IOException {
    DoctorActor doctorActor = mock(DoctorActor.class);
    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    Task task = new Task(readExampleTaskCreateResponse(), false);
    when(responseEnvelope.getStatusCode()).thenReturn(201);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));
    when(doctorActor.createPrescription()).thenReturn(responseEnvelope);

    FdClientImpl fdClient = new FdClientImpl(doctorActor, null);
    TaskCreateData taskCreateData = fdClient
        .invokeTaskCreate("not relevant");
    TaskCreateData expectedTaskCreateData = new TaskCreateData(201, "4711",
        "160.123.456.789.123.58",
        "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea", "draft");

    Assert.assertEquals(expectedTaskCreateData, taskCreateData);
  }

  private String readExampleTaskCreateResponse() throws IOException {
    return new String(Files.readAllBytes(Paths.get(PATH_TO_EXAMPLE_TASK_CREATE_RESPONSE)),
        StandardCharsets.UTF_8);
  }

  @Test
  public void shouldPerformActivate() throws IOException {
    DoctorActor doctorActor = mock(DoctorActor.class);
    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    Task task = new Task(
        readExampleTaskActivateResponse(PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE), false);
    when(responseEnvelope.getStatusCode()).thenReturn(200);
    when(responseEnvelope.getTask()).thenReturn(Optional.of(task));
    when(doctorActor.issuePrescription(any(), eq(SIGNED_PRESCRIPTION)))
        .thenReturn(responseEnvelope);

    Task taskAfterCreate = new Task(readExampleTaskCreateResponse(), false);
    FdClientImpl fdClient = new FdClientImpl(doctorActor, taskAfterCreate);
    TaskActivateData taskActivateData = fdClient.invokeTaskActivat(SIGNED_PRESCRIPTION);
    TaskActivateData expectedTaskActivateData = new TaskActivateData(200, "ready");

    Assert.assertEquals(expectedTaskActivateData, taskActivateData);
  }

  private String readExampleTaskActivateResponse(String pathToActivateResponse) throws IOException {
    return new String(Files.readAllBytes(Paths.get(pathToActivateResponse)),
        StandardCharsets.UTF_8);
  }

  @Test
  public void shouldPerformAccept() throws IOException {
    PharmacistActor pharmacistActor = mock(PharmacistActor.class);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);

    String taskAcceptBundlePharmacistAsXml = readExampleTaskActivateResponse(
        PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE);
    Optional<TaskAcceptBundle> taskAcceptBundle = Optional
        .of(new TaskAcceptBundle(taskAcceptBundlePharmacistAsXml, false));
    when(responseEnvelope.getTaskAcceptBundle()).thenReturn(taskAcceptBundle);

    String taskDoctorAsXml = readExampleTaskActivateResponse(
        PATH_TO_EXAMPLE_TASK_ACTIVATE_DOCTOR_RESPONSE);
    Task taskDoctor = new Task(taskDoctorAsXml, false);

    when(pharmacistActor.acceptPrescription(taskDoctor)).thenReturn(responseEnvelope);
    FdClientImpl fdClient = new FdClientImpl(pharmacistActor, taskDoctor);

    TaskAcceptData taskAcceptData = fdClient.invokeTaskAccept("accessToken");
    TaskAcceptData expectedTaskAcceptData = new TaskAcceptData(200, "in-progress");
    expectedTaskAcceptData
        .setSecret("c36ca26502892b371d252c99b496e31505ff449aca9bc69e231c58148f6233cf");
    expectedTaskAcceptData
        .setSignedPrescription(Base64.decode("SWNoIGJpbiBlaW4gQmFzZTY0LVN0cmluZw=="));

    Assert.assertEquals(expectedTaskAcceptData, taskAcceptData);
  }

  @Test
  public void shouldInvokeTaskClose() throws IOException {
    PharmacistActor pharmacistActor = mock(PharmacistActor.class);

    ResponseEnvelope responseEnvelope = mock(ResponseEnvelope.class);
    when(responseEnvelope.getStatusCode()).thenReturn(200);

    ErxSignature erxSignature = mock(ErxSignature.class);
    when(erxSignature.getData()).thenReturn(SIGNED_PRESCRIPTION);
    Receipt receipt = mock(Receipt.class);
    when(receipt.getSignature()).thenReturn(erxSignature);
    when(responseEnvelope.getReceipt()).thenReturn(Optional.of(receipt));


    String taskAcceptBundlePharmacistAsXml = readExampleTaskActivateResponse(
        PATH_TO_EXAMPLE_TASK_ACTIVATE_PHARMACIST_RESPONSE);
    TaskAcceptBundle taskAcceptBundle = new TaskAcceptBundle(taskAcceptBundlePharmacistAsXml, false);

    FdClientImpl fdClient = new FdClientImpl(pharmacistActor, taskAcceptBundle);

    // die equals-Methode von KBV_Medication_PZN erlaubt nicht, dass wir den Aufruf mit einem
    // spezifischen Objekt mit initialisierten Werten prüfen
    when(pharmacistActor.dispenseMedication(eq(taskAcceptBundle), any()))
        .thenReturn(responseEnvelope);

    MedicationData medicationData = new MedicationData("pznValue", "pznText");
    TaskCloseData taskCloseData = fdClient
        .invokeTaskClose(medicationData);

    TaskCloseData expectedTaskCloseData = new TaskCloseData(200, SIGNED_PRESCRIPTION);
    Assert.assertEquals(expectedTaskCloseData, taskCloseData);
  }


}
