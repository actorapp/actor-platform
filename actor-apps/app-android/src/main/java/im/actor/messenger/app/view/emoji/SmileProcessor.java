/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.view.emoji;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.core.utils.IOUtils;
import im.actor.messenger.app.util.Logger;
import im.actor.messenger.app.util.images.common.ImageMetadata;
import im.actor.messenger.app.util.images.ops.ImageLoading;
import im.actor.messenger.app.util.images.sources.FileSource;
import im.actor.messenger.app.view.emoji.smiles.SmilesPack;
import im.actor.messenger.app.view.emoji.smiles.SmilesRecentListener;
import im.actor.messenger.app.view.emoji.smiles.SmilesRecentsController;
import im.actor.messenger.app.view.emoji.smiles.SmileysPack;
import im.actor.messenger.app.view.keyboard.emoji.smiles.SmilesListener;


public class SmileProcessor {

    private static final String TAG = "Emoji";

    private static final int COUNT_IN_ROW = 32;
    private static final int COUNT_IN_COL = 26;
    private static final int SECTION_SIDE = 8;
    private static final int SECTION_ROW_COUNT = COUNT_IN_ROW / SECTION_SIDE;
    private static final int SECTION_COL_COUNT = (int) Math.ceil((float) COUNT_IN_COL / SECTION_SIDE);

    public static final int CONFIGURATION_DIALOGS = 0;
    public static final int CONFIGURATION_BUBBLES = 1;

