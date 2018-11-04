package jan.schuettken.bierpongleague.basic;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.ConfirmActivity;
import jan.schuettken.bierpongleague.activities.EloTrendActivity;
import jan.schuettken.bierpongleague.activities.LoginActivity;
import jan.schuettken.bierpongleague.activities.OverviewActivity;
import jan.schuettken.bierpongleague.activities.PlayedGamesActivity;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

/**
 * Created by Jan Schüttken on 03.11.2018 at 11:50
 */
public abstract class BasicDrawerPage extends BasicPage implements NavigationView.OnNavigationItemSelectedListener {

    public final static String CURRENT_USER = "CURRENT_USER";
    protected UserData currentUser = null;

    @Override
    public void setContentView(int contentView) {
        super.setContentView(contentView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //pass the current user, so it must not be read from the internet all time
        currentUser = (UserData) getObjectParameter(CURRENT_USER);
        if (currentUser == null) {
            ApiHandler apiHandler = createApiHandler();
            try {
                currentUser = apiHandler.getYourself();
            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                e.printStackTrace();
                switchView(LoginActivity.class, true);
                return;
            }
        }

        //fill Header with information
        View head = navigationView.getHeaderView(navigationView.getHeaderCount() - 1);
        TextView welcome = head.findViewById(R.id.text_name);
        welcome.setText(getResString(R.string.hello_user, currentUser.getFullName()));
        TextView elo = head.findViewById(R.id.text_elo);
        elo.setText(getResString(R.string.your_elo, (int) currentUser.getElo()));

        selectPage();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_overview:
                if (!(this instanceof OverviewActivity)) {
                    switchView(OverviewActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_played_games:
                if (!(this instanceof PlayedGamesActivity)) {
                    switchView(PlayedGamesActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_to_confirm:
                if (!(this instanceof ConfirmActivity)) {
                    switchView(ConfirmActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_elo_trend:
                if (!(this instanceof EloTrendActivity)) {
                    switchView(EloTrendActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Call {@link #selectPage(int) selectPage} with the right id
     */
    protected abstract void selectPage();

    /**
     * sets the right menu option as selected (displayed in blue)
     *
     * @param id the R.id.xxx for from the nav
     */
    protected void selectPage(int id) {
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(id).setChecked(true);
    }
}
