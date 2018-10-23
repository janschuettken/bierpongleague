package jan.schuettken.bierpongleague.basic;

import android.view.View;

/**
 * Created by Jan Schüttken on 26.09.2018 at 18:14
 */
public interface TutorialPage {
    void initTutorial();

    void initTutorial(String[] values);

    void closeTutorial(View view);
}
