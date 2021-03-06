# FDR Flight Recorder #

Flight Recorder is an application that can log and record flights. The user inputs the aircraft registration, departing airport, and the pilot's name, and the application pulls weather information and GPS and gyroscope data to record the aircraft's location and orientation during the flight. This data can then be exported in file formats, such as X-Plane's FDR format, to be played back at a later time. The user can also set the recording to start and stop with the engine. As the sound increases in amplitude when the engine is turned on, the recording can start, and when the engine is turned off and the sound amplitude dies down, the recording stops. The average sound within a five-second duration is used, to ensure that the recording does not start or stop accidentally.

![Aircraft Screen](doc/aircraft.png)

![Menu](doc/menu.png)

![Recording Screen](doc/recording.png)
