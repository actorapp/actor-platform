package im.actor.messenger.app.fragment.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.pickers.file.util.FileTypes;

import im.actor.messenger.R;
import im.actor.messenger.app.view.EngineHolderAdapter;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.storage.scheme.media.Document;
import im.actor.messenger.util.TextUtils;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.core.Core.users;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DocumentAdapter extends EngineHolderAdapter<Document> {

    public DocumentAdapter(EngineUiList<Document> engine, Context context) {
        super(engine, context);
    }

    @Override
    protected ViewHolder<Document> createHolder(Document obj) {
        return new DocumentViewHolder();
    }

    @Override
    public long getItemId(Document obj) {
        return obj.getListId();
    }

    private class DocumentViewHolder extends ViewHolder<Document> {

        private TextView titleView;
        private TextView subtitleView;
        private ImageView fileIcon;
        private View subheaderCont;
        private TextView subheader;
        private View div;

        @Override
        public View init(Document data, ViewGroup viewGroup, Context context) {
            View res = LayoutInflater.from(context).inflate(R.layout.adapter_doc, viewGroup, false);
            titleView = (TextView) res.findViewById(R.id.title);
            subtitleView = (TextView) res.findViewById(R.id.subtitle);
            fileIcon = (ImageView) res.findViewById(R.id.icon);
            subheaderCont = res.findViewById(R.id.headerCont);
            subheader = (TextView) res.findViewById(R.id.header);
            subheader.setTypeface(Fonts.medium());
            div = res.findViewById(R.id.divider);
            return res;
        }

        @Override
        public void bind(Document data, int position, Context context) {
            boolean showDiv = true;
            if (position > 0) {
                showDiv = !TextUtils.areSameDays(getItem(position - 1).getAddedTime(),
                        data.getAddedTime());
            }

            if (showDiv) {
                subheaderCont.setVisibility(View.VISIBLE);
                subheader.setText(TextUtils.formatDate(data.getAddedTime()));
            } else {
                subheaderCont.setVisibility(View.GONE);
            }

            titleView.setText(data.getFileName());
            UserVM userModel = users().get(data.getSender());
            if (userModel != null) {
                subtitleView.setText(Formatter.formatFileSize(data.getFileLocation().getFileSize()) + ", " + userModel.getName() + ", " + TextUtils.formatTime(data.getAddedTime()));
            } else {
                subtitleView.setText(Formatter.formatFileSize(data.getFileLocation().getFileSize()) + ", " + TextUtils.formatTime(data.getAddedTime()));
            }

            int type = FileTypes.getType(getFileExtension(data.getFileName()));
            switch (type) {
                default:
                case FileTypes.TYPE_UNKNOWN:
                    fileIcon.setImageResource(R.drawable.picker_unknown);
                    break;
                case FileTypes.TYPE_APK:
                    fileIcon.setImageResource(R.drawable.picker_apk);
                    break;
                case FileTypes.TYPE_MUSIC:
                    fileIcon.setImageResource(R.drawable.picker_music);
                    break;
                case FileTypes.TYPE_PICTURE:
                    fileIcon.setImageResource(R.drawable.picker_unknown);
                    break;
                case FileTypes.TYPE_DOC:
                    fileIcon.setImageResource(R.drawable.picker_doc);
                    break;
                case FileTypes.TYPE_RAR:
                    fileIcon.setImageResource(R.drawable.picker_rar);
                    break;
                case FileTypes.TYPE_VIDEO:
                    fileIcon.setImageResource(R.drawable.picker_video);
                    break;
                case FileTypes.TYPE_ZIP:
                    fileIcon.setImageResource(R.drawable.picker_zip);
                    break;
                case FileTypes.TYPE_XLS:
                    fileIcon.setImageResource(R.drawable.picker_xls);
                    break;
                case FileTypes.TYPE_PPT:
                    fileIcon.setImageResource(R.drawable.picker_ppt);
                    break;
                case FileTypes.TYPE_CSV:
                    fileIcon.setImageResource(R.drawable.picker_csv);
                    break;
                case FileTypes.TYPE_HTM:
                    fileIcon.setImageResource(R.drawable.picker_htm);
                    break;
                case FileTypes.TYPE_HTML:
                    fileIcon.setImageResource(R.drawable.picker_html);
                    break;
                case FileTypes.TYPE_PDF:
                    fileIcon.setImageResource(R.drawable.picker_pdf);
                    break;
            }

            if (position + 1 < getCount()) {
                if (!TextUtils.areSameDays(getItem(position + 1).getAddedTime(),
                        data.getAddedTime())) {
                    div.setVisibility(View.GONE);
                } else {
                    div.setVisibility(View.VISIBLE);
                }
            } else {
                div.setVisibility(View.GONE);
            }
        }

        private String getFileExtension(String name) {
            int lastIndexOf = name.lastIndexOf(".");
            if (lastIndexOf == -1) {
                return ""; // empty extension
            }
            return name.substring(lastIndexOf + 1);
        }
    }
}
