package im.actor.messenger.app.emoji.keyboard;

/**
 * Created by Jesus Christ. Amen.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;

import im.actor.messenger.app.emoji.EmojiProcessor;

/**
 * Author: Korshakov Stepan
 * Created: 04.07.13 22:50
 */
public class EmojiPackView extends View {

    public interface OnEmojiClickedListener {
        public void onEmojiClicked(long smileId);
    }

    private int rowCount;
    private int countInRow;
    private EmojiProcessor processor;
    private long[] smileyIds;
    private int[] smileysSections;
    private int[] smileysX;
    private int[] smileysY;
    private int smileySrcSize;
    private int smileySize;
    private int smileyPadding;
    private Rect rect = new Rect();
    private Rect sectionRect = new Rect();
    private Paint paint = new Paint();
    private OnEmojiClickedListener onEmojiClickedListener;
    private float touchX, touchY;

    public EmojiPackView(Context context, EmojiProcessor processor,
                         long[] smileyIds, int smileysInRow, int smileySize, int smileyPadding) {
        super(context);
        this.rowCount = (int) Math.ceil((float) smileyIds.length / smileysInRow);
        this.processor = processor;
        this.smileyIds = smileyIds;
        this.countInRow = smileysInRow;
        this.smileySize = smileySize;
        this.smileyPadding = smileyPadding;
        this.smileySrcSize = processor.getRectSize();

        smileysSections = new int[smileyIds.length];
        smileysX = new int[smileyIds.length];
        smileysY = new int[smileyIds.length];
        for (int i = 0; i < smileyIds.length; i++) {
            smileysSections[i] = processor.getSectionIndex(smileyIds[i]);
            smileysX[i] = processor.getSectionX(smileyIds[i]);
            smileysY[i] = processor.getSectionY(smileyIds[i]);
        }

        this.paint.setAntiAlias(true);
        this.paint.setFlags(Paint.FILTER_BITMAP_FLAG);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(smileySize * countInRow, smileySize * rowCount);
    }

    public OnEmojiClickedListener getOnEmojiClickedListener() {
        return onEmojiClickedListener;
    }

    public void setOnEmojiClickedListener(OnEmojiClickedListener onEmojiClickedListener) {
        this.onEmojiClickedListener = onEmojiClickedListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (Math.abs(event.getX() - touchX) < slop && Math.abs(event.getY() - touchY) < slop) {
                    int offsetLeft = (getWidth() - countInRow * smileySize) / 2;
                    if (touchX > offsetLeft || touchX < offsetLeft + smileySize * countInRow) {
                        int row = (int) (touchY / smileySize);
                        int col = (int) ((touchX - offsetLeft) / smileySize);
                        int index = row * countInRow + col;
                        if (index >= 0 && index < smileyIds.length) {
                            if (onEmojiClickedListener != null) {
                                playSoundEffect(SoundEffectConstants.CLICK);
                                onEmojiClickedListener.onEmojiClicked(smileyIds[index]);
                            }
                        }
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (processor.isLoaded()) {
            int offsetLeft = (getWidth() - countInRow * smileySize) / 2;
            for (int i = 0; i < smileyIds.length; i++) {
                int row = i / countInRow;
                int col = i % countInRow;
                rect.set(col * smileySize + smileyPadding + offsetLeft, row * smileySize + smileyPadding, (col + 1) * smileySize - smileyPadding + offsetLeft,
                        (row + 1) * smileySize - smileyPadding);
                if (!canvas.quickReject(rect.left, rect.top, rect.right, rect.bottom, Canvas.EdgeType.AA)) {
                    Bitmap img = processor.getSection(smileysSections[i]);
                    if (img != null) {
                        sectionRect.set(smileysX[i] * smileySrcSize + 1, smileysY[i] * smileySrcSize + 1,
                                (smileysX[i] + 1) * smileySrcSize - 1, (smileysY[i] + 1) * smileySrcSize - 1);
                        canvas.drawBitmap(img, sectionRect, rect, paint);
                    }
                }
            }
        }
    }
}