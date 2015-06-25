package im.actor.messenger.app.fragment.tour;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.view.Fonts;

/**
 * Created by Jesus Christ. Amen.
 */
public class TourFragment extends Fragment {
    private static final String ARG_POSITION = "arg_pos";
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tour_fragment, null);

        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        titleView.setTypeface(Fonts.medium());
        TextView bodyView = (TextView) rootView.findViewById(R.id.body);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.image);


        Bundle args = getArguments();
        int position = args.getInt(ARG_POSITION);

        switch (position) {
            default:
            case 1:
                titleView.setText(R.string.tour_groups_title);
                bodyView.setText(R.string.tour_groups_text);
                imageView.setImageResource(R.drawable.intro_groups);
                break;
            case 2:
                titleView.setText(R.string.tour_everywhere_title);
                bodyView.setText(R.string.tour_everywhere_text);
                imageView.setImageResource(R.drawable.intro_subway);
                break;
            case 3:
                titleView.setText(R.string.tour_secure_title);
                bodyView.setText(R.string.tour_secure_text);
                imageView.setImageResource(R.drawable.intro_secure);
                break;
        }

        return rootView;
    }

    public static Fragment getInstance(int position) {
        Fragment fragment = new TourFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }
}