    private static final Long[] EMOJI_MAP = {2302179L, 3154147L, 3219683L, 3285219L, 3350755L, 3416291L, 3481827L, 3547363L, 3612899L, 3678435L, 3743971L, 169L, 174L, 8252L, 8265L, 8482L, 8505L, 8596L, 8597L, 8598L, 8599L, 8600L, 8601L, 8617L, 8618L, 8986L, 8987L, 9193L, 9194L, 9195L, 9196L, 9200L, 9203L, 9410L, 9642L, 9643L, 9654L, 9664L, 9723L, 9724L, 9725L, 9726L, 9728L, 9729L, 9742L, 9745L, 9748L, 9749L, 9757L, 9786L, 9800L, 9801L, 9802L, 9803L, 9804L, 9805L, 9806L, 9807L, 9808L, 9809L, 9810L, 9811L, 9824L, 9827L, 9829L, 9830L, 9832L, 9851L, 9855L, 9875L, 9888L, 9889L, 9898L, 9899L, 9917L, 9918L, 9924L, 9925L, 9934L, 9940L, 9962L, 9970L, 9971L, 9973L, 9978L, 9981L, 9986L, 9989L, 9992L, 9993L, 9994L, 9995L, 9996L, 9999L, 10002L, 10004L, 10006L, 10024L, 10035L, 10036L, 10052L, 10055L, 10060L, 10062L, 10067L, 10068L, 10069L, 10071L, 10084L, 10133L, 10134L, 10135L, 10145L, 10160L, 10175L, 10548L, 10549L, 11013L, 11014L, 11015L, 11035L, 11036L, 11088L, 11093L, 12336L, 12349L, 12951L, 12953L, 3627867140L, 3627867343L, 3627867504L, 3627867505L, 3627867518L, 3627867519L, 3627867534L, 3627867537L, 3627867538L, 3627867539L, 3627867540L, 3627867541L, 3627867542L, 3627867543L, 3627867544L, 3627867545L, 3627867546L, -2865171270784459277L, -2865171266489491990L, -2865171262194524680L, -2865171257899557385L, -2865171253604590105L, -2865171245014655495L, -2865171240719688203L, -2865171236424720905L, -2865171206359949830L, -2865171193475047944L, 3627867649L, 3627867650L, 3627867674L, 3627867695L, 3627867699L, 3627867701L, 3627867702L, 3627867703L, 3627867704L, 3627867705L, 3627867706L, 3627867728L, 3627867904L, 3627867905L, 3627867906L, 3627867907L, 3627867908L, 3627867909L, 3627867910L, 3627867911L, 3627867912L, 3627867913L, 3627867914L, 3627867915L, 3627867916L, 3627867917L, 3627867918L, 3627867919L, 3627867920L, 3627867921L, 3627867922L, 3627867923L, 3627867924L, 3627867925L, 3627867926L, 3627867927L, 3627867928L, 3627867929L, 3627867930L, 3627867931L, 3627867932L, 3627867933L, 3627867934L, 3627867935L, 3627867936L, 3627867952L, 3627867953L, 3627867954L, 3627867955L, 3627867956L, 3627867957L, 3627867959L, 3627867960L, 3627867961L, 3627867962L, 3627867963L, 3627867964L, 3627867965L, 3627867966L, 3627867967L, 3627867968L, 3627867969L, 3627867970L, 3627867971L, 3627867972L, 3627867973L, 3627867974L, 3627867975L, 3627867976L, 3627867977L, 3627867978L, 3627867979L, 3627867980L, 3627867981L, 3627867982L, 3627867983L, 3627867984L, 3627867985L, 3627867986L, 3627867987L, 3627867988L, 3627867989L, 3627867990L, 3627867991L, 3627867992L, 3627867993L, 3627867994L, 3627867995L, 3627867996L, 3627867997L, 3627867998L, 3627867999L, 3627868000L, 3627868001L, 3627868002L, 3627868003L, 3627868004L, 3627868005L, 3627868006L, 3627868007L, 3627868008L, 3627868009L, 3627868010L, 3627868011L, 3627868012L, 3627868013L, 3627868014L, 3627868015L, 3627868016L, 3627868017L, 3627868018L, 3627868019L, 3627868020L, 3627868021L, 3627868022L, 3627868023L, 3627868024L, 3627868025L, 3627868026L, 3627868027L, 3627868028L, 3627868032L, 3627868033L, 3627868034L, 3627868035L, 3627868036L, 3627868037L, 3627868038L, 3627868039L, 3627868040L, 3627868041L, 3627868042L, 3627868043L, 3627868044L, 3627868045L, 3627868046L, 3627868047L, 3627868048L, 3627868049L, 3627868050L, 3627868051L, 3627868064L, 3627868065L, 3627868066L, 3627868067L, 3627868068L, 3627868069L, 3627868070L, 3627868071L, 3627868072L, 3627868073L, 3627868074L, 3627868075L, 3627868076L, 3627868077L, 3627868078L, 3627868079L, 3627868080L, 3627868081L, 3627868082L, 3627868083L, 3627868084L, 3627868085L, 3627868086L, 3627868087L, 3627868088L, 3627868089L, 3627868090L, 3627868091L, 3627868092L, 3627868093L, 3627868094L, 3627868095L, 3627868096L, 3627868097L, 3627868098L, 3627868099L, 3627868100L, 3627868102L, 3627868103L, 3627868104L, 3627868105L, 3627868106L, 3627868128L, 3627868129L, 3627868130L, 3627868131L, 3627868132L, 3627868133L, 3627868134L, 3627868135L, 3627868136L, 3627868137L, 3627868138L, 3627868139L, 3627868140L, 3627868141L, 3627868142L, 3627868143L, 3627868144L, 3627932672L, 3627932673L, 3627932674L, 3627932675L, 3627932676L, 3627932677L, 3627932678L, 3627932679L, 3627932680L, 3627932681L, 3627932682L, 3627932683L, 3627932684L, 3627932685L, 3627932686L, 3627932687L, 3627932688L, 3627932689L, 3627932690L, 3627932691L, 3627932692L, 3627932693L, 3627932694L, 3627932695L, 3627932696L, 3627932697L, 3627932698L, 3627932699L, 3627932700L, 3627932701L, 3627932702L, 3627932703L, 3627932704L, 3627932705L, 3627932706L, 3627932707L, 3627932708L, 3627932709L, 3627932710L, 3627932711L, 3627932712L, 3627932713L, 3627932714L, 3627932715L, 3627932716L, 3627932717L, 3627932718L, 3627932719L, 3627932720L, 3627932721L, 3627932722L, 3627932723L, 3627932724L, 3627932725L, 3627932726L, 3627932727L, 3627932728L, 3627932729L, 3627932730L, 3627932731L, 3627932732L, 3627932733L, 3627932734L, 3627932736L, 3627932738L, 3627932739L, 3627932740L, 3627932741L, 3627932742L, 3627932743L, 3627932744L, 3627932745L, 3627932746L, 3627932747L, 3627932748L, 3627932749L, 3627932750L, 3627932751L, 3627932752L, 3627932753L, 3627932754L, 3627932755L, 3627932756L, 3627932757L, 3627932758L, 3627932759L, 3627932760L, 3627932761L, 3627932762L, 3627932763L, 3627932764L, 3627932765L, 3627932766L, 3627932767L, 3627932768L, 3627932769L, 3627932770L, 3627932771L, 3627932772L, 3627932773L, 3627932774L, 3627932775L, 3627932776L, 3627932777L, 3627932778L, 3627932779L, 3627932780L, 3627932781L, 3627932782L, 3627932783L, 3627932784L, 3627932785L, 3627932786L, 3627932787L, 3627932788L, 3627932789L, 3627932790L, 3627932791L, 3627932792L, 3627932793L, 3627932794L, 3627932795L, 3627932796L, 3627932797L, 3627932798L, 3627932799L, 3627932800L, 3627932801L, 3627932802L, 3627932803L, 3627932804L, 3627932805L, 3627932806L, 3627932807L, 3627932808L, 3627932809L, 3627932810L, 3627932811L, 3627932812L, 3627932813L, 3627932814L, 3627932815L, 3627932816L, 3627932817L, 3627932818L, 3627932819L, 3627932820L, 3627932821L, 3627932822L, 3627932823L, 3627932824L, 3627932825L, 3627932826L, 3627932827L, 3627932828L, 3627932829L, 3627932830L, 3627932831L, 3627932832L, 3627932833L, 3627932834L, 3627932835L, 3627932836L, 3627932837L, 3627932838L, 3627932839L, 3627932840L, 3627932841L, 3627932842L, 3627932843L, 3627932844L, 3627932845L, 3627932846L, 3627932847L, 3627932848L, 3627932849L, 3627932850L, 3627932851L, 3627932852L, 3627932853L, 3627932854L, 3627932855L, 3627932856L, 3627932857L, 3627932858L, 3627932859L, 3627932860L, 3627932861L, 3627932862L, 3627932863L, 3627932864L, 3627932865L, 3627932866L, 3627932867L, 3627932868L, 3627932869L, 3627932870L, 3627932871L, 3627932872L, 3627932873L, 3627932874L, 3627932875L, 3627932876L, 3627932877L, 3627932878L, 3627932879L, 3627932880L, 3627932881L, 3627932882L, 3627932883L, 3627932884L, 3627932885L, 3627932886L, 3627932887L, 3627932888L, 3627932889L, 3627932890L, 3627932891L, 3627932892L, 3627932893L, 3627932894L, 3627932895L, 3627932896L, 3627932897L, 3627932898L, 3627932899L, 3627932900L, 3627932901L, 3627932902L, 3627932903L, 3627932904L, 3627932905L, 3627932906L, 3627932907L, 3627932908L, 3627932909L, 3627932910L, 3627932911L, 3627932912L, 3627932913L, 3627932914L, 3627932915L, 3627932916L, 3627932917L, 3627932918L, 3627932919L, 3627932921L, 3627932922L, 3627932923L, 3627932924L, 3627932928L, 3627932929L, 3627932930L, 3627932931L, 3627932932L, 3627932933L, 3627932934L, 3627932935L, 3627932936L, 3627932937L, 3627932938L, 3627932939L, 3627932940L, 3627932941L, 3627932942L, 3627932943L, 3627932944L, 3627932945L, 3627932946L, 3627932947L, 3627932948L, 3627932949L, 3627932950L, 3627932951L, 3627932952L, 3627932953L, 3627932954L, 3627932955L, 3627932956L, 3627932957L, 3627932958L, 3627932959L, 3627932960L, 3627932961L, 3627932962L, 3627932963L, 3627932964L, 3627932965L, 3627932966L, 3627932967L, 3627932968L, 3627932969L, 3627932970L, 3627932971L, 3627932972L, 3627932973L, 3627932974L, 3627932975L, 3627932976L, 3627932977L, 3627932978L, 3627932979L, 3627932980L, 3627932981L, 3627932982L, 3627932983L, 3627932984L, 3627932985L, 3627932986L, 3627932987L, 3627932988L, 3627932989L, 3627933008L, 3627933009L, 3627933010L, 3627933011L, 3627933012L, 3627933013L, 3627933014L, 3627933015L, 3627933016L, 3627933017L, 3627933018L, 3627933019L, 3627933179L, 3627933180L, 3627933181L, 3627933182L, 3627933183L, 3627933184L, 3627933185L, 3627933186L, 3627933187L, 3627933188L, 3627933189L, 3627933190L, 3627933191L, 3627933192L, 3627933193L, 3627933194L, 3627933195L, 3627933196L, 3627933197L, 3627933198L, 3627933199L, 3627933200L, 3627933201L, 3627933202L, 3627933203L, 3627933204L, 3627933205L, 3627933206L, 3627933207L, 3627933208L, 3627933209L, 3627933210L, 3627933211L, 3627933212L, 3627933213L, 3627933214L, 3627933215L, 3627933216L, 3627933217L, 3627933218L, 3627933219L, 3627933220L, 3627933221L, 3627933222L, 3627933223L, 3627933224L, 3627933225L, 3627933226L, 3627933227L, 3627933228L, 3627933229L, 3627933230L, 3627933231L, 3627933232L, 3627933233L, 3627933234L, 3627933235L, 3627933236L, 3627933237L, 3627933238L, 3627933239L, 3627933240L, 3627933241L, 3627933242L, 3627933243L, 3627933244L, 3627933245L, 3627933246L, 3627933247L, 3627933248L, 3627933253L, 3627933254L, 3627933255L, 3627933256L, 3627933257L, 3627933258L, 3627933259L, 3627933260L, 3627933261L, 3627933262L, 3627933263L, 3627933312L, 3627933313L, 3627933314L, 3627933315L, 3627933316L, 3627933317L, 3627933318L, 3627933319L, 3627933320L, 3627933321L, 3627933322L, 3627933323L, 3627933324L, 3627933325L, 3627933326L, 3627933327L, 3627933328L, 3627933329L, 3627933330L, 3627933331L, 3627933332L, 3627933333L, 3627933334L, 3627933335L, 3627933336L, 3627933337L, 3627933338L, 3627933339L, 3627933340L, 3627933341L, 3627933342L, 3627933343L, 3627933344L, 3627933345L, 3627933346L, 3627933347L, 3627933348L, 3627933349L, 3627933350L, 3627933351L, 3627933352L, 3627933353L, 3627933354L, 3627933355L, 3627933356L, 3627933357L, 3627933358L, 3627933359L, 3627933360L, 3627933361L, 3627933362L, 3627933363L, 3627933364L, 3627933365L, 3627933366L, 3627933367L, 3627933368L, 3627933369L, 3627933370L, 3627933371L, 3627933372L, 3627933373L, 3627933374L, 3627933375L, 3627933376L, 3627933377L, 3627933378L, 3627933379L, 3627933380L, 3627933381L};
    private static final long[] EMOJI_SORTED;
    private static final long minEmoji1;
    private static final long maxEmoji1;
    private static final long minEmoji2;
    private static final long maxEmoji2;
    private static final HashSet<Long> EMOJI_SET = new HashSet<Long>();

