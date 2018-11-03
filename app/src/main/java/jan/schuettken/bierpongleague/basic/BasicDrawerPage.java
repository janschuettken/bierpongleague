package jan.schuettken.bierpongleague.basic;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.OverviewActivity;
import jan.schuettken.bierpongleague.activities.PlayedGamesActivity;

/**
 * Created by Jan Schüttken on 03.11.2018 at 11:50
 */
public abstract class BasicDrawerPage extends BasicPage implements NavigationView.OnNavigationItemSelectedListener {
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.nav_overview:
                if (!(this instanceof OverviewActivity)) {
                    switchView(OverviewActivity.class, true);
                }
                break;
            case R.id.nav_played_games:
                if (!(this instanceof PlayedGamesActivity)) {
                    switchView(PlayedGamesActivity.class, true);
                }
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
     * @param id
     */
    protected void selectPage(int id) {
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(id).setChecked(true);
    }
}
