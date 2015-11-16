package us.idinfor.smartrelationship.zip;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import us.idinfor.smartrelationship.Utils;

public class ZipLogsAsyncTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog progress;

    public ZipLogsAsyncTask(ProgressDialog progress) {
        this.progress = progress;
    }

    @Override
    public void onPreExecute() {
        progress.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        return Utils.zipLogFiles();
    }

    @Override
    public void onPostExecute(String filepath) {
        progress.dismiss();
    }


}
