package im.actor.core.js.entity;

public class JsContentVoice extends JsContent {

    public native static JsContentVoice create(String fileName, String fileExtension, String fileSize, String fileUrl, boolean isUploading, int duration)/*-{
        return {content: "voice", fileName: fileName, fileExtension: fileExtension, fileSize: fileSize, fileUrl: fileUrl, isUploading: isUploading, duration: duration};
    }-*/;

    protected JsContentVoice() {

    }
}