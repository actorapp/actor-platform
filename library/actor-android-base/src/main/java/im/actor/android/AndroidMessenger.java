package im.actor.android;

import im.actor.model.BaseMessenger;
import im.actor.model.Configuration;

/**
 * Created by ex3ndr on 23.03.15.
 */
public class AndroidMessenger extends BaseMessenger {
    public AndroidMessenger(Configuration configuration) {
        super(configuration);
    }

//    public void sendDocument(Peer peer, String fullFilePath) {
//        sendDocument(peer, fullFilePath, new File(fullFilePath).getName());
//    }
//
//    public void sendDocument(Peer peer, String fullFilePath, String fileName) {
//        int dot = fileName.indexOf('.');
//        String mimeType = null;
//        if (dot >= 0) {
//            String ext = fileName.substring(dot + 1);
//            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
//        }
//
//        if (mimeType == null) {
//            mimeType = "application/octet-stream";
//        }
//
//        sendDocument(peer, fileName, mimeType, new AndroidFileSystemReference(fullFilePath));
//    }
//
//    public void sendPhoto(Peer peer, String fullFilePath) {
//        sendPhoto(peer, fullFilePath, new File(fullFilePath).getName());
//    }
//
//    public void sendPhoto(Peer peer, String fullFilePath, String fileName) {
//        try {
//            ImageMetadata metadata = new FileSource(fullFilePath).getImageMetadata();
//            Bitmap bitmap = ImageLoading.loadBitmapOptimizedHQ(fullFilePath);
//            Bitmap optimized = ImageRotating.fixExif(bitmap, metadata.getExifOrientation());
//            Bitmap smallThumb = ImageScaling.scaleFit(optimized, 90, 90);
//            byte[] data = ImageLoading.saveJpeg(smallThumb, ImageLoading.JPEG_QUALITY_LOW);
//            String resultFileName = AppContext.getExternalTempFile("image", "jpg");
//            if (resultFileName == null) {
//                return;
//            }
//            ImageLoading.save(optimized, resultFileName);
//
//            sendPhoto(peer, fileName, optimized.getWidth(), optimized.getHeight(),
//                    new FastThumb(smallThumb.getWidth(), smallThumb.getHeight(), data),
//                    new AndroidFileSystemReference(resultFileName));
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }
//
//    public void sendVideo(Peer peer, String fullFilePath) {
//        sendVideo(peer, fullFilePath, new File(fullFilePath).getName());
//    }
//
//    public void sendVideo(Peer peer, String fullFilePath, String fileName) {
//        try {
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//            retriever.setDataSource(fullFilePath);
//            int duration = (int) (Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000L);
//            Bitmap img = retriever.getFrameAtTime(0);
//            int width = img.getWidth();
//            int height = img.getHeight();
//            Bitmap smallThumb = ImageScaling.scaleFit(img, 90, 90);
//            byte[] smallThumbData = ImageLoading.saveJpeg(smallThumb, ImageLoading.JPEG_QUALITY_LOW);
//
//            FastThumb thumb = new FastThumb(smallThumb.getWidth(), smallThumb.getHeight(), smallThumbData);
//
//            sendVideo(peer, fileName, width, height, duration, thumb, new AndroidFileSystemReference(fullFilePath));
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
}