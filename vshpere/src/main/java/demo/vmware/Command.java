package demo.vmware;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Map;

/**
 * Abstract command to simply operation with vCenter.
 * User: Ma Tao
 * Date: 5/13/13
 * Time: 1:48 PM
 */
public abstract class Command {
    private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
    private static final String SVC_INST_NAME = "ServiceInstance";

    private String url;
    private String userName;
    private String password;
    private boolean help = false;
    private boolean isConnected = false;

    protected ManagedObjectReference rootRef;
    protected ServiceContent serviceContent;
    protected VimService vimService;
    protected VimPortType vimPort;

    public abstract void onExecute() throws Exception;

    // get common parameters
    private void getConnectionParameters(String[] args)
            throws IllegalArgumentException {
        int ai = 0;
        String param = "";
        String val = "";
        while (ai < args.length) {
            param = args[ai].trim();
            if (ai + 1 < args.length) {
                val = args[ai + 1].trim();
            }
            if (param.equalsIgnoreCase("--help")) {
                help = true;
                break;
            } else if (param.equalsIgnoreCase("--url") && !val.startsWith("--")
                    && !val.isEmpty()) {
                url = val;
            } else if (param.equalsIgnoreCase("--username")
                    && !val.startsWith("--") && !val.isEmpty()) {
                userName = val;
            } else if (param.equalsIgnoreCase("--password")
                    && !val.startsWith("--") && !val.isEmpty()) {
                password = val;
            }
            val = "";
            ai += 2;
        }
        if (url == null || userName == null || password == null) {
            throw new IllegalArgumentException(
                    "Expected --url, --username, --password arguments.");
        }
    }

    protected void printUsage() {
        System.out.println("This sample prints managed entity, its type, reference value,");
        System.out.println("property name, Property Value, Inner Object Type,");
        System.out.println("its Inner Reference Value and inner property value");
        System.out.println("\nParameters:");
        System.out.println("url         [required] : url of the web service");
        System.out.println("username    [required] : username for the authentication");
        System.out.println("password    [required] : password for the authentication");
        System.out.println("\nCommand:");
        System.out.println("run.bat com.vmware.general.Browser --url [webserviceurl] "
                + "--username [username] --password [password]");
    }

    private static class TrustAllTrustManager implements
            javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }


    private void trustAllHttpsCertificates() throws Exception {
        // Create a trust manager that does not validate certificate chains:
        javax.net.ssl.TrustManager[] trustAllCerts =
                new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    }

    /**
     * Establishes session with the vCenter server server.
     *
     * @throws Exception the exception
     */
    private void connect() throws Exception {

        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        SVC_INST_REF.setType(SVC_INST_NAME);
        SVC_INST_REF.setValue(SVC_INST_NAME);

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt =
                ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
        vimPort.login(serviceContent.getSessionManager(), userName, password,
                null);
        isConnected = true;

        rootRef = serviceContent.getRootFolder();
    }

    /**
     * Disconnects the user session.
     *
     * @throws Exception
     */
    private void disconnect() throws Exception {
        if (isConnected) {
            vimPort.logout(serviceContent.getSessionManager());
        }
        isConnected = false;
    }

    private void printSoapFaultException(SOAPFaultException sfe) {
        System.out.println("SOAP Fault -");
        if (sfe.getFault().hasDetail()) {
            System.out.println(sfe.getFault().getDetail().getFirstChild()
                    .getLocalName());
        }
        if (sfe.getFault().getFaultString() != null) {
            System.out.println("\n Message: " + sfe.getFault().getFaultString());
        }
    }

    public void execute(String[] args) {
        long start = System.currentTimeMillis();
        long start0 = 0L;
        long end0 = 0L;
        try {
            getConnectionParameters(args);
            if (help) {
                printUsage();
                return;
            }
            connect();
            start0 = System.currentTimeMillis();
            onExecute();
            end0 = System.currentTimeMillis();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                disconnect();
            } catch (SOAPFaultException sfe) {
                printSoapFaultException(sfe);
            } catch (Exception e) {
                System.out.println("Failed to disconnect - " + e.getMessage());
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println("onExecution time cost: " + (end0 - start0));
            System.out.println("  execution time cost: " + (end - start));
        }
    }
}
