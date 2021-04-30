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
import de.gematik.test.logger.TestNGLogManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

/**
 * Startet den RMI-Server, der den FD-Client wrappt.
 */
public class ApplicationStarter {

  static {
    TestNGLogManager.useTestNGLogger = false;
  }

  private static final int PORT_ZERO = 0;

  public static void main( String[] args ) throws RemoteException {
    LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

    FdClientImpl fdClientImpl = new FdClientImpl();
    FdClient fdClientStub = (FdClient) UnicastRemoteObject.exportObject(fdClientImpl, PORT_ZERO);
    RemoteServer.setLog(System.out);

    Registry registry = LocateRegistry.getRegistry();
    registry.rebind(FdClient.NAME, fdClientStub);
  }

}
