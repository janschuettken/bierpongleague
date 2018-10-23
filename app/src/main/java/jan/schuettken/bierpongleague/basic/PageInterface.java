package jan.schuettken.bierpongleague.basic;

/**
 * Created by Jan on 29.07.2017.
 * This should be added to every OnlinePage-class
 */

public interface PageInterface {

    /**
     * This method switches the page
     * @param page the next page
     * @param finish should this page be finished after?
     */
    void switchView(Class<?> page, boolean finish);
}