    static {
        Collections.addAll(EMOJI_SET, EMOJI_MAP);
        EMOJI_SORTED = new long[EMOJI_MAP.length];
        long min1 = 0xFFFF, max1 = 0;
        long min2 = 0xFFFFFFFF, max2 = 0;
        for (int i = 0; i < EMOJI_MAP.length; i++) {
            EMOJI_SORTED[i] = EMOJI_MAP[i];
            if ((EMOJI_SORTED[i] & 0xffff) == EMOJI_SORTED[i]) {
                if (EMOJI_SORTED[i] < min1) {
                    min1 = EMOJI_SORTED[i];
                }
                if (EMOJI_SORTED[i] > max1) {
                    max1 = EMOJI_SORTED[i];
                }
            } else if ((EMOJI_SORTED[i] & 0xffffffff) == EMOJI_SORTED[i]) {
                if (EMOJI_SORTED[i] < min2) {
                    min2 = EMOJI_SORTED[i];
                }
                if (EMOJI_SORTED[i] > max2) {
                    max2 = EMOJI_SORTED[i];
                }
            }
        }

        minEmoji1 = min1;
        maxEmoji1 = max1;

        minEmoji2 = min2;
        maxEmoji2 = max2;
        Arrays.sort(EMOJI_SORTED);
    }

