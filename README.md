# Proof of Concept Komfortsignatur
## Überblick

Der Proof of Concept für die Komfortsignatur soll Primärsystem-Herstellern eine Orientierung zur
Implementierung geben, er umfasst:
* Die wesentlichen Szenarien der Komfortsignatur in Form eines Cucumber Features
* Implementierungsbeispiele mittels SpringBoot und JAXB

Der aktuelle Stand des PoC ist außerhalb der gematik nicht kompilier- und lauffähig, da Maven-
Dependencys verwendet werden, die aktuell nur in der gematik geladen werden können und benötigte
Gegenstellen ggf. nicht verfügbar sind (z.B. ein Konnektor der die Komfortsignatur unterstützt).

## Lizenzhinweis

Copyright (c) 2020 gematik GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Getting started

Um die Client-Schnittstelle des Konnektors für die Komfortsignatur nutzen zu können, werden zunächst
aus den WSDL-Beschreibungen der Schnittstelle mittels JAXB Java-Klassen generiert. Dies erfolgt 
währed des Maven Builds. In der Datei rezeps.testsuite/pom.xml kann anhand des Plugins 
org.codehaus.mojo / jaxws-maven-plugin die Konfiguration nachvollzogen werden. Wir weisen auf 
folgende Besonderheit hin: Aktuell verwendet der AuthSignatureService Nachrichten aus dem 
SignatureService, jedoch in einer älteren Version. Daher ist es notwendig die Nachichten des 
AuthSignatureService separat zu generieren und vorzuhalten. Wir lösen dies mittels JAXB Bindings. 
Die vom AuthSignatureService benötigten Nachrichten des SignatureService werden dabei in ein 
separates Package generiert.

Die Szenarien sind in der Datei 
rezeps.testsuite/src/test/resources/features/poc_komfortsignatur.feature abgelegt. Von dort aus 
können die Abläufe nachvollzogen werden

* Die Cucumber-Steps rufen Methoden in der Klasse de.gematik.rezeps.cucumber.stepdefinitions.KonnektorStepDefinitions
auf
* Die Klasse de.gematik.rezeps.gluecode.KonnektorGlueCode implementiert eine Facade, die Aufrufe an
den Konnektor kapselt und Response-Daten für den weiteren Test-Verlauf zur Verfügung stellt
* Im nächsten Layer finden sich SpringBeans, welche die Logik eines Aufrufs an den Konnektor
kapseln. Diese SpringBeans werden jeweils von zwei Klassen unterstützt:
  
  * *NameDerKonnektorOperation*Configuration: Implementiert das Marshalling / Unmarshalling der 
  SOAP-Nachrichten
  * Perform*NameDerKonnektorOperation*: Befüllt die Datenstruktur, die den SOAP-Request 
  repräsentiert, sendet diesen und empfängt die Response

Nachfolgend werden nun die einzelnen Module der Anwendung detaillierter beschrieben. Diese 
Dokumentation richtet sich in erster Linie an die Entwickler des Testsystems.

## Module der Anwendung
Die Anwendung RezePS besteht insbesondere aus den Modulen rezeps.testsuite, rezeps.fd.client und
rezeps.data.exchange. Diese werden nachfolgend beschrieben.

### rezeps.testsuite
Das Modul rezeps.testsuite umfasst 
* Die von den Tester:innen erstellten Testfälle (nicht im PoC Komfortsignatur enthalten)
* Die Schnittstelle zwischen Cucumber und Java
* Die Client-Schnittstelle zum Konnektor

Die Testfälle werde im Ordner src/test/resources/features abgelegt. Die Klasse 
KonnektorStepDefinitions bildet die Schnittstelle zwischen Cucumber und Java. Die Konnektor-
Schnittstelle ist insbesondere in folgenden Packages hinterlegt:
* de.gematik.rezeps.authentication
* de.gematik.rezeps.card
* de.gematik.rezeps.certificate
* de.gematik.rezeps.comfortsignature
* de.gematik.rezeps.signature

### rezeps.fd.client
Im Modul rezeps.fd.client wird die Schnittstelle zum E-Rezept-Fachdienst implementiert. Dazu wird
Funktionalität aus dem Projekt erp-testsuite verwendet. Die Methoden im Modul rezeps.fd.client 
werden über RMI aufgerufen. Das Projekt erp-testsuite bringt eine Vielzahl von Abhängigkeiten 
mit. Durch die Trennung in unterschiedlich Prozesse, welche durch RMI ermöglicht wird, werden 
Dependency-Konflikte zur Laufzeit vermieden.

### rezeps.data.exchange
In diesem Modul wird die Schnittstelle des Moduls rezeps.fd.client definiert, so dass sie für das
Modul rezeps.testsuite zum Import zur Verfügung steht.

## Erweiterung der Konnektor-Schnittstelle
Um eine weitere Operation des Konnektors zu nutzen, müssen drei Klassen implementiert werden:
* Eine Spring Boot Configuration, die insbesondere das Marshalling / Unmarshalling von Java-Objekten
in SOAP-Nachrichten übernimmt (Beispiel: GetCardsConfiguration)
* Eine Klasse, die das Senden der Nachricht und Empfangen der Response umsetzt. Dazu ist die Klasse 
  WebServiceGatewaySupport des Frameworks Spring Boot zu erweitern (Beispiel: PerformGetCards)
* Eine Spring-Boot-Komponente, welche Logik im Zusammenhang mit der Operation implementiert 
  (Beispiel: PerformGetCards)
  
Falls die zu unterstützende Operation in den Umfang eines Konnektor-Dienstes fällt, der bisher nicht
angesprochen wurde, so müssen auch die Java-Klassen aus der entsprechende WSDL generiert werden. 
Dazu ist die WSDL, welche die zu unterstützende Funktion enthält, in der POM des Moduls 
rezeps.testsuite hinzuzufügen. Der Eintrag wird in der Konfiguration des Plugins jaxws-maven-plugin
unterhalb von wsdlFiles hinzugefügt. Nach dem nächsten Maven-Build stehen die benötigten Klassen
dann zur Verfügung.

## Ausführung der Integrationstests
Im Moduel rezeps.testsuite gibt es einige Integrationstests, die dem Entwickler dazu dienen können 
Use Cases und Abläufe durchzuspielen. Erkennbar sind diese Tests am Postfix IntegrationTest im 
Klassennamen. Die Integrationstests werden aktuell nicht über die CI ausgeführt, sie erfordern lokal
folgende Voraussetzungen:
* Der Konnektor-Simulator ist installiert und gestartet
* Die Docker-Umgebung des E-Rezept-Fachdienstes ist gestartet

Da der Konnektor-Simulator und der E-Rezept-Fachdienst unterschiedliche Vertrauensräume nutzen, ist 
es notwendig beim Einstellen eines Rezeptes das signierte Rezept zu mocken. Das vom 
Konnektor-Simulator signierte Rezept würde vom E-Rezept-Fachdienst abgelehnt werden. Um das Rezept 
zu mocken, muss die Umgebungsvariable mockSignedBundle auf true gesetzt werden.


