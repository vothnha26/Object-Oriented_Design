Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2026-03-31T09:56:18.799+07:00 ERROR 6204 --- [AloTraWebsite] [  restartedMain] o.s.b.d.LoggingFailureAnalysisReporter   :

***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 2 of constructor in com.alotra.controller.admin.AdminController required a single bean, but 2 were found:
- cloudinaryAdapter: defined in file [D:\Fullit\projects\OOP Design\target\classes\com\alotra\storage\CloudinaryAdapter.class]
- localStorageAdapter: defined in file [D:\Fullit\projects\OOP Design\target\classes\com\alotra\storage\LocalStorageAdapter.class]

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed

Ensure that your compiler is configured to use the '-parameters' flag.
You may need to update both your build tool settings as well as your IDE.
(See https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x#parameter-name-retention)



Process finished with exit code 0