    private static final int LAYOUT_1X = 1;
    private static final int LAYOUT_15X_1 = 2;
    private static final int LAYOUT_15X_2 = 3;
    private static final int LAYOUT_2X_1 = 4;
    private static final int LAYOUT_2X_2 = 5;
    private static SmileProcessor instance;
    private static SmileProcessor processor;

    // protected Bitmap emojiImages;
    protected HashMap<Integer, Bitmap> emojiMap;

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private Application application;
    private float density;

    private HashMap<Long, Integer> indexes;
    private HashMap<Integer, Paint.FontMetricsInt> originalMetrics;

    private int layoutType = LAYOUT_1X;

    private boolean isLoading = false;
    private boolean isLoaded = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private CopyOnWriteArrayList<SmilesListener> listeners = new CopyOnWriteArrayList<SmilesListener>();

    private int emojiSideSize;

    private int rectSize = 0;
    private SmilesRecentListener smilesRecentListener;
    private SmilesRecentsController recentController;

    public static final SmileProcessor emoji() {
        return processor;
    }

    public SmileProcessor(Application application) {
        long start = System.currentTimeMillis();
        this.application = application;
        processor = this;
        density = application.getResources().getDisplayMetrics().density;

        emojiSideSize = (int) (density * 20);

        Logger.d(TAG, "Emoji phase 0 in " + (System.currentTimeMillis() - start) + " ms");

        if (density >= 2 || density == 1) {
            if (density >= 2) {
                // XHDPI and more
                if (SmileysPack.PACK_2) {
                    layoutType = LAYOUT_2X_1;
                } else if (SmileysPack.PACK_15) {
                    layoutType = LAYOUT_15X_1;
                } else if (SmileysPack.PACK_1) {
                    layoutType = LAYOUT_1X;
                } else {
                    throw new RuntimeException("Unable to find smileys pack");
                }
            } else {
                // MDPI
                if (SmileysPack.PACK_1) {
                    layoutType = LAYOUT_1X;
                } else if (SmileysPack.PACK_15) {
                    layoutType = LAYOUT_15X_1;
                } else if (SmileysPack.PACK_2) {
                    layoutType = LAYOUT_2X_2;
                } else {
                    throw new RuntimeException("Unable to find smileys pack");
                }
            }
        } else {
            if (density > 1) { // 1.3333 and 1.5
                // HDPI & TVDPI
                if (SmileysPack.PACK_15) {
                    layoutType = LAYOUT_15X_1;
                } else if (SmileysPack.PACK_2) {
                    layoutType = LAYOUT_2X_1;
                } else if (SmileysPack.PACK_1) {
                    layoutType = LAYOUT_1X;
                } else {
                    throw new RuntimeException("Unable to find smileys pack");
                }
            } else { // 0.75
                // LDPI
                if (SmileysPack.PACK_15) {
                    layoutType = LAYOUT_15X_2;
                } else if (SmileysPack.PACK_1) {
                    layoutType = LAYOUT_1X;
                } else if (SmileysPack.PACK_2) {
                    layoutType = LAYOUT_2X_2;
                } else {
                    throw new RuntimeException("Unable to find smileys pack");
                }
            }
        }

        Logger.d(TAG, "Emoji phase 1 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        switch (layoutType) {
            default:
            case LAYOUT_1X:
                rectSize = 28;
                break;
            case LAYOUT_15X_1:
                rectSize = 36;
                break;
            case LAYOUT_15X_2:
                rectSize = 18;
                break;
            case LAYOUT_2X_1:
                rectSize = 56;
                break;
            case LAYOUT_2X_2:
                rectSize = 28;
                break;
        }

        indexes = new HashMap<Long, Integer>();
        emojiMap = new HashMap<Integer, Bitmap>();
        originalMetrics = new HashMap<Integer, Paint.FontMetricsInt>();

        TextPaint bodyPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        // bodyPaint.setTypeface(FontController.loadTypeface(application, "normal"));
        bodyPaint.setTextSize(getSp(16));
        bodyPaint.setColor(0xff000000);
        originalMetrics.put(CONFIGURATION_BUBBLES, bodyPaint.getFontMetricsInt());

        bodyPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        // bodyPaint.setTypeface(FontController.loadTypeface(application, "light"));
        bodyPaint.setColor(0xff808080);
        bodyPaint.setTextSize(getSp(15.5f));
        originalMetrics.put(CONFIGURATION_DIALOGS, bodyPaint.getFontMetricsInt());

        Logger.d(TAG, "Emoji phase 2 in " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        for (int i = 0; i < EMOJI_MAP.length; i++) {
            indexes.put(EMOJI_MAP[i], i);
        }

        Logger.d(TAG, "Emoji phase 3 in " + (System.currentTimeMillis() - start) + " ms");
    }

    protected int getSp(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, application.getResources().getDisplayMetrics());
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void registerListener(SmilesListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(SmilesListener listener) {
        listeners.remove(listener);
    }

    public void loadEmoji() {
        if (isLoaded) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;
        new Thread() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                long start = System.currentTimeMillis();
                Logger.d(TAG, "emoji loading start");
                try {

                    boolean useScale = false;
                    String fileName = null;
                    String fileNameAlpha = null;


                    switch (layoutType) {
                        default:
                        case LAYOUT_1X:
                            fileName = "emoji_c_1.jpg";
                            fileNameAlpha = "emoji_a_1.jpg";
                            useScale = false;
                            break;
                        case LAYOUT_15X_1:
                            fileName = "emoji_c_15.jpg";
                            fileNameAlpha = "emoji_a_15.jpg";
                            useScale = false;
                            break;
                        case LAYOUT_15X_2:
                            fileName = "emoji_c_15.jpg";
                            fileNameAlpha = "emoji_a_15.jpg";
                            useScale = true;
                            break;
                        case LAYOUT_2X_1:
                            fileName = "emoji_c_2.jpg";
                            fileNameAlpha = "emoji_a_2.jpg";
                            useScale = false;
                            break;
                        case LAYOUT_2X_2:
                            fileName = "emoji_c_2.jpg";
                            fileNameAlpha = "emoji_a_2.jpg";
                            useScale = true;
                            break;
                    }

                    File sourceFile = application.getFileStreamPath(fileName);
                    if (!sourceFile.exists()) {
                        InputStream colorsIs = SmileProcessor.this.application.getAssets().open(fileName);
                        IOUtils.copy(colorsIs, sourceFile);
                        colorsIs.close();
                    }

                    File sourceAlphaFile = application.getFileStreamPath(fileNameAlpha);
                    if (!sourceAlphaFile.exists()) {
                        InputStream colorsIs = SmileProcessor.this.application.getAssets().open(fileNameAlpha);
                        IOUtils.copy(colorsIs, sourceAlphaFile);
                        colorsIs.close();
                    }

                    ImageMetadata metadata = new FileSource(sourceFile.getAbsolutePath()).getImageMetadata();
                    int w = useScale ? metadata.getW() / 2 : metadata.getW();
                    int h = useScale ? metadata.getH() / 2 : metadata.getH();

                    Bitmap colorsBitmap;
                    Bitmap alphaBitmap;

                    if (useScale) {
                        colorsBitmap = ImageLoading.loadBitmap(sourceFile.getAbsolutePath(), 2);
                        alphaBitmap = ImageLoading.loadBitmap(sourceAlphaFile.getAbsolutePath(), 2);
                    } else {
                        colorsBitmap = ImageLoading.loadBitmap(sourceFile.getAbsolutePath(), 1);
                        alphaBitmap = ImageLoading.loadBitmap(sourceAlphaFile.getAbsolutePath(), 1);
                    }

                    // Bitmap colorsBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    // BitmapDecoderEx.decodeReuseBitmapScaled(sourceFile.getAbsolutePath(), colorsBitmap);
                    // colorsBitmap.setHasAlpha(true);

                    // BitmapDecoderEx.decodeReuseBitmapBlend(sourceAlphaFile.getAbsolutePath(), colorsBitmap, useScale);

                    Logger.d(TAG, "emoji pre-loaded in " + (System.currentTimeMillis() - start) + " ms");

                    int[] resultColors = new int[rectSize * SECTION_SIDE * rectSize * SECTION_SIDE];
                    int[] tmpColors = new int[rectSize * SECTION_SIDE * rectSize * SECTION_SIDE];

                    int[] order = new int[]{8, 9, 10, 11, 4, 5, 6, 7, 0, 1, 2, 3, 12, 13, 14, 15};
                    int stride = rectSize * SECTION_SIDE;
                    for (int ordinal : order) {
                        int col = ordinal % SECTION_COL_COUNT;
                        int row = ordinal / SECTION_COL_COUNT;

                        int leftOffset = col * stride;
                        int topOffset = row * stride;
                        int width = stride;
                        int height = stride;
                        if (row == SECTION_ROW_COUNT - 1) {
                            height = colorsBitmap.getHeight() - topOffset;
                        }

                        colorsBitmap.getPixels(tmpColors, 0, stride, leftOffset, topOffset, width, height);
                        for (int ind = 0; ind < resultColors.length; ind++) {
                            resultColors[ind] = 0xFFFFFF & tmpColors[ind];
                        }
                        alphaBitmap.getPixels(tmpColors, 0, stride, leftOffset, topOffset, width, height);
                        for (int ind = 0; ind < resultColors.length; ind++) {
                            resultColors[ind] = resultColors[ind] | ((tmpColors[ind] & 0xFF) << 24);
                        }

                        Bitmap section = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        /*Canvas canvas = new Canvas(section);
                        canvas.drawBitmap(colorsBitmap, new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height),
                                new Rect(0, 0, width, height), new Paint());*/
                        section.setPixels(resultColors, 0, stride, 0, 0, width, height);
                        emojiMap.put(ordinal, section);

                        Logger.d(TAG, "emoji region loaded in " + (System.currentTimeMillis() - start) + " ms");
                    }

                    recentController = SmilesRecentsController.getInstance(application);

                    isLoaded = true;
                    notifyEmojiUpdated(true);
                    Logger.d(TAG, "emoji loaded in " + (System.currentTimeMillis() - start) + " ms");
                } catch (Throwable t) {
                    t.printStackTrace();
                    Logger.d(TAG, "emoji loading error");
                    isLoaded = false;
                    isLoading = false;
                }
            }
        }.start();
    }

