package im.actor.messenger.app.keyboard.emoji.smiles;

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

import im.actor.messenger.app.keyboard.emoji.SmileProcessor;

/**
 * Author: Korshakov Stepan
 * Created: 04.07.13 22:50
 */
public class SmilesPackView extends View {

    private int rowCount;
    private int countInRow;
    private SmileProcessor processor;
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
    private OnSmileClickListener onSmileClickListener;
    private float touchX, touchY;

    public SmilesPackView(Context context, SmileProcessor processor,
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

    public OnSmileClickListener getOnSmileClickListener() {
        return onSmileClickListener;
    }

    public void setOnSmileClickListener(OnSmileClickListener onSmileClickListener) {
        this.onSmileClickListener = onSmileClickListener;
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
                            if (onSmileClickListener != null) {
                                playSoundEffect(SoundEffectConstants.CLICK);

                                long smileId = smileyIds[index];
                                String smile =  null;
                                char a = (char) (smileId & 0xFFFFFFFF);
                                char b = (char) ((smileId >> 16) & 0xFFFFFFFF);
                                char c = (char) ((smileId >> 32) & 0xFFFFFFFF);
                                char d = (char) ((smileId >> 48) & 0xFFFFFFFF);
                                if (c != 0 && d != 0) {
                                    smile = "" + d + c + b + a;
                                } else if (b != 0) {
                                    smile = b + "" + a;
                                } else {
                                    smile = "" + a;
                                }
                                onSmileClickListener.onEmojiClicked(smile);
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