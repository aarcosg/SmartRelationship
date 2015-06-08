package us.idinfor.smartrelationship;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class ListeningService extends WakefulIntentService {
  public ListeningService() {
    super("ListeningService");
  }

  @Override
  protected void doWakefulWork(Intent intent) {
    Log.e("OLE","Estoy en Listening Service");
    File log=new File(Environment.getExternalStorageDirectory(),
                      "AlarmLog.txt");
    
    try {
      BufferedWriter out=new BufferedWriter(
                            new FileWriter(log.getAbsolutePath(),
                                            log.exists()));
      
      out.write(new Date().toString());
      out.write("\n");
      out.close();
    }
    catch (IOException e) {
      Log.e("AppService", "Exception appending to log file", e);
    }
  }
}