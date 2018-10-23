package jan.schuettken.bierpongleague.basic;

import java.io.Serializable;

/**
 * Created by Jan on 29.07.2017.
 */

public interface PageInterfaceLarge extends PageInterface {


    void switchView(Class<?> o);

    void switchView(Class<?> o, boolean finish, boolean clearTop);

    void switchView(Class<?> o, boolean finish, String parameterName, String parameter);

    void switchView(Class<?> o, boolean finish, String parameterName, Serializable parameter);

    void switchView(Class<?> o, Portable... portables);

    void switchView(Class<?> o, boolean finish, Portable... portables);

    void switchBackToView(Class<?> o);

    void switchForResult(Class<?> o, int request);

    void switchForResult(Class<?> o, int request, Portable... portables);

    void finishWithResult(int result);

    void finishWithResult(int result, Portable... portables);

    String getParameter(String name);

    Serializable getObjectParameter(String name);


}
