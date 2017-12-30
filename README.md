You can download the project from Github: https://github.com/jshamalainen/cybersecuritybase-project

It is a fork of the template project, so it can be prepared and executed the same way. 

----------

Issue: Lack of authentication 
Steps to reproduce:
1. Go to the site http://localhost:8080 and try to register using any name, e.g. "pekka" 
2. Registration succeeds without any form of authentication 


Mitigation: 
Simplest way is to let Spring create a login form and a session, by taking the commented out code into use instead of the current implementation that allows everyone. It is a bad idea to let anyone feed data in your database without adding any checks and hinderances (registration, captchas), because it takes just one miscreant with a computer to fill the database with junk and bury any real participants underneath. 

----------

Issue: Insecure direct access to data 
Steps to reproduce:
1. Go to the site http://localhost:8080 and register using any name, e.g. "jaska"
2. Register using another (different) name, e.g. "pekka"
3. Observe that there is an "Unregister" button. Check the source code of it. This button unregisters pekka.
4. You can now create a web page on a web server that has the following part in it: 
<form action='http://localhost:8080/participation/jaska' method='POST' name='_method'>
	<input type='hidden' name='_method' value='delete'>
	<input type='submit' value='Delete'>
</form>
5. Click the Submit button on your custom web page. This will remove "jaska" from participants list. 
6. Go to localhost:8080/participation/jaska to verify that jaska has been removed. 
7. Go to localhost:8080/participation/pekka to verify that pekka is still participating. 


Please refer to https://www.owasp.org/index.php/Testing_for_Insecure_Direct_Object_References_(OTG-AUTHZ-004)

Mitigation: 
The data access must be tied to user information contained in session after a proper login has been added. 

----------

Issue: SQL injection: 
Steps to reproduce:
1. Go to the site http://localhost:8080 and register using any name, e.g. "pekka"
2. Go to address http://localhost:8080/participation/pekka to check that the registration succeeded 
3. Now go to address http://localhost:8080/participation/%27%3bdrop%20table%20Signup%3b%27
4. Go to address http://localhost:8080/participation/pekka and observe that the Signup table is gone
5. To recover you need to restart the server. 

Mitigation: 
It would be easiest to use the ORM the same way it is done in function removeParticipation. Note that there's the direct object reference problem mentioned above which needs to be corrected, too. 

--------------------

Issue: cross-site scripting vulnerability (reflected)
Steps to reproduce: 
1. Go to the site http://localhost:8080 and register using a name containing javascript, e.g. "<script>alert("XSS");</script>"
2. Observe the popup saying "XSS". 
This implies that an attacker could trick the user to click a link which inserts javascript code in the database on user's behalf and immediately returns that to the user's browser, which may now execute it. 

Mitigation: 
Don't store or return user originating data as-is. Simplest approach would be treating it as text everywhere and ensuring it cannot be interpreted. One quick fix is to use th:text instead of th:utext in "done.html" template's h1 tag. 

I'm thinking about this approach because names are a difficult to validate. There are so many different ways different cultures name people. But at least strip away everything that the browser would understand as a directive instead of data by putting it through a library that sanitizes input. There are plenty of those, even OWASP appears to offer one. 

(Speaking of validating names, it would be best not to assume that names have some particular structure, or even that they contain just letters. Written name is just an approximation of sounds used to identify a person anyway, and you get closer with some character sets than with others. It would be kind to let people enter any text they want by supporting Unicode -- however, then they should also be assigned an identifier that can fit ASCII range, so they can be searched with reasonable effort. And probably such names wouldn't print properly everywhere, staff might have hard time figuring out how to pronounce it so that it's even remotely right, and such names might even crash some systems. In year 2017. Sigh.) 

----------

Issue: CSRF 
Steps to reproduce: 
1. Justg like in issue "Insecure direct access to data", go to the site and register using any name, e.g. "jaska"
2. Observe that there is an "Unregister" button. Check the source code of it to see how to format an attack.  
3. You can now create a web page on a web server that has the following part in it: 
<form action='http://localhost:8080/participation/jaska' method='POST' name='_method'>
	<input type='hidden' name='_method' value='delete'>
	<input type='submit' value='Delete'>
</form>
4. Click the Submit button on your custom web page. This will remove "jaska" from participants list. 
5. Go to http://localhost:8080/participation/jaska to verify that jaska has been removed. 
There is no authentication and there's insecure direct access to data, so CSRF attack is a bit pointless at the moment. But even after those other two vulnerabilities were closed, the application would be vulnerable to this attack. Thus, this security hole must be closed, too. 

Please refer to https://www.owasp.org/index.php/Testing_for_CSRF_(OTG-SESS-005)

Mitigation: 
Take CSRF cookies into use by uncommenting the related line in security configuration class, Spring makes this easy. 
