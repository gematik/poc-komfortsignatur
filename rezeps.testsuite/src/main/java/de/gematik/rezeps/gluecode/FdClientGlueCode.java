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

import de.gematik.rezeps.dataexchange.create.FdClient;
import de.gematik.rezeps.dataexchange.create.MedicationData;
import de.gematik.rezeps.dataexchange.create.TaskAcceptData;
import de.gematik.rezeps.dataexchange.create.TaskActivateData;
import de.gematik.rezeps.dataexchange.create.TaskCloseData;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import de.gematik.rezeps.fd.TaskValidator;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/** Ermöglicht die Kommunikation mit dem E-Rezept-Fachdienst. */
public class FdClientGlueCode {

  private static final Logger LOGGER = LoggerFactory.getLogger(FdClientGlueCode.class);

  private FdClient fdClient;

  public FdClientGlueCode() throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry();
    fdClient = (FdClient) registry.lookup(FdClient.NAME);
  }

  protected FdClientGlueCode(FdClient fdClient) {
    this.fdClient = fdClient;
  }

  /**
   * Ruft den Task create beim E-Rezept Fachdienst auf. Die Daten der Response, die im weiteren
   * Testablauf benötigt werden, werden im Objekt TestcaseData bereitgestellt.
   */
  public void invokeTaskCreate() {
    try {
      String accessToken = TestcaseData.getInstance().getAccessTokenPrescribingEntity();
      TaskCreateData taskCreateData = fdClient.invokeTaskCreate(accessToken);
      TestcaseData.getInstance().setTaskCreateData(taskCreateData);
    } catch (RemoteException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob der Task create erfolgreich durchgeführt werden konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskCreatedOk() {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskCreatedOk(taskCreateData);
  }

  /** Ruft den Task activate aus Sicht des verordnenden LEs auf. */
  public void invokeTaskActivate() {
    try {
      TestcaseData testcaseData = TestcaseData.getInstance();
      byte[] signedBundle = testcaseData.getSignDocumentResult().getSignedBundle();
      TaskActivateData taskActivateData = fdClient.invokeTaskActivat(signedBundle);
      testcaseData.setTaskActivateData(taskActivateData);
    } catch (RemoteException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob der Task activate aus Sicht des verordnenden LEs erfolgreich durchgeführt werden
   * konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskActivateOk() {
    TaskActivateData taskActivateData = TestcaseData.getInstance().getTaskActivateData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskActivateDataOk(taskActivateData);
  }

  /** Ruft den Task activate aus Sicht des abgebenden LEs auf. */
  public void invokeTaskAccept() {
    try {
      String accessTokenDispensingEntity =
          TestcaseData.getInstance().getAccessTokenDispensingEntity();
      TaskAcceptData taskAcceptData = fdClient.invokeTaskAccept(accessTokenDispensingEntity);
      TestcaseData.getInstance().setTaskAcceptData(taskAcceptData);
    } catch (Exception exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  /**
   * Prüft, ob der Task activate aus Sicht des abgebenden LEs erfolgreich durchgeführt werden
   * konnte.
   *
   * @return True, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskAcceptOk() {
    TaskAcceptData taskAcceptData = TestcaseData.getInstance().getTaskAcceptData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskAcceptDataOk(taskAcceptData);
  }

  /** Ruft den Task close aus Sicht des abgebenden Leistungserbringers beim Fachdienst auf. */
  public void invokeTaskClose(MedicationData medicationData) {
    try {
      TaskCloseData taskCloseData = fdClient.invokeTaskClose(medicationData);
      TestcaseData.getInstance().setTaskCloseData(taskCloseData);
    } catch (RemoteException remoteException) {
      LOGGER.error(remoteException.getMessage(), remoteException);
    }
  }

  /**
   * Prüft, ob der Task close aus Sicht des abgebenden LE erfolgreich durchgeführt werden konnte.
   *
   * @return true, falls der Task erfolgreich durchgeführt werden konnte, andernfalls false.
   */
  public boolean checkTaskCloseOk() {
    TaskCloseData taskCloseData = TestcaseData.getInstance().getTaskCloseData();
    TaskValidator taskValidator = new TaskValidator();
    return taskValidator.validateTaskCloseDataOk(taskCloseData);
  }

  /**
   * Prüft, ob ein AccessCode verfügbar ist.
   *
   * @throws MissingPreconditionException Falls kein AccessCode verfügbar ist.
   */
  public void checkAccessCodeAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    if (taskCreateData == null || StringUtils.isEmpty(taskCreateData.getAccessCode())) {
      throw new MissingPreconditionException("Es liegt kein AccessCode vor");
    }
  }

  /**
   * Prüft, ob eine Task-ID des E-Rezepts verfügbar ist.
   *
   * @throws MissingPreconditionException Falls keine Task-ID des E-Rezepts verfügbar ist
   */
  public void checkTaskIdAvailable() throws MissingPreconditionException {
    TaskCreateData taskCreateData = TestcaseData.getInstance().getTaskCreateData();
    if (taskCreateData == null || StringUtils.isEmpty(taskCreateData.getTaskId())) {
      throw new MissingPreconditionException("Es liegt keine Task-ID des E-Rezepts vor");
    }
  }
}
