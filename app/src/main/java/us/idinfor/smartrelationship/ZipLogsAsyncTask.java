package us.idinfor.smartrelationship;

import android.app.ProgressDialog;
import android.os.AsyncTask;

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
