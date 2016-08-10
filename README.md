This code is made for logging all http request/response headers and bodies in Java EE application.

To use this classes "as is" you need:
 - at least Java 1.7 (because of try-with-resources);
 - Servlet 3.0 compliant container (because of @WebServlet);
 - slf4j-api.jar (which is using for actual logging).