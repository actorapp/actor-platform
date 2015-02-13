package im.actor.messenger.core.actors.log;

import android.content.Intent;
import android.net.Uri;
import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.util.RandomUtil;
import im.actor.messenger.util.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ex3ndr on 02.10.14.
 */
public class FileLog extends Actor {

    public static ActorSelection fileLog(final String fileName) {
        return new ActorSelection(Props.create(FileLog.class, new ActorCreator<FileLog>() {
            @Override
            public FileLog create() {
                return new FileLog(fileName);
            }
        }), fileName);
    }

    private FileOutputStream stream;
    private OutputStreamWriter streamWriter;

    private String fileName;
    private File file;
    private SimpleDateFormat simpleDateFormat;

    public FileLog(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void preStart() {
        super.preStart();
        new File(AppContext.getContext().getFilesDir(), "/logs/").mkdirs();
        file = new File(AppContext.getContext().getFilesDir(), "/logs/" + fileName);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        // file.mkdirs();
        open();
    }

    private void close() {
        try {
            if (streamWriter != null) {
                streamWriter.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        streamWriter = null;
        stream = null;
    }

    private void open() {
        try {
            stream = new FileOutputStream(file, true);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("============LOGGER_STARTED============\r\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            if (streamWriter != null) {
                try {

                    streamWriter.write(simpleDateFormat.format(new Date(System.currentTimeMillis())) + "| " + message + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (message instanceof Export) {
            close();
            try {
                File destFile = new File(AppContext.getContext().getExternalFilesDir(null), "log_export" + RandomUtil.randomId() + ".txt");
                IOUtils.copy(file, destFile);
                AppContext.getContext().startActivity(new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(destFile))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (IOException e) {
                e.printStackTrace();
            }
            open();
        } else if (message instanceof Clear) {
            close();
            file.delete();
            open();
        }
    }

    public static class Export {


    }

    public static class Clear {

    }
}
