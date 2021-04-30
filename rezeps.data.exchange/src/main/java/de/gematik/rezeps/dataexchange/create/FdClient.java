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

package de.gematik.rezeps.dataexchange.create;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Stellt Methoden für Aufrufe beim E-Rezept-Fachdienst bereit.
 */
public interface FdClient extends Remote { //NOSONAR

  String NAME = "FdClient";

  /**
   * Ruft den Task create beim Fachdienst auf.
   * @param accessToken Das vom IDP erhaltene Access Token
   * @return Ergebnis des Aufrufs von Task create, das für die weitere Testdurchführung benötigt wird.
   */
  TaskCreateData invokeTaskCreate(String accessToken) throws RemoteException;

  /**
   * Ruft den Task activate aus Sicht des verschreibenden LE beim Fachdienst auf.
   * @param signedBundle Der signierte Verordnungsdatensatz.
   * @return Ergebnis des Tasks activate für die Auswertung
   */
  TaskActivateData invokeTaskActivat(byte[] signedBundle) throws RemoteException;

  /**
   * Ruft den Task accept aus Sicht des abgebenden LE beim Fachdienst auf.
   * @param accessToken Das vom IDP erhaltene Access Token
   * @return Ergebnis des Aufrufs von Task accept, das für die weitere Testdurchführung benötigt wird.
   */
  TaskAcceptData invokeTaskAccept(String accessToken) throws RemoteException;

  /**
   * Ruft den Task close aus Sicht des abgebenden Leistungserbringers auf.
   * @param medicationData Die Daten des ausgegebenen Medikamentes.
   * @return Ergebnis des Tasks close für die Auswertung und weitere Verarbeitung.
   */
  TaskCloseData invokeTaskClose(MedicationData medicationData) throws RemoteException;


}
