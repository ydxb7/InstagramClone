package ai.tomorrow.photory.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Heart {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public ImageView heartWhite, heartRed;

    public Heart(ImageView heartWhite, ImageView heartRed) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
    }

    public void toggleLike() {
        Log.d(TAG, "toggleLike: toggling heart");

        AnimatorSet animatorSet = new AnimatorSet();

        if (heartRed.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "toggleLike: toggling red heart off.");

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartWhite, "scaleY", 0.1f, 1.5f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartWhite, "scaleX", 0.1f, 1.5f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            animatorSet.playTogether(scaleDownY, scaleDownX);

            heartRed.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);

        } else if (heartRed.getVisibility() == View.GONE) {
            Log.d(TAG, "toggleLike: toggling red heart on.");

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(heartRed, "scaleY", 0.1f, 1.5f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(heartRed, "scaleX", 0.1f, 1.5f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            animatorSet.playTogether(scaleDownY, scaleDownX);

            heartWhite.setVisibility(View.GONE);
            heartRed.setVisibility(View.VISIBLE);
        }
        animatorSet.start();
    }
}
