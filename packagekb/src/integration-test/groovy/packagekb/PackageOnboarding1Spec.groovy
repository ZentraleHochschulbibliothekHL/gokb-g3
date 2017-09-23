package packagekb


import grails.testing.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder

@Integration
@Rollback
class PackageOnboarding1Spec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "Submit a package"() {
        when:"The home page is requested"

            // println("hello\n\n\n");
            // URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
            // println(urls)

            // File package_file =  PackageOnboarding1Spec.class.getClassLoader().getResource("/elsevier_Global_Backfile Package - Computer Science 1995 [BFJCSC95]_2017-09-11_96497 ").file
            // File package_file = new File('src/integration-test/resources/elsevier_Global_Backfile Package - Computer Science 1995 [BFJCSC95]_2017-09-11_96497');
            // File package_file = PackageOnboarding1Spec.class.getClassLoader().getResource("/elsevier_Global_Backfile Package - Computer Science 1995 [BFJCSC95]_2017-09-11_96497 ").file
            // File package_file = Thread.currentThread().getContextClassLoader().getResource("/elsevier_Global_Backfile Package - Computer Science 1995 [BFJCSC95]_2017-09-11_96497 ").file
            // File package_file = Thread.currentThread().getContextClassLoader().getResource("/file.tsv").file;
            java.net.URL package_file_uri = (java.net.URL) Thread.currentThread().getContextClassLoader().getResource('file.tsv')
            println("Got package file: ${package_file_uri.class.name} ${package_file_uri}");
            File package_file_resource = new File(package_file_uri.toURI());


            def resp = restBuilder().post("$baseUrl/package/elsevier/admin") {
              contentType "multipart/form-data"
              package_file = package_file_resource
              wait = true
            }

        then:"The response is correct"
            resp.status == OK.value()
            resp.headers[CONTENT_TYPE] == ['application/json;charset=UTF-8']
            resp.json.message == 'OK!'
    }

    RestBuilder restBuilder() {
        new RestBuilder()
    }
}
