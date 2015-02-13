package im.actor.messenger.core.actors.send.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import im.actor.messenger.core.actors.send.video.gl.InputSurface;
import im.actor.messenger.core.actors.send.video.gl.OutputSurface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ex3ndr on 20.09.14.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoTranscodeActor extends FilePipelineActor {

    private static final float MAX_SIZE = 640;
    private static final int TIMEOUT_USEC = 10000;

    private static final String TAG = "VideoTranscode";

    MediaCodec decoder = null;
    MediaCodec encoder = null;
    InputSurface inputSurface = null;
    OutputSurface outputSurface = null;

    ByteBuffer[] decoderInputBuffers;
    ByteBuffer[] encoderOutputBuffers;
    MediaCodec.BufferInfo info;

    boolean outputDone = false;
    boolean inputDone = false;
    boolean decoderDone = false;

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof VideoExtractorActor.NotifyMediaFormat) {
            VideoExtractorActor.NotifyMediaFormat mf = (VideoExtractorActor.NotifyMediaFormat) message;
            prepare(mf.getWidth(), mf.getHeight(), mf.getMediaFormat());
        } else if (message instanceof DoIteration) {
            doIteration();
        }
    }

    private void prepare(int w, int h, MediaFormat mediaFormat) {
        float scale = Math.min(MAX_SIZE / h, MAX_SIZE / w);
        int destW = (int) (w * scale);
        int destH = (int) (h * scale);

        MediaFormat outputFormat = MediaFormat.createVideoFormat("video/avc", destW, destH);
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1000000);

        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 15);

        try {
            encoder = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        inputSurface = new InputSurface(encoder.createInputSurface());
        inputSurface.makeCurrent();
        encoder.start();


        try {
            decoder = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputSurface = new OutputSurface();
        decoder.configure(mediaFormat, outputSurface.getSurface(), null, 0);
        decoder.start();

        decoderInputBuffers = decoder.getInputBuffers();
        encoderOutputBuffers = encoder.getOutputBuffers();
        info = new MediaCodec.BufferInfo();

        // editVideoData(inputData, decoder, outputSurface, inputSurface, encoder, outputData);
    }

    private void doIteration() {
        if (outputDone) {
            return;
        }

        if (!inputDone) {
            int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufIndex >= 0) {
                if (isCompleted()) {
                    // End of stream -- send empty frame with EOS flag set.
                    decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    inputDone = true;
                    // Log.d(TAG, "sent input EOS (with zero-length frame)");
                } else {
                    // Copy a chunk of input to the decoder.  The first chunk should have
                    // the BUFFER_FLAG_CODEC_CONFIG flag set.
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    inputBuf.clear();

                    // Read to decoder

//                    inputData.getChunkData(inputChunk, inputBuf);
//                    int flags = inputData.getChunkFlags(inputChunk);
//                    long time = inputData.getChunkTime(inputChunk);
//                    decoder.queueInputBuffer(inputBufIndex, 0, inputData.getChunkSize(inputChunk),
//                            time, flags);
                }
            } else {
                // Log.d(TAG, "input buffer not available");
            }
        }

        boolean decoderOutputAvailable = !decoderDone;
        boolean encoderOutputAvailable = true;
        while (decoderOutputAvailable || encoderOutputAvailable) {
            // Start by draining any pending output from the encoder.  It's important to
            // do this before we try to stuff any more data in.
            int encoderStatus = encoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                // Log.d(TAG, "no output from encoder available");
                encoderOutputAvailable = false;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = encoder.getOutputBuffers();
                // Log.d(TAG, "encoder output buffers changed");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = encoder.getOutputFormat();
                // Log.d(TAG, "encoder output format changed: " + newFormat);
                // outputData.setMediaFormat(newFormat);
                // TODO: Send media format
            } else if (encoderStatus < 0) {
                // fail("unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else { // encoderStatus >= 0
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    // fail("encoderOutputBuffer " + encoderStatus + " was null");
                }

                // Write the data to the output "file".
                if (info.size != 0) {
                    encodedData.position(info.offset);
                    encodedData.limit(info.offset + info.size);

                    // TODO: Send to next actor
                    // outputData.addChunk(encodedData, info.size, info.flags, info.presentationTimeUs);
                    // outputCount++;
                }
                // Log.d(TAG, "encoder output " + info.size + " bytes");
                outputDone = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                encoder.releaseOutputBuffer(encoderStatus, false);
            }
            if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                // Continue attempts to drain output.
                continue;
            }

            // Encoder is drained, check to see if we've got a new frame of output from
            // the decoder.  (The output is going to a Surface, rather than a ByteBuffer,
            // but we still get information through BufferInfo.)
            if (!decoderDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    // Log.d(TAG, "no output from decoder available");
                    decoderOutputAvailable = false;
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // decoderOutputBuffers = decoder.getOutputBuffers();
                    // Log.d(TAG, "decoder output buffers changed (we don't care)");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // expected before first buffer of data
                    // MediaFormat newFormat = decoder.getOutputFormat();
                    // Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    // fail("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    // Log.d(TAG, "surface decoder given buffer " + decoderStatus + " (size=" + info.size + ")");
                    // The ByteBuffers are null references, but we still get a nonzero
                    // size for the decoded data.
                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't
                    // guarantee that the texture will be available before the call
                    // returns, so we need to wait for the onFrameAvailable callback to
                    // fire.  If we don't wait, we risk rendering from the previous frame.
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                    if (doRender) {
                        // This waits for the image and renders it after it arrives.
                        // Log.d(TAG, "awaiting frame");
                        outputSurface.awaitNewImage();
                        outputSurface.drawImage();

                        // Send it to the encoder.
                        inputSurface.setPresentationTime(info.presentationTimeUs * 1000);
                        // Log.d(TAG, "swapBuffers");
                        inputSurface.swapBuffers();
                    }
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        // forward decoder EOS to encoder
                        //if (VERBOSE) Log.d(TAG, "signaling input EOS");
                        //if (WORK_AROUND_BUGS) {
                        // Bail early, possibly dropping a frame.
                        // return;
                        //} else {
                        encoder.signalEndOfInputStream();
                        //}
                    }
                }
            }
        }
    }

    @Override
    protected void onPipeAvailable(int available) {

    }

    @Override
    protected void onPipeCompleted() {

    }

    @Override
    public void finallyStop() {
        if (outputSurface != null) {
            outputSurface.release();
        }
        if (inputSurface != null) {
            inputSurface.release();
        }
        if (encoder != null) {
            encoder.stop();
            encoder.release();
        }
        if (decoder != null) {
            decoder.stop();
            decoder.release();
        }
    }

    private class DoIteration {

    }
}