    private void notifyEmojiUpdated(final boolean completed) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, "notify");
                for (SmilesListener listener : listeners) {
                    listener.onSmilesUpdated(completed);
                }
            }
        });
    }

    public void waitForEmoji() {
        if (isLoaded) {
            return;
        }

        final Object lock = new Object();
        synchronized (lock) {
            listeners.add(new SmilesListener() {
                @Override
                public void onSmilesUpdated(boolean completed) {
                    synchronized (lock){
                        lock.notify();
                    }

                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private long getId(String val) {
        long id = 0;
        if (val.length() == 1) {
            id = val.charAt(0);
        } else {
            id = ((long) val.charAt(0) << 16) + (long) val.charAt(1);
        }

        return id;
    }


    public Bitmap getBitmap(String emojiString) {
        return getSection(emojiString.charAt(0));
    }

    public void upRecent(long smileId) {
        SmilesPack.upRecent(smileId);
        if (smilesRecentListener != null) {
            smilesRecentListener.onSmilesUpdated();
        }

    }

    public void setRecentUpdateListener(SmilesRecentListener smilesRecentListener) {
        this.smilesRecentListener = smilesRecentListener;
    }

    public SmilesRecentsController getRecentController() {
        return recentController;
    }

    private class SpanDescription {

        private SpanDescription(long id, int start, int end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }

        public long id;
        public int start, end;
    }

    public String cutEmoji(String s) {
        StringBuilder stringBuilder = new StringBuilder();

        long prev = 0;
        long prevLong = 0;
        int prevLongCount = 0;
        int lastTextPos = 0;

        ArrayList<SpanDescription> list = new ArrayList<SpanDescription>();

        for (int i = 0; i < s.length(); i++) {
            long current = s.charAt(i);

            if (prevLongCount == 3) {
                long prevId = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (EMOJI_SET.contains(prevId)) {
                    if (lastTextPos < i - 3) {
                        stringBuilder.append(s.substring(lastTextPos, i - 3));
                        lastTextPos = i - 3;
                    }
                    stringBuilder.append(":smile:");
                    lastTextPos += 4;

                    list.add(new SpanDescription(prevId, i - 3, i + 1));

                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (prev != 0) {
                long prevId = ((prev & 0xFFFF) << 16) + current;

                if (EMOJI_SET.contains(prevId)) {

                    if (lastTextPos < i - 1) {
                        stringBuilder.append(s.substring(lastTextPos, i - 1));
                        lastTextPos = i - 1;
                    }
                    stringBuilder.append(":smile:");
                    lastTextPos += 2;

                    list.add(new SpanDescription(prevId, i - 1, i + 1));

                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (EMOJI_SET.contains(current)) {

                if (lastTextPos < i) {
                    stringBuilder.append(s.substring(lastTextPos, i));
                    lastTextPos = i;
                }
                stringBuilder.append(":smile:");
                lastTextPos += 1;

                list.add(new SpanDescription(current, i, i + 1));

                prev = 0;
                prevLong = 0;
                prevLongCount = 0;
            } else {
                prev = current;
                prevLong = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (prevLongCount < 3) {
                    prevLongCount++;
                }
            }
        }

        if (lastTextPos < s.length()) {
            stringBuilder.append(s.substring(lastTextPos, s.length()));
        }

        return stringBuilder.toString();
    }

    public String fixStringCompat(String src) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return src;
        } else {
            return cutEmoji(src);
        }
    }

    public Spannable processEmojiCutMutable(String s, int mode) {
        StringBuilder stringBuilder = new StringBuilder();

        long prev = 0;
        long prevLong = 0;
        int prevLongCount = 0;
        int lastTextPos = 0;

        ArrayList<SpanDescription> list = new ArrayList<SpanDescription>();

        for (int i = 0; i < s.length(); i++) {
            long current = s.charAt(i);

            if (prevLongCount == 3) {
                long prevId = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (EMOJI_SET.contains(prevId)) {
                    if (lastTextPos < i - 3) {
                        stringBuilder.append(s.substring(lastTextPos, i - 3));
                        lastTextPos = i - 3;
                    }
                    stringBuilder.append("++++");
                    lastTextPos += 4;

                    list.add(new SpanDescription(prevId, i - 3, i + 1));

                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (prev != 0) {
                long prevId = ((prev & 0xFFFF) << 16) + current;

                if (EMOJI_SET.contains(prevId)) {

                    if (lastTextPos < i - 1) {
                        stringBuilder.append(s.substring(lastTextPos, i - 1));
                        lastTextPos = i - 1;
                    }
                    stringBuilder.append("++");
                    lastTextPos += 2;

                    list.add(new SpanDescription(prevId, i - 1, i + 1));

                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (EMOJI_SET.contains(current)) {

                if (lastTextPos < i) {
                    stringBuilder.append(s.substring(lastTextPos, i));
                    lastTextPos = i;
                }
                stringBuilder.append("+");
                lastTextPos += 1;

                list.add(new SpanDescription(current, i, i + 1));
                /*stringBuilder.setSpan(new ImageSpan(res, isAlignBottom ? ImageSpan.ALIGN_BOTTOM : ImageSpan.ALIGN_BASELINE),
                        i, i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

                prev = 0;
                prevLong = 0;
                prevLongCount = 0;
            } else {
                prev = current;
                prevLong = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (prevLongCount < 3) {
                    prevLongCount++;
                }
            }
        }

        if (lastTextPos < s.length()) {
            stringBuilder.append(s.substring(lastTextPos, s.length()));
        }

        Spannable spannable = spannableFactory.newSpannable(stringBuilder.toString());
        for (SpanDescription description : list) {
            spannable.setSpan(new EmojiSpan(this, indexes.get(description.id), emojiSideSize, originalMetrics.get(mode)), description.start, description.end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public Spannable processEmojiMutable(CharSequence s, int mode) {

        long prev = 0;
        long prevLong = 0;
        int prevLongCount = 0;

        ArrayList<SpanDescription> list = new ArrayList<SpanDescription>();

        for (int i = 0; i < s.length(); i++) {
            long current = s.charAt(i);

            if (prevLongCount == 3) {
                long prevId = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (Arrays.binarySearch(EMOJI_SORTED, prevId) >= 0) {
                    list.add(new SpanDescription(prevId, i - 3, i + 1));
                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (prev != 0) {
                long prevId = ((prev & 0xFFFF) << 16) + current;
                if (Arrays.binarySearch(EMOJI_SORTED, prevId) >= 0) {
                    list.add(new SpanDescription(prevId, i - 1, i + 1));
                    prev = 0;
                    prevLong = 0;
                    prevLongCount = 0;
                    continue;
                }
            }

            if (Arrays.binarySearch(EMOJI_SORTED, current) >= 0) {
                list.add(new SpanDescription(current, i, i + 1));
                prev = 0;
                prevLong = 0;
                prevLongCount = 0;
            } else {
                prev = current;
                prevLong = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (prevLongCount < 3) {
                    prevLongCount++;
                }
            }
        }

        Spannable spannable = spannableFactory.newSpannable(s);
        for (SpanDescription description : list) {
            spannable.setSpan(new EmojiSpan(this, indexes.get(description.id),
                            emojiSideSize, originalMetrics.get(mode)), description.start, description.end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public Spannable processEmojiCompatMutable(CharSequence s, int mode) {
        return processEmojiMutable(s, mode);
    }

    public static long[] findFirstUniqEmoji(String s, int count) {
        long prev = 0;
        long prevLong = 0;
        int prevLongCount = 0;

        long[] res = new long[count];
        int index = 0;
        HashSet<Long> founded = new HashSet<Long>();

        for (int i = 0; i < s.length(); i++) {
            long current = s.charAt(i);

            if (prevLongCount == 3) {
                long prevId = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (EMOJI_SET.contains(prevId) && !founded.contains(prevId)) {
                    founded.add(prevId);
                    res[index++] = prevId;
                    if (index >= count)
                        break;
                }
            }

            if (prev != 0) {
                long prevId = ((prev & 0xFFFF) << 16) + current;

                if (EMOJI_SET.contains(prevId) && !founded.contains(prevId)) {
                    founded.add(prevId);
                    res[index++] = prevId;
                    if (index >= count)
                        break;
                }
            }

            if (EMOJI_SET.contains(current) && !founded.contains(current)) {
                founded.add(current);
                res[index++] = current;
                if (index >= count)
                    break;
            } else {
                prev = current;
                prevLong = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (prevLongCount < 3) {
                    prevLongCount++;
                }
            }
        }

        if (index == count) {
            return res;
        } else {
            long[] res2 = new long[index];
            for (int i = 0; i < index; i++) {
                res2[i] = res[i];
            }
            return res2;
        }
    }

    public static boolean containsEmoji(CharSequence s) {
        long prev = 0;
        long prevLong = 0;
        int prevLongCount = 0;

        for (int i = 0; i < s.length(); i++) {
            long current = s.charAt(i);

//            if (prevLongCount == 3) {
//                long prevId = ((prevLong & 0xFFFFFFFF) << 16) + current;
//                if (Arrays.binarySearch(EMOJI_SORTED, prevId) > 0) {
//                    return true;
//                }
//            }

            if (prev != 0) {
                long prevId = ((prev & 0xFFFF) << 16) + current;

                if ((current >= minEmoji2) && (current <= maxEmoji2) && Arrays.binarySearch(EMOJI_SORTED, prevId) > 0) {
                    return true;
                }
            }

            if ((current >= minEmoji1) && (current <= maxEmoji1) && Arrays.binarySearch(EMOJI_SORTED, current) > 0) {
                return true;
            } else {
                prev = current;
                prevLong = ((prevLong & 0xFFFFFFFF) << 16) + current;
                if (prevLongCount < 3) {
                    prevLongCount++;
                }
            }
        }

        return false;
    }

    public int getRectSize() {
        return rectSize;
    }

    public int getSectionIndex(long emoji) {
        int globalIndex = indexes.get(emoji);
        int x = globalIndex / COUNT_IN_ROW;
        int y = globalIndex % COUNT_IN_ROW;

        return (y / SECTION_SIDE) + (x / SECTION_SIDE) * SECTION_ROW_COUNT;
    }

    public int getSectionX(long emoji) {
        int globalIndex = indexes.get(emoji);
        return (globalIndex % COUNT_IN_ROW) % SECTION_SIDE;
    }

    public int getSectionY(long emoji) {
        int globalIndex = indexes.get(emoji);
        return (globalIndex / COUNT_IN_ROW) % SECTION_SIDE;
    }

    public Bitmap getSection(int index) {
        return emojiMap.get(index);
    }

    private static Paint bitmapPaint = new Paint();

    static {
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFlags(Paint.FILTER_BITMAP_FLAG);
    }

    private static Rect bitmapRect = new Rect();
    private static Rect srcRect = new Rect();

    private class EmojiSpan extends ReplacementSpan {

        private SmileProcessor processor;
        private int offset;
        private int size;
        private int padding;
        private int section;
        private int sectionX;
        private int sectionY;
        private Paint.FontMetricsInt originalMetrics;

        public EmojiSpan(SmileProcessor processor, int index, int size, Paint.FontMetricsInt original) {
            this.processor = processor;
            this.size = size;
            this.originalMetrics = original;

            int x = index / COUNT_IN_ROW;
            int y = index % COUNT_IN_ROW;

            section = (y / SECTION_SIDE) + (x / SECTION_SIDE) * SECTION_ROW_COUNT;
            sectionX = y % SECTION_SIDE;
            sectionY = x % SECTION_SIDE;
        }

        @Override
        public int getSize(Paint paint, CharSequence charSequence, int start, int end, Paint.FontMetricsInt fm) {
            padding = (int) paint.measureText(" ") / 3;

            if (fm != null) {
                fm.ascent = originalMetrics.ascent;
                fm.descent = originalMetrics.descent;

                fm.top = originalMetrics.top;
                fm.bottom = originalMetrics.bottom;
            }
            return size + padding * 2;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Bitmap srcEmoji = processor.emojiMap.get(section);
            if (srcEmoji != null) {
                if (paint.getFontMetrics() != null) {
                    offset = (int) (paint.getFontMetrics().descent);
                }
                x += padding;
                bitmapRect.set((int) x, y - size + offset, (int) (x + size), y + offset);
                srcRect.set(sectionX * rectSize, sectionY * rectSize, (sectionX + 1) * rectSize, (sectionY + 1) * rectSize);
                canvas.drawBitmap(srcEmoji, srcRect, bitmapRect, bitmapPaint);
            }
        }
    }
}