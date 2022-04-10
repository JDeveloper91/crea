import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

class PaymentGateway {

  protected String server;
  protected String port;
  protected String path;
  protected String security_key;

  public PaymentGateway(String key)
  {

    server = "integratepayments.transactiongateway.com";
    port = "443";
    path = "https://integratepayments.transactiongateway.com/api/transact.php";
    security_key = key;

  }

  public HashMap doSale( double amount,
                           String ccNumber,
                           String ccExp
                           ) throws Exception
  {
      HashMap result = new HashMap();
      HashMap request = new HashMap();
      
      DecimalFormat form = new DecimalFormat("#.00");

      request.put("amount", form.format(amount));
      request.put("type", "sale");
      request.put("ccnumber", ccNumber);
      request.put("ccexp", ccExp);

      System.out.println("Stage 1 done..");
      
      String data_out = prepareRequest(request);

      String error = "";
      String data_in = "";
      boolean success = true;
      try {
          HashMap retval = postForm(data_out);
          System.out.println("Stage 2 done..");
          data_in = (String)retval.get("response");
          result.put("transactionid", retval.get("transactionid"));
          System.out.println("Stage 3 done..");
      } catch (IOException e) {
          success = false;
          error = "Connect error, " + e.getMessage();
      } catch (Exception e) {
          success = false;
          error = e.getMessage();
      }
      if (!success) {
          throw new Exception(error);
      }

      return result;
  }

  // Utility Functions

  public String prepareRequest(HashMap request) {

      if (request.size() == 0) {
         return "";
      }

      request.put("security_key", security_key);

      Set s = request.keySet();
      Iterator i = s.iterator();
      Object key = i.next();
      StringBuffer buffer = new StringBuffer();



      buffer.append(key).append("=")
            .append(URLEncoder.encode((String) request.get(key)));

      while (i.hasNext()) {
          key = i.next();
          buffer.append("&").append(key).append("=")
                .append(URLEncoder.encode((String) request.get(key)));
      }

      return buffer.toString();

  }

  protected HashMap postForm(String data) throws Exception {

     HashMap result = new HashMap();

     HttpURLConnection postConn;

     HostnameVerifier hv = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
            return true;
        }
     };

     HttpsURLConnection.setDefaultHostnameVerifier(hv);
     URL post = new URL("https", server, Integer.parseInt(port), path);
     postConn = (HttpURLConnection)post.openConnection();

     System.out.println("a..");
     postConn.setRequestMethod("POST");
     postConn.setDoOutput(true);

     PrintWriter out = new PrintWriter(postConn.getOutputStream());
     out.print(data);
     out.close();

     System.out.println("b..");
     BufferedReader in =
        new BufferedReader(new InputStreamReader(postConn.getInputStream()));

     String inputLine;
     StringBuffer buffer = new StringBuffer();
     while ((inputLine = in.readLine()) != null) {
        buffer.append(inputLine);
     }
     in.close();

     System.out.println("c..");

     String response = buffer.toString();
     System.out.println("d.." + response);
     result.put("response", response);

     // Parse Result
     StringTokenizer st = new StringTokenizer(response, "&");
     while (st.hasMoreTokens()) {
        String varString = st.nextToken();
        StringTokenizer varSt = new StringTokenizer(varString, "=");
        if (varSt.countTokens() > 2 || varSt.countTokens()<1) {
            throw new Exception("Bad variable from processor center: " + varString);
        }
        if (varSt.countTokens()==1) {
            result.put(varSt.nextToken(), "");
        } else {
            result.put(varSt.nextToken(), varSt.nextToken());
        }
     }

     if (result.get("response")=="") {
        throw new Exception("Bad response from processor center" + response);
     }

     if (!result.get("response").toString().equals("1")) {
        throw new Exception(result.get("responsetext").toString());
     }

     return result;
  }

}