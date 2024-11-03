# MeetMe



git clone https://github.com/sachn99e/MeetMe.git
cd MeetMe

mvn clean install

mvn test

mvn spring-boot:run




Api:


http://localhost:8080/api/calendar/book

Request:
{
  "ownerId": 1,
  "participantIds": [2, 3],
  "startTime": "2024-11-06T11:30:00",
  "duration": "PT1H"
}



http://localhost:8080/api/calendar/availability?userId1=1&userId2=2&duration=PT30M

