package fr.areastudio.jwterritorio.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.activeandroid.query.Select;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Assignments;
import fr.areastudio.jwterritorio.model.Territory;

/**
 * Created by julien on 08.10.15.
 */
public class Printer {
    private final Context context;
    private WebView mWebView;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("dd-MM-yyyy");
//    private Collection<PrintJob> mPrintJobs;

    public Printer(Context context) {
        this.context = context;
    }

    public void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("PRINTER", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = generateS13();
        webView.loadDataWithBaseURL("file:///android_asset/", htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) context
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            printAdapter = webView.createPrintDocumentAdapter("S-13");
        } else {
            printAdapter = webView.createPrintDocumentAdapter();
        }

        // Create a print job with name and adapter instance
        String jobName = context.getString(R.string.app_name) + " Document";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
//        mPrintJobs.add(printJob);
    }

    public String generateS13() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
//        if (context.getSharedPreferences(
//                TalksActivity.PREFS, 0).getString("FONT_PATH",null) != null){
//            sb.append("<head>");
//            sb.append("<style type=\"text/css\">\n" +
//                    "    @font-face { font-family: 'Special'; src: url('").append(context.getSharedPreferences(
//                    TalksActivity.PREFS, 0).getString("FONT_PATH",null)).append("');}\n");
//            sb.append("body { font-family:'Special';}");
//            sb.append("</style>");
//            sb.append("</head>");
//        }
        sb.append("<body>");
        //sb.append("<h1 style='text-align:center'>");
        List<Territory> terrs = new Select().from(Territory.class).where("number <> ","-1").orderBy("name").execute();

        for (int i = 0; i <= Math.ceil(terrs.size()/5);i++){
            sb.append("<div>");
            sb.append("<table border=\"1\" style=\"width:1000px;border-collapse: collapse;\">");
            sb.append("<tr valign=\"top\">");
            for (int j = 5*i; j < Math.min(terrs.size(),5*i+5);j++){
                sb.append("<td style=\"width:200px;text-align: center\">");
                sb.append("<b>" + terrs.get(j).name + "</b>");
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("<tr valign=\"top\">");
            for (int j = 5*i; j < Math.min(terrs.size(),5*i+5);j++){
                sb.append("<td style=\"width:200px;text-align: center\">");
                List<Assignments> assigns = new Select().from(Assignments.class).where("territory = ?", terrs.get(j).getId()).orderBy("assign_date").execute();
                for (Assignments a: assigns) {
                    sb.append(" <table border=\"1\" style=\"width:100%;border-collapse: collapse;\">");
                    sb.append("<tr valign=\"top\">");
                    sb.append("<td colspan=\"2\" style=\"text-align: center\">");
                    sb.append("<div style=\"width:100%;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;\">"+((a.publisher == null || a.publisher.name == null) ? "" : a.publisher.name.toUpperCase())+"</div>");
                    sb.append("</td>");
                    sb.append("</tr>");
                    sb.append("<tr valign=\"top\" style=\"font-size: 0.8rem\">");
                    sb.append("<td style=\"width:50%;text-align: center\">"+((a.dateBegin != null) ? dateformatter.format(a.dateBegin) : "") +"</td>");
                    sb.append("<td style=\"width:50%;text-align: center\">"+((a.dateEnd != null) ? dateformatter.format(a.dateEnd) : "") +"</td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                }
            }
            sb.append("</tr>");
            sb.append("</table>");
            /*sb.append("");
            sb.append("");
            sb.append("");
            sb.append("");
            sb.append("");
            sb.append("");
            sb.append("");
            sb.append("");


              <tr valign="top">
                <td *ngFor="let terr of territories.slice(5*i,5*i + 5);" style="width:200px;text-align: center">
                  <table border="1" *ngFor="let assign of assign_index.get(terr.id)" style="width:200px;border-collapse: collapse;">
                    <tr valign="top">
                      <td colspan="2" style="text-align: center">
                        <div style="width:200px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{assign.publisher[1].toUpperCase()}}</div>
                      </td>
                    </tr>
                    <tr valign="top" style="font-size: 0.8rem">
                      <td style="width:50%;text-align: center">{{getDate(assign.assignDate)}}</td>
                      <td style="width:50%;text-align: center">{{getDate(assign.backDate)}}</td>
                    </tr>

                  </table>
                </td>
              </tr>*/


            sb.append("</div>");
        }


        /*
         <div *ngFor="let t of temp(math.ceil(territories.length/5)).fill(); let i = index" class="row" style="margin-bottom:20px;">
          <div class="col-12">
            <table border="1" style="width:1000px;border-collapse: collapse;">
              <tr valign="top">
                <td *ngFor="let terr of territories.slice(5*i,5*i + 5);" style="width:200px;text-align: center">
                  <b>{{terr.name.substring(terr.name.indexOf("-",terr.name.length-6) + 1)}}</b>
                </td>
              </tr>
              <tr valign="top">
                <td *ngFor="let terr of territories.slice(5*i,5*i + 5);" style="width:200px;text-align: center">
                  <table border="1" *ngFor="let assign of assign_index.get(terr.id)" style="width:200px;border-collapse: collapse;">
                    <tr valign="top">
                      <td colspan="2" style="text-align: center">
                        <div style="width:200px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{assign.publisher[1].toUpperCase()}}</div>
                      </td>
                    </tr>
                    <tr valign="top" style="font-size: 0.8rem">
                      <td style="width:50%;text-align: center">{{getDate(assign.assignDate)}}</td>
                      <td style="width:50%;text-align: center">{{getDate(assign.backDate)}}</td>
                    </tr>

                  </table>
                </td>
              </tr>
            </table>
          </div>
        */
        sb.append("<body></html>");
        return sb.toString();
    }


}
