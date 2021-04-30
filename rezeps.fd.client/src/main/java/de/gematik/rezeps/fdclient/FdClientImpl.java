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

import de.gematik.rezeps.dataexchange.create.FdClient;
import de.gematik.rezeps.dataexchange.create.MedicationData;
import de.gematik.rezeps.dataexchange.create.TaskAcceptData;
import de.gematik.rezeps.dataexchange.create.TaskActivateData;
import de.gematik.rezeps.dataexchange.create.TaskCloseData;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import de.gematik.rezeps.fdclient.idp.IdpClientFactory;
import de.gematik.test.erezept.fd.actors.DoctorActor;
import de.gematik.test.erezept.fd.actors.PharmacistActor;
import de.gematik.test.erezept.fd.actors.actions.TaskLocalActions;
import de.gematik.test.erezept.fd.actors.response.ResponseEnvelope;
import de.gematik.test.erezept.fd.fhir.adapter.ErxBinary;
import de.gematik.test.erezept.fd.fhir.adapter.KBV_Bundle;
import de.gematik.test.erezept.fd.fhir.adapter.KBV_Medication_PZN;
import de.gematik.test.erezept.fd.fhir.adapter.Receipt;
import de.gematik.test.erezept.fd.fhir.adapter.Task;
import de.gematik.test.erezept.fd.fhir.adapter.TaskAcceptBundle;
import de.gematik.test.erezept.fd.fhir.adapter.Value;
import de.gematik.test.erezept.idp.I_IDPClient;
import de.gematik.test.erezept.keystore.IdentityHelper;
import java.rmi.RemoteException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FdClientImpl implements FdClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(FdClientImpl.class);

  private static final String EMPTY_STRING = "";
  private static final String TASK_STATUS_READY = "ready";
  private static final String SYSTEM_PZN = "http://fhir.de/CodeSystem/ifa/pzn";
  private static final String PROPERTY_MOCK_SIGNED_BUNDLE = "mockSignedBundle";

  private PharmacistActor pharmacistActor;
  private DoctorActor doctorActor;
  private Task task;
  private TaskAcceptBundle taskAcceptBundle;

  public FdClientImpl() {
  }

  protected FdClientImpl(DoctorActor doctorActor, Task task) {
    this.doctorActor = doctorActor;
    this.task = task;
  }


  protected FdClientImpl(PharmacistActor pharmacistActor, Task task) {
    this.pharmacistActor = pharmacistActor;
    this.task = task;
  }

  protected FdClientImpl(PharmacistActor pharmacistActor, TaskAcceptBundle taskAcceptBundle) {
    this.pharmacistActor = pharmacistActor;
    this.taskAcceptBundle = taskAcceptBundle;
  }

  @Override
  public TaskCreateData invokeTaskCreate(String accessToken) throws RemoteException {
    TaskCreateData taskCreateData = null;

    if (doctorActor == null) {
      initializeDoctorActor(accessToken);
    }

    ResponseEnvelope prescription = doctorActor
        .createPrescription();

    taskCreateData = new TaskCreateData(prescription.getStatusCode(), EMPTY_STRING, EMPTY_STRING,
        EMPTY_STRING, EMPTY_STRING);
    if (prescription.getTask().isPresent()) {
      task = prescription.getTask().get();//NOSONAR
      taskCreateData.setTaskId(task.getId());
      taskCreateData.setPrescriptionId(task.getPrescriptionID().getValue());
      taskCreateData.setAccessCode(task.getAccessCode().getValue());
      taskCreateData.setStatus(task.getStatus());
    }

    return taskCreateData;
  }

  private void initializeDoctorActor(String accessToken) {
    doctorActor = new DoctorActor(IdentityHelper.getHbaArztRsa2048());
    I_IDPClient idpClient = IdpClientFactory.determineIdpClient(accessToken);
    idpClient.login(doctorActor.getIdentity());
    doctorActor.setIdpClient(idpClient);
  }

  @Override
  public TaskActivateData invokeTaskActivat(byte[] signedBundle) throws RemoteException {
    signedBundle = replaceSignedBundleByMock(signedBundle);
    ResponseEnvelope responseEnvelope = doctorActor.issuePrescription(task, signedBundle);
    return initializeTaskActivateData(responseEnvelope);
  }

  private byte[] replaceSignedBundleByMock(byte[] signedBundle) {
    String mockSignedBundle = System.getenv(PROPERTY_MOCK_SIGNED_BUNDLE); //NOSONAR
    if (Boolean.parseBoolean(mockSignedBundle)) {
      String patientKVNR = "X234567890";
      String prescriptionId = task.getPrescriptionID().getValue();

      TaskLocalActions localAction = TaskLocalActions
          .builder()
          .identity(doctorActor.getIdentity())
          .patientKVNR(patientKVNR)
          .prescriptionID(prescriptionId)
          .build();

      KBV_Bundle kbvBundle = localAction.createKbvBundle();
      signedBundle = localAction.signQES(kbvBundle).get();//NOSONAR
    }
    return signedBundle;
  }

  private TaskActivateData initializeTaskActivateData(ResponseEnvelope responseEnvelope) {
    TaskActivateData taskActivateData = new TaskActivateData(responseEnvelope.getStatusCode(),
        EMPTY_STRING);
    if (responseEnvelope.getTask().isPresent()) {
      task = responseEnvelope.getTask().get();//NOSONAR
      taskActivateData.setStatus(task.getStatus());
    }
    return taskActivateData;
  }

  @Override
  public TaskAcceptData invokeTaskAccept(String accessToken) throws RemoteException {
    if (pharmacistActor == null) {
      initializePharmacistActor(accessToken);
    }

    if (task == null || !task.getStatus().equals(TASK_STATUS_READY)) {
      LOGGER.error("Es liegt kein Task aus dem Einstellen eines Rezeptes vor.");
      return null;
    }

    ResponseEnvelope responseEnvelope = pharmacistActor.acceptPrescription(task);
    return initializeTaskAcceptData(responseEnvelope);
  }

  private void initializePharmacistActor(String accessToken) {
    pharmacistActor = new PharmacistActor(IdentityHelper.getSmcbApothekeRsa2048());
    I_IDPClient idpClient = IdpClientFactory.determineIdpClient(accessToken);
    idpClient.login(pharmacistActor.getIdentity());
    pharmacistActor.setIdpClient(idpClient);
  }

  private TaskAcceptData initializeTaskAcceptData(ResponseEnvelope responseEnvelope) {
    TaskAcceptData taskAcceptData = new TaskAcceptData(responseEnvelope.getStatusCode(),
        EMPTY_STRING);
    if (responseEnvelope.getTaskAcceptBundle().isPresent()) {
      taskAcceptBundle = responseEnvelope.getTaskAcceptBundle().get();//NOSONAR
      task = taskAcceptBundle.getTask();
      taskAcceptData.setStatus(task.getStatus());
      taskAcceptData.setSecret(task.getSecret().getValue());
      ErxBinary binary = taskAcceptBundle.getBinary();
      taskAcceptData.setSignedPrescription(binary.getData());
    }
    return taskAcceptData;
  }

  @Override
  public TaskCloseData invokeTaskClose(MedicationData medicationData) throws RemoteException {
    Value value = new Value(SYSTEM_PZN, medicationData.getPznValue());
    KBV_Medication_PZN kbvMedicationPzn = KBV_Medication_PZN.builder().code(value).codeText(
        medicationData.getPznText()).build();

    ResponseEnvelope responseEnvelope = pharmacistActor
        .dispenseMedication(taskAcceptBundle, kbvMedicationPzn);

    int statusCode = responseEnvelope.getStatusCode();
    byte[] signature = null;
    Optional<Receipt> receipt = responseEnvelope.getReceipt();
    if (receipt.isPresent()) {
      signature = receipt.get().getSignature().getData();
    }

    return new TaskCloseData(statusCode, signature);
  }


}
