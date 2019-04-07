# BackgroundLocationTracking
Android application to track distance driven in a car with background services

Application works by creating service(LocationService and LocationServiceRestarterBroadcastReceiver)
which is mostly unkillable by android OS, when 'START' button is pressed, subscribing to Google's user
activity recognition API(BackgroundDetectedActivitiesService and DetectedActivitesIntentService) and
when user activity change is detected(user driving), app starts gps location tracking(GPSTracker)
and storing locations to database(DBClass andDBHandler). When another activity(user on foot) is detected,
gps tracking stops, app calculates total distance driven in meters on that trip. After that if user does 
not stop app services(STOP button), activity tracking service continues tracking activities.


If you want more or less notifications and toasts(for user notifying or debugging)
 go to res -> values -> settings_values.xml -> change 'notificationIntrusivenessLevel' to:
0 Silent, no notifications or toasts
1 Just notifications
2 Notifications and activity tracking service toasts
