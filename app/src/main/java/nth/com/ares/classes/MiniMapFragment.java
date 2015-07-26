package nth.com.ares.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MiniMapFragment extends SupportMapFragment {

    private LatLng mPosFija;
    String titulo;

    public MiniMapFragment() {
        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
        super.newInstance(options);

    }

    public static MiniMapFragment newInstance(LatLng posicion,String titulo){

        MiniMapFragment frag = new MiniMapFragment();
        frag.mPosFija = posicion;
        frag.titulo=titulo;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        initMap();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(600,350);
        v.setLayoutParams(params);
        return v;
    }

    private void initMap(){
        if(mPosFija!=null){
            UiSettings settings = getMap().getUiSettings();
            settings.setAllGesturesEnabled(false);
            settings.setMyLocationButtonEnabled(false);
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(mPosFija, 14));
            getMap().addMarker(new MarkerOptions().position(mPosFija).title(titulo));
        }
    }
}