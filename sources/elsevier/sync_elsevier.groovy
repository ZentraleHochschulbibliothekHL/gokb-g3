#!groovy

@Grapes([
  @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
  @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14'),
  @Grab(group='javax.mail', module='mail', version='1.4.7'),
  @Grab(group='net.sourceforge.htmlunit', module='htmlunit', version='2.21'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.2'),
  @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.2'),
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.5.2'),
  @GrabExclude('org.codehaus.groovy:groovy-all')
])


import javax.mail.*
import javax.mail.search.*
import java.util.Properties
import static groovy.json.JsonOutput.*
import groovy.json.JsonSlurper
import java.security.MessageDigest
import com.gargoylesoftware.htmlunit.*
import groovyx.net.http.HTTPBuilder
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.StringBody
import groovyx.net.http.*
import org.apache.http.entity.mime.MultipartEntityBuilder /* we'll use the new builder strategy */
import org.apache.http.entity.mime.content.ByteArrayBody /* this will encapsulate our file uploads */
import org.apache.http.entity.mime.content.StringBody /* this will encapsulate string params */

config = null;
cfg_file = new File('./sync-elsevier-cfg.json')
if ( cfg_file.exists() ) {
  config = new JsonSlurper().parseText(cfg_file.text);
  if ( config.packageData==null ) {
    config.packageData=[:]
  }
}
else {
  config=[:]
  config.packageData=[:]
  config.publish=false
}

println("Using config ${config}");

println("Pulling latest messages");
pullLatest(config,'http://holdings.sciencedirect.com/ehr/manageProductReports.url', cfg_file);
println("All done");

def pullLatest(config, url, cfg_file) {
  def result = false;

  println("Get URL ${url}");
  client = new WebClient()
  client.getOptions().setThrowExceptionOnScriptError(false);
  client.getOptions().setJavaScriptEnabled(true);
  client.getOptions().setRedirectEnabled(true);
  client.getOptions().setCssEnabled(false);
  client.getOptions().setTimeout(600000);
  client.waitForBackgroundJavaScript(8000);
  client.setAjaxController(new NicelyResynchronizingAjaxController());
  client.getCookieManager().setCookiesEnabled(true);

  // Added as HtmlUnit had problems with the JavaScript
  // client.javaScriptEnabled = true
  html = client.getPage(url);
  
  boolean next_page = true;
  int page_count = 0;
  int package_count = 0;

  def httpbuilder = null;
  if ( config.publish == true ) {
    httpbuilder = new HTTPBuilder( 'http://localhost:8080' )
    httpbuilder.auth.basic config.uploadUser, config.uploadPass
  }

  while(next_page) {
    page_count++
    // List<?> links = page.getByXPath("//div[@class='generate']/@href");
    List<?> links = html.getByXPath("//a/@href");
    println("Processing ${links.size()} links");
    links.each { link ->
      if ( link.value.startsWith('../holdings/productReport.url') ) {
        def package_name = link.getOwnerElement().getParentNode().getByXPath('../td[@class="report"]/text()');
        try {
          processFile(package_name[0].toString(),link.value, config, httpbuilder);
        }
        catch ( Exception e ) {
          e.printStackTrace()
        }
        package_count++;
      }

      println("Updating config");
      cfg_file.delete()
      cfg_file << toJson(config);
    }

  
    def next_page_links = html.getByXPath("//a[text()='Next >']")
    if ( next_page_links.size() > 0 ) {
      html = next_page_links[0].click();
    }
    else {
      next_page = false;
    }
  }
  
  println("Done ${page_count} pages");
  println("Done ${package_count} packages");
}

def processFile(official_package_name, link, config, http) {

  def url_to_fetch = "http://holdings.sciencedirect.com/"+link.substring(3,link.length())
  println("processFile ${official_package_name} - ${url_to_fetch}");

  def package_id = url_to_fetch.substring(url_to_fetch.indexOf('=')+1,url_to_fetch.indexOf('&'));
  println("Package ID: \"${package_id}\"");

  if ( package_id == '' ) {
    package_id = 'MasterList'
  }

  def package_info = config.packageData[url_to_fetch]
  def process_package = false;

  if ( package_info == null ) {
    package_info = [last_check:0,cksum:0]
    config.packageData[url_to_fetch] = package_info
    process_package = true
  }

  if (process_package) {

    def package_data = new URL(url_to_fetch).getText()

    MessageDigest md5_digest = MessageDigest.getInstance("MD5");
    InputStream md5_is = new ByteArrayInputStream(package_data.getBytes());
  
    int filesize = 0;
    byte[] md5_buffer = new byte[8192];
    int md5_read = 0;
    while( (md5_read = md5_is.read(md5_buffer)) >= 0) {
      md5_digest.update(md5_buffer, 0, md5_read);
      filesize += md5_read
    }
    md5_is.close();
    byte[] md5sum = md5_digest.digest();
    def md5sumHex = new BigInteger(1, md5sum).toString(16);
  
    println("Hash for ${link} is ${md5sumHex}");
  
    if ( md5sumHex == package_info.cksum ) {
      println("Checksum not changed - update last checked and continue");
      package_info.last_check = System.currentTimeMillis()
    }
    else {
      println("Checksum changed - process file");
      // pushToGokb(official_package_name, package_data, http);
      package_info.cksum = md5sumHex
      package_info.last_check = System.currentTimeMillis()

      def ts_label = new java.text.SimpleDateFormat('yyyy-MM-dd').format(new Date());

      // Put package data in cache
      File cache_dir = new File('./package_cache')
      if ( !cache_dir.exists() ) {
        cache_dir.mkdir()
      }

      // String cached_package_file_name = "./package_cache/${package_id}_${ts_label}".toString()
      String cached_package_file_name = "./package_cache/elsevier_Global_${official_package_name}_${ts_label}_${package_id}".toString().replaceAll('/','_slash_')
      File cached_package_file = new File(cached_package_file_name)
      package_info.cached_file = cached_package_file_name

      cached_package_file << package_data
    }

  }
  else {
    println("Skipping package ${package_id} - already registered, and date check not expired");
  }

}

def pushToGokb(name, data, http) {
  // curl -v --user admin:admin -X POST \
  //   $GOKB_HOST/gokb/packages/deposit

  http.request(Method.POST) { req ->
    uri.path="/gokb/packages/deposit"

    MultipartEntityBuilder multiPartContent = new MultipartEntityBuilder()
    // Adding Multi-part file parameter "imageFile"
    multiPartContent.addPart("content", new ByteArrayBody( data.getBytes(), name.toString()))

    // Adding another string parameter "city"
    multiPartContent.addPart("source", new StringBody("ELSEVIER"))
    multiPartContent.addPart("fmt", new StringBody("elsevier"))
    multiPartContent.addPart("pkg", new StringBody(name.toString()))
    multiPartContent.addPart("platformUrl", new StringBody("http://www.sciencedirect.com/science"));
    multiPartContent.addPart("format", new StringBody("JSON"));
    multiPartContent.addPart("providerName", new StringBody("elsevier"));
    multiPartContent.addPart("providerIdentifierNamespace", new StringBody("ELSEVIER"));
    multiPartContent.addPart("reprocess", new StringBody("Y"));
    multiPartContent.addPart("synchronous", new StringBody("Y"));
    multiPartContent.addPart("flags", new StringBody("+ReviewNewTitles,+ReviewVariantTitles,+ReviewNewOrgs"));
    
    req.entity = multiPartContent.build()

    response.success = { resp, rdata ->
      if (resp.statusLine.statusCode == 200) {
        // response handling
        println("OK");
      }
    }
  }
}

