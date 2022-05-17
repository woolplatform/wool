# WOOL Platform ReadMe
WOOL is a simple, powerful dialogue platform for creating virtual agent conversations. Visit www.woolplatform.eu for full documentation, news and updates.

## Repository structure
This repository is structured as follows:

  * html5 - Contains all web and javascript tools, including the WOOL Editor
  * java - Contains the libraries and tools written in the Java Language:
    * WoolCore - Implementation of the Gold Standard WOOL Language Parser in Java.
    * WoolUtils - A library of utility functions used throughout other Wool Projects.
    * WoolWebService - A Spring Boot application (Web Service) that can execute WOOL Dialogues server-side.
    * WoolExternalVariableServiceDummy - A dummy implementation of an "External WOOL Variable Service".
  * test-dialogues - Contains a number of sample .wool scripts and .json file used for testing.

## WOOL Editor Desktop version
The WOOL Editor can be used online (see www.woolplatform.eu), but can also be run as a Desktop application. While we are working on a user-friendly installer, you can run it as follows:

  * Install NodeJS (see www.nodejs.org)
  * Use a terminal / command prompt to go to the folder /woolplatform/html5/
  * Use "npm install" to install
  * Use "npm run start" to run the WOOL Editor as Desktop application